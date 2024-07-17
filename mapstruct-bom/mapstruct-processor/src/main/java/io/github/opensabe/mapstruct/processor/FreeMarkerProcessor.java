package io.github.opensabe.mapstruct.processor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;

/**
 * processor for generate class by freemarker
 * @author heng.ma
 */
public abstract class FreeMarkerProcessor extends AbstractProcessor {

    private final Configuration configuration;
    private Filer filer;


    protected FreeMarkerProcessor() {
        Configuration cfg = new Configuration(new Version("2.3.32"));
        cfg.setClassForTemplateLoading(this.getClass(), "/");
        cfg.setDefaultEncoding("UTF-8");
        this.configuration = cfg;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }

    protected void writeClass (String className, String template, Object attributes) {
        try (Writer writer = filer.createSourceFile(className).openWriter()) {
            Template t = configuration.getTemplate(template);
            t.process(attributes, writer);
            writer.flush();

        }catch (TemplateException | IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
