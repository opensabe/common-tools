package io.github.opensabe.common.mybatis.interceptor;

import io.github.opensabe.common.mybatis.observation.SQLExecuteContext;
import io.github.opensabe.common.mybatis.observation.SQLExecuteDocumentation;
import io.github.opensabe.common.mybatis.observation.SQLExecuteObservationConvention;
import io.github.opensabe.common.utils.SpringUtil;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class CustomizedTransactionInterceptor extends TransactionInterceptor {

	/**
	 *
	 */
	private static final long serialVersionUID = -7946552130677466888L;

	private static final Map<String, String> TRANSACTION_MANAGER_NAME_MAP = new HashMap<>();

	private static final ThreadLocal<String> CURRENT_TRANSACTION_MANAGER = new ThreadLocal<>();
	private static final ThreadLocal<String> CURRENT_TRANSACTION_ID = new ThreadLocal<>();

	private UnifiedObservationFactory unifiedObservationFactory;

	public static void putTransactionManagerName(String key, String value) {
		TRANSACTION_MANAGER_NAME_MAP.put(key, value);
	}

	@Override
	/**
	 * Determine the specific transaction manager to use for the given transaction,
	 * according to the configuration in SqlSessionFactoryConfiguration.
	 */
	@Nullable
	protected TransactionManager determineTransactionManager(@Nullable TransactionAttribute txAttr) {
		DefaultTransactionAttribute concreteTxAttr = (DefaultTransactionAttribute) txAttr;
		if (StringUtils.isBlank(concreteTxAttr.getQualifier())) {
			concreteTxAttr.setQualifier(CURRENT_TRANSACTION_MANAGER.get());
		}
		log.debug("CustomizedTransactionInterceptor-determineTransactionManager {} use transaction manager: {}", () -> {
			if (txAttr instanceof RuleBasedTransactionAttribute) {
				RuleBasedTransactionAttribute ruleBasedTransactionAttribute = (RuleBasedTransactionAttribute) txAttr;
				return ruleBasedTransactionAttribute.getDescriptor();
			}
			return txAttr;
		}, () -> concreteTxAttr.getQualifier());
		TransactionManager transactionManager = super.determineTransactionManager(concreteTxAttr);
		CURRENT_TRANSACTION_MANAGER.remove();
		return transactionManager;
	}

	public static String getCurrentTransactionId () {
		return CURRENT_TRANSACTION_ID.get();
	}

	@Override
	@Nullable
	public Object invoke(MethodInvocation invocation) throws Throwable {

		// Work out the target class: may be {@code null}.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface.
		Class<?> targetClass = invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null;

		for (String packageName : TRANSACTION_MANAGER_NAME_MAP.keySet()) {
			if (targetClass.getName().startsWith(packageName)) {
				CURRENT_TRANSACTION_MANAGER.set(TRANSACTION_MANAGER_NAME_MAP.get(packageName));
				break;
			}
		}
		UnifiedObservationFactory observationFactory = getUnifiedObservationFactory();
		if (Objects.isNull(observationFactory)  || Objects.isNull(observationFactory.getObservationRegistry())) {
			return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
		}
		String transactionId = String.valueOf(System.currentTimeMillis());
		CURRENT_TRANSACTION_ID.set(transactionId);
		SQLExecuteContext context = new SQLExecuteContext(invocation.getMethod().toGenericString(), transactionId);
		Observation observation = SQLExecuteDocumentation.SQL_EXECUTE_TRANSACTION.observation(
				null,
						SQLExecuteObservationConvention.DEFAULT,
						() -> context, observationFactory.getObservationRegistry())
				.parentObservation(observationFactory.getCurrentObservation())
				.start();
		try {
			log.debug("invoke transaction interceptor: {}, {}", () -> invocation.getMethod().getName(), targetClass::getName);

			// Adapt to TransactionAspectSupport's invokeWithinTransaction...
			return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
		}catch (Throwable e) {
			observation.error(e);
			context.setSuccess(false);
			throw e;
		}finally {
			CURRENT_TRANSACTION_ID.remove();
			observation.stop();
		}
	}

	public UnifiedObservationFactory getUnifiedObservationFactory() {
		if (Objects.isNull(this.unifiedObservationFactory)  && Objects.nonNull(SpringUtil.getApplicationContext())) {
			this.unifiedObservationFactory = SpringUtil.getBean(UnifiedObservationFactory.class);
		}
		return this.unifiedObservationFactory;
	}
}
