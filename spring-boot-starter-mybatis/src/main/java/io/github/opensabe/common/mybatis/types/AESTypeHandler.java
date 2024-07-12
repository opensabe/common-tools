package io.github.opensabe.common.mybatis.types;

import io.github.opensabe.common.mybatis.plugins.CryptTypeHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

@Log4j2
public class AESTypeHandler extends CryptTypeHandler {

	private static ThreadLocal<String> KEY_HOLDER = new ThreadLocal<String>();

	public static void setKey(String key) {
		KEY_HOLDER.set(key);
	}
	public static String getKey() {
		return KEY_HOLDER.get();
	}
	public static void clearKey() {
		KEY_HOLDER.remove();
	}

	@Override
	protected String encrypt(String origin) {
		var key = getKey();
		if(StringUtils.hasText(key)) {
			try {
				return AESUtil.Encrypt(origin, key);
			} catch (Throwable e) {
				log.error(e);
			}finally {
				clearKey();
			}
		}
		return origin;
	}

	@Override
	protected String decrypt(String origin) {
		var key = getKey();
		if(StringUtils.hasText(key)) {
			try {
				return AESUtil.Decrypt(origin, key);
			} catch (Throwable e) {
				log.error(e);
			}finally {
				clearKey();
			}
		}
		return origin;
	}

}
