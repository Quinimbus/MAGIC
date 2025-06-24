package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicExecutableElement;
import com.squareup.javapoet.ClassName;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record RecordInstanceContextActionDefinition(ClassName type, String name, Method method) {

    public static record Method(String name, MagicExecutableElement element) {}

    public static RecordInstanceContextActionDefinition fromExecutableElement(MagicExecutableElement method) {
        return new RecordInstanceContextActionDefinition(
                method.enclosingElement().getClassName(),
                method.findAnnotation(QuiNimbusCommon.ACTION_NAME)
                        .flatMap(a -> a.getElementValue("value").map(String.class::cast))
                        .orElseThrow(),
                new cloud.quinimbus.magic.generator.RecordInstanceContextActionDefinition.Method(
                        method.getSimpleName(), method));
    }

    public static Map<MagicClassElement, Set<RecordInstanceContextActionDefinition>> fromExecutableElements(
            Set<MagicExecutableElement> methods) {
        return methods.stream()
                .filter(e -> e.isAnnotatedWith(QuiNimbusCommon.ACTION_NAME))
                .filter(e -> e.isAnnotatedWith(QuiNimbusCommon.RECORD_ENTITY_INSTANCE_CONTEXT_NAME))
                .collect(Collectors.groupingBy(
                        e -> e.findAnnotation(QuiNimbusCommon.RECORD_ENTITY_INSTANCE_CONTEXT_NAME)
                                .flatMap(a -> a.getElementValue("value").map(MagicClassElement.class::cast))
                                .orElseThrow(),
                        Collectors.mapping(
                                RecordInstanceContextActionDefinition::fromExecutableElement, Collectors.toSet())));
    }
}
