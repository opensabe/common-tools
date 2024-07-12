package io.github.opensabe.mapstruct.processor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.github.opensabe.mapstruct.core.MapperRepository;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * 最后将自动生成的Mapper添加到{@link MapperRepository}中
 * @author heng.ma
 */
@SupportedAnnotationTypes("org.mapstruct.Mapper")
public class RepositoryGeneratorProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();
        if (roundEnv.processingOver()) {
            try (Writer writer = filer.createSourceFile(MapperRepository.class.getName()+"Impl")
                    .openWriter()) {
                Configuration cfg = new Configuration(new Version("2.3.32"));
                cfg.setClassForTemplateLoading(MapperGeneratorProcessor.class, "/");
                cfg.setDefaultEncoding("UTF-8");

                Template template = cfg.getTemplate(MapperRepository.class.getSimpleName()+".ftl");
                template.process(MapperGeneratorProcessor.mappers, writer);
                writer.flush();

            }catch (TemplateException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return false;
    }
}
