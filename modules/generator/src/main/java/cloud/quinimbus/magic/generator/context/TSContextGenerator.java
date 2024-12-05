package cloud.quinimbus.magic.generator.context;

import static cloud.quinimbus.magic.util.Strings.*;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.config.AdminUIConfigLoader;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import cloud.quinimbus.magic.util.Strings;
import io.marioslab.basis.template.TemplateContext;
import java.util.List;
import java.util.stream.Collectors;

public class TSContextGenerator {

    public static record TSAllowedValue(String key, String label) {}

    public static record TSField(
            String nameLC,
            String nameUC,
            String type,
            String label,
            String fieldType,
            boolean owningField,
            boolean hiddenInForm,
            String references,
            String enumName,
            List<TSAllowedValue> allowedValues) {}

    public static TemplateContext createTypeContext(
            AdminUIConfig.Type typeConfig,
            MagicClassElement recordElement,
            MagicClassElement owningType,
            String name,
            boolean weak,
            String ownerField,
            boolean idGenerated,
            String idFieldName) {
        var context = new TemplateContext();
        context.set("keyField", typeConfig.keyField());
        context.set("icon", typeConfig.icon());
        context.set("labelSingular", typeConfig.labelSingular());
        context.set("labelPlural", typeConfig.labelPlural());
        context.set("typeNameLC", uncapitalize(name));
        context.set("typeNameLCPlural", uncapitalize(IDs.toPlural(name)));
        context.set("typeNameUC", capitalize(name));
        context.set("typeNameUCPlural", capitalize(IDs.toPlural(name)));
        context.set("weak", weak);
        context.set("owningType", weak ? uncapitalize(owningType.getSimpleName()) : null);
        context.set(
                "containsBinary",
                recordElement
                        .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                        .findAny()
                        .isPresent());
        context.set(
                "fields",
                recordElement
                        .findFields()
                        .map(e -> new TSField(
                                uncapitalize(e.getSimpleName()),
                                capitalize(e.getSimpleName()),
                                toTSType(
                                        e.typeElement(),
                                        name,
                                        e.typeParameters()
                                                .collect(Collectors.toList())
                                                .toArray(MagicClassElement[]::new)),
                                AdminUIConfigLoader.getFieldConfig(typeConfig, e.getSimpleName())
                                        .label(),
                                toFieldType(e),
                                weak && e.getSimpleName().equals(ownerField),
                                idFieldName.equals(e.getSimpleName()) ? idGenerated : false,
                                e.findAnnotation(QuiNimbusCommon.REFERENCES_ANNOTATION_NAME)
                                        .flatMap(ae -> ae.<MagicClassElement>getElementValue("value")
                                                .map(MagicClassElement::getSimpleName)
                                                .map(Strings::uncapitalize))
                                        .orElse(null),
                                "%s%s"
                                        .formatted(
                                                capitalize(name),
                                                capitalize(e.typeElement().getSimpleName())),
                                allowedValues(e)))
                        .toList());
        context.set(
                "hasBinaryField",
                recordElement
                        .findFields()
                        .map(e -> toFieldType(e))
                        .filter("BINARY"::equals)
                        .findAny()
                        .isPresent());
        context.set(
                "hasBooleanField",
                recordElement
                        .findFields()
                        .map(e -> toFieldType(e))
                        .filter("BOOLEAN"::equals)
                        .findAny()
                        .isPresent());
        context.set(
                "hasReferenceField",
                recordElement
                        .findFields()
                        .filter(e -> e.findAnnotation(QuiNimbusCommon.REFERENCES_ANNOTATION_NAME)
                                .isPresent())
                        .findAny()
                        .isPresent());
        return context;
    }

    private static String toTSType(MagicClassElement classElement, String name, MagicClassElement... typeParameter) {
        return switch (classElement.getQualifiedName()) {
            case "java.lang.String", "java.time.LocalDateTime", "java.time.LocalDate" -> "string";
            case "java.lang.Boolean" -> "Boolean";
            case "java.util.List" -> "(%s)[]".formatted(toTSType(typeParameter[0], name));
            case "cloud.quinimbus.binarystore.persistence.EmbeddableBinary" -> "EmbeddableBinary | File";
            default -> {
                if (classElement.isEnum()) {
                    yield "%s%s".formatted(capitalize(name), capitalize(classElement.getSimpleName()));
                }
                yield "any";
            }
        };
    }

    private static String toFieldType(MagicVariableElement ve) {
        return toFieldType(ve.typeElement(), ve.typeParameters().findAny().orElse(null));
    }

    private static String toFieldType(MagicClassElement type, MagicClassElement parameter) {
        return switch (type.getQualifiedName()) {
            case "java.lang.String" -> "STRING";
            case "java.lang.Integer" -> "NUMBER";
            case "java.lang.Boolean" -> "BOOLEAN";
            case "java.time.LocalDate" -> "LOCALDATE";
            case "java.time.LocalDateTime" -> "LOCALDATETIME";
            case "cloud.quinimbus.binarystore.persistence.EmbeddableBinary" -> "BINARY";
            case "java.util.List" -> "LIST_" + toFieldType(parameter, null);
            default -> {
                if (type.isEnum()) {
                    yield "SELECTION";
                }
                yield "UNKNOWN";
            }
        };
    }

    private static List<TSAllowedValue> allowedValues(MagicVariableElement ve) {
        if (ve.typeElement().isEnum()) {
            return ve.typeElement()
                    .enumValues()
                    .map(v -> new TSAllowedValue(v, v))
                    .toList();
        }
        return List.of();
    }
}
