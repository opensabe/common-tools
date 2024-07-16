package io.github.opensabe.mapstruct.processor;

import io.github.opensabe.mapstruct.core.MapperRepository;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * 最后将自动生成的Mapper添加到{@link MapperRepository}中
 * @author heng.ma
 */
@SupportedAnnotationTypes("org.mapstruct.Mapper")
public class RepositoryGeneratorProcessor extends FreeMarkerProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            writeClass(MapperRepository.class.getName()+"Impl", MapperRepository.class.getSimpleName()+".ftl", MapperGeneratorProcessor.mappers);
            MapperGeneratorProcessor.mappers.destroy();
        }
        return false;
    }
}
