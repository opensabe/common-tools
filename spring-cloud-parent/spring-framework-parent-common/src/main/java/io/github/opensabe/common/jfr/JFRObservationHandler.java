package io.github.opensabe.common.jfr;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 全局 ObservationHandler 用于将 Micrometer 的 Observation 转换为 JFR 事件
 * 数据收集通过 ObservationHandler 来实现完成。该handler会通过其回调方法(supportsContext、onStart、onStop、onError)收到有关观察生命周期事件的通知。
 *
 * @see ObservationHandler
 *
 * 使用方法：
 * 1. 实现 ObservationToJFRGenerator 接口，实现将 Observation 转换为 JFR 事件
 * 2. 注册为一个 Bean 即可
 */
@Log4j2
public class JFRObservationHandler<T extends Observation.Context> implements ObservationHandler<T> {

    private final Map<Class<? extends Observation.Context>, List<ObservationToJFRGenerator<? extends Observation.Context>>> generatorMap;

    public JFRObservationHandler(List<ObservationToJFRGenerator<T>> generators) {
        this.generatorMap = generators.stream().collect(
                Collectors.groupingBy(ObservationToJFRGenerator::getContextClazz)
        );
    }

    @Override
    public void onStart(Observation.Context context) {
        List<ObservationToJFRGenerator<? extends Observation.Context>> observationToJFRGenerators = generatorMap.get(context.getClass());
        if (observationToJFRGenerators != null) {
            log.debug("JFRObservationHandler-onStart {} -> observationToJFRGenerators {}", context.getName(), observationToJFRGenerators);
            observationToJFRGenerators.forEach(generator -> {
                try {
                    generator.onStart(context);
                } catch (Exception e) {
                    log.error("JFRObservationHandler-onStart error {}", e.getMessage(), e);
                }
            });
        }
    }

    @Override
    public void onStop(Observation.Context context) {
        List<ObservationToJFRGenerator<? extends Observation.Context>> observationToJFRGenerators = generatorMap.get(context.getClass());
        if (observationToJFRGenerators != null) {
            log.debug("JFRObservationHandler-onStop {} -> observationToJFRGenerators {}", context.getName(), observationToJFRGenerators);
            observationToJFRGenerators.forEach(generator -> {
                try {
                    generator.onStop(context);
                } catch (Exception e) {
                    log.error("JFRObservationHandler-onStop error {}", e.getMessage(), e);
                }
            });
        }
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }
}
