package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicExecutableElement;
import com.squareup.javapoet.ClassName;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record RecordContextActionDefinition(ClassName type, String name, Method method) {

    public static record Method(String name) {}

    public static RecordContextActionDefinition fromExecutableElement(MagicExecutableElement method) {
        return new RecordContextActionDefinition(
                method.enclosingElement().getClassName(),
                method.findAnnotation(QuiNimbusCommon.ACTION_NAME)
                        .flatMap(a -> a.getElementValue("value").map(String.class::cast))
                        .orElseThrow(),
                new cloud.quinimbus.magic.generator.RecordContextActionDefinition.Method(method.getSimpleName()));
    }

    public static Map<MagicClassElement, Set<RecordContextActionDefinition>> fromExecutableElements(Set<MagicExecutableElement> methods) {
        return methods.stream()
                .filter(e -> e.isAnnotatedWith(QuiNimbusCommon.ACTION_NAME))
                .collect(Collectors.groupingBy(
                        e -> e.findAnnotation(QuiNimbusCommon.RECORD_TYPE_CONTEXT_NAME)
                                .flatMap(a -> a.getElementValue("value").map(MagicClassElement.class::cast))
                                .orElseThrow(),
                        Collectors.mapping(RecordContextActionDefinition::fromExecutableElement, Collectors.toSet())));
    }
}
