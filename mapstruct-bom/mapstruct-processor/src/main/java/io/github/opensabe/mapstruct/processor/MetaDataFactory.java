package io.github.opensabe.mapstruct.processor;

import com.google.common.collect.Sets;
import org.springframework.util.StringUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import java.util.*;
import java.util.function.BinaryOperator;

/**
 * 根据 javax.lang.model api 生成相应的Mapper元数据
 * @author heng.ma
 */
@SuppressWarnings("unused")
public class MetaDataFactory {


    static Set<? extends AbstractMapper> create (Elements elementUtils, Element bean, List<? extends AnnotationMirror> binding) {
        String packageName = elementUtils.getPackageOf(bean).toString()+".mapper";
        Set<AbstractMapper> result = new HashSet<>();
        BindRelation relations = BindRelation.resolveBinding(elementUtils, binding).stream().reduce(reducer).orElseThrow();
        result.add(relations.createMapMapper(packageName, bean));
        result.addAll(relations.createCommonMapper(packageName, bean));
        return result;
    }


    static BinaryOperator<BindRelation> reducer = (a,b) -> {
        a.values.addAll(b.values);
        return new BindRelation( a.cycle || b.cycle, a.values);
    };


    record BindRelation (boolean cycle, Set<String> values) {

        Set<AbstractMapper> createCommonMapper (String packageName, Element bean) {
            SelfMapper self = new SelfMapper(packageName, bean.toString(), bean.getSimpleName()+"Mapper", cycle);
            Set<AbstractMapper> list = Sets.newHashSet(self);
            if (values.isEmpty()) {
                return list;
            }
            values.remove(bean.toString());
            if (values.isEmpty()) {
                return list;
            }
            list.addAll(values.stream().filter(StringUtils::hasText).map(v -> {
                String name = bean.getSimpleName()+v.substring(v.lastIndexOf(".")+1)+"Mapper";
                return new CommonMapper(packageName, bean.toString(), name, v, cycle);
            }).toList());
            return list;
        }

        MapMapper createMapMapper (String packageName, Element bean) {
            return new MapMapper(packageName, bean.toString(), bean.getSimpleName()+"MapMapper", cycle);
        }



        static List<BindRelation> resolveBinding (Elements elementUtils, List<? extends AnnotationMirror> binding) {
            List<BindRelation> eles = new ArrayList<>();
            for (AnnotationMirror b : binding) {
                boolean cycle = false;
                Set<String> values = new HashSet<>();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e: elementUtils.getElementValuesWithDefaults(b).entrySet()) {
                    switch (e.getKey().getSimpleName().toString()) {
                        case "value":
                            values.addAll(resolveElements(e.getValue().toString()));
                            break;
                        case "cycle":
                            cycle = cycle || (boolean)e.getValue().getValue();
                            break;
                    }
                }
                eles.add(new BindRelation(cycle, values));
            }
            return eles;
        }
    }



    private static List<String> resolveElements (String v) {
        return Arrays.stream(v.replace("{", "")
                        .replace("}","").replace(".class","").split(","))
                .map(String::trim)
                .toList();
    }

}
