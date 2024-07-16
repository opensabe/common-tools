package io.github.opensabe.mapstruct.processor;

import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.mapstruct.core.MapperRegister;
import org.mapstruct.Mapper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("io.github.opensabe.mapstruct.core.RegisterRepository")
public class CustomerRegisterProcessor extends FreeMarkerProcessor {

    private final List<CustomerMapper> mappers = new ArrayList<>();

    private Types typeUtils;

    private Elements elementUtils;

    private TypeMirror commonMirror;
    private TypeMirror mapMirror;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        commonMirror = typeUtils.erasure(elementUtils.getTypeElement(CommonCopyMapper.class.getName()).asType());
        mapMirror = typeUtils.erasure(elementUtils.getTypeElement(FromMapMapper.class.getName()).asType());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (!roundEnv.processingOver()) {
            for (TypeElement annotation : annotations) {
                Set<? extends Element> mappers = roundEnv.getElementsAnnotatedWith(annotation);
                for (Element mapper : mappers) {
                    if (hasMapperAnnotation(mapper)) {
                        if (mapper instanceof TypeElement typeElement) {
                            typeElement.getInterfaces().stream()
                                    .filter(this::isSubtype)
                                    .filter(m -> m instanceof DeclaredType)
                                    .findFirst()
                                    .ifPresent(m -> {
                                        List<? extends TypeMirror> arguments = ((DeclaredType) m).getTypeArguments();
                                        if (arguments.size() == 2) {
                                            this.mappers.add(new CustomerMapper(mapper.toString(), arguments.get(0).toString(), arguments.get(1).toString()));
                                        }else {
                                            this.mappers.add(new CustomerMapper(mapper.toString(),null, arguments.get(0).toString()));
                                        }
                                    });
                        }
                    }
                }
            }
        }else {
            Element interfaceName = elementUtils.getTypeElement(MapperRegister.class.getName());
            String className = interfaceName.toString() + "Impl";
            writeClass(className, MapperRegister.class.getSimpleName()+".ftl", Map.of("mappers", this.mappers));
        }
        return false;
    }

    private boolean isSubtype (TypeMirror type) {
        return typeUtils.isSubtype(type, mapMirror) || typeUtils.isSubtype(type, commonMirror);
    }

    private boolean hasMapperAnnotation (Element mapper) {
        return elementUtils.getAllAnnotationMirrors(mapper)
                .stream()
                .anyMatch(a -> typeUtils.isSameType(elementUtils.getTypeElement(Mapper.class.getName()).asType(), a.getAnnotationType()));
    }
}
