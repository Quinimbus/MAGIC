package cloud.quinimbus.magic.generator.context;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.config.AdminUIConfigLoader;
import cloud.quinimbus.magic.elements.MagicAnnotationElement;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import cloud.quinimbus.magic.generator.RecordContextActionDefinition;
import cloud.quinimbus.magic.generator.RecordInstanceContextActionDefinition;
import cloud.quinimbus.magic.util.Strings;
import static cloud.quinimbus.magic.util.Strings.*;
import io.marioslab.basis.template.TemplateContext;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    public static record Action(String key, String label, String icon, RequiredRole requiredRole) {}

    public static record RequiredRoles(
            RequiredRole create, RequiredRole read, RequiredRole update, RequiredRole delete) {}

    public static record RequiredRole(boolean anonymous, Set<String> roles) {}

    public static record FieldWithConfig(MagicVariableElement field, AdminUIConfig.Field config) {}
    ;

    public static TemplateContext createTypeContext(
            AdminUIConfig.Type typeConfig,
            MagicClassElement recordElement,
            MagicClassElement owningType,
            String name,
            boolean weak,
            String ownerField,
            boolean idGenerated,
            String idFieldName,
            List<RecordContextActionDefinition> globalActions,
            List<RecordInstanceContextActionDefinition> instanceActions) {
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
        context.set("hasGroup", typeConfig.group() != null);
        context.set("group", typeConfig.group());
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
                        .map(f -> new FieldWithConfig(
                                f, AdminUIConfigLoader.getFieldConfig(typeConfig, f.getSimpleName())))
                        .sorted(Comparator.comparing(f -> f.config().orderKey()))
                        .map(f -> generateTSField(f, name, weak, ownerField, idGenerated, idFieldName))
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
        context.set(
                "globalActions",
                globalActions.stream()
                        .map(a -> {
                            var config = AdminUIConfigLoader.getGlobalActionConfig(typeConfig, a.name());
                            return new Action(
                                    a.name(),
                                    config.label(),
                                    config.icon(),
                                    requiredRole(
                                            "call",
                                            a.method()
                                                    .element()
                                                    .findAnnotation(QuiNimbusCommon.ACTION_ROLES_ALLOWED_NAME)));
                        })
                        .toList());
        context.set(
                "instanceActions",
                instanceActions.stream()
                        .map(a -> {
                            var config = AdminUIConfigLoader.getInstanceActionConfig(typeConfig, a.name());
                            return new Action(
                                    a.name(),
                                    config.label(),
                                    config.icon(),
                                    requiredRole(
                                            "call",
                                            a.method()
                                                    .element()
                                                    .findAnnotation(QuiNimbusCommon.ACTION_ROLES_ALLOWED_NAME)));
                        })
                        .toList());
        context.set("requiredRoles", requiredRoles(recordElement));
        return context;
    }

    private static TSField generateTSField(
            FieldWithConfig f,
            String typeName,
            boolean weak,
            String ownerField,
            boolean idGenerated,
            String idFieldName) {
        var e = f.field();
        return new TSField(
                uncapitalize(e.getSimpleName()),
                capitalize(e.getSimpleName()),
                toTSType(
                        e.typeElement(),
                        typeName,
                        e.typeParameters().collect(Collectors.toList()).toArray(MagicClassElement[]::new)),
                f.config().label(),
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
                                capitalize(typeName),
                                capitalize(
                                        e.typeElement().isEnum()
                                                ? e.typeElement().getSimpleName()
                                                : e.typeParameters()
                                                        .findAny()
                                                        .map(MagicClassElement::getSimpleName)
                                                        .orElse("<MissingTypeParameter>"))),
                allowedValues(e, f.config()));
    }

    private static String toTSType(MagicClassElement classElement, String name, MagicClassElement... typeParameter) {
        return switch (classElement.getQualifiedName()) {
            case "java.lang.String", "java.time.LocalDateTime", "java.time.LocalDate" -> "string";
            case "java.lang.Boolean" -> "Boolean";
            case "java.util.List" -> "(%s)[]".formatted(toTSType(typeParameter[0], name));
            case "java.util.Set" -> "(%s)[]".formatted(toTSType(typeParameter[0], name));
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
            case "java.util.Set" -> "SET_" + toFieldType(parameter, null);
            default -> {
                if (type.isEnum()) {
                    yield "SELECTION";
                }
                yield "UNKNOWN";
            }
        };
    }

    private static List<TSAllowedValue> allowedValues(MagicVariableElement ve, AdminUIConfig.Field fieldConfig) {
        if (ve.typeElement().isEnum()) {
            return ve.typeElement()
                    .enumValues()
                    .map(v -> new TSAllowedValue(
                            v,
                            Optional.ofNullable(fieldConfig.allowedValues().get(v))
                                    .map(AdminUIConfig.AllowedValue::label)
                                    .orElse(v)))
                    .toList();
        } else if (ve.typeParameters().findAny().map(MagicClassElement::isEnum).orElse(false)) {
            return ve.typeParameters()
                    .findAny()
                    .orElseThrow()
                    .enumValues()
                    .map(v -> new TSAllowedValue(
                            v,
                            Optional.ofNullable(fieldConfig.allowedValues().get(v))
                                    .map(AdminUIConfig.AllowedValue::label)
                                    .orElse(v)))
                    .toList();
        }
        return List.of();
    }

    private static RequiredRoles requiredRoles(MagicClassElement recordElement) {
        var crudRolesAllowedAnno = recordElement.findAnnotation(QuiNimbusCommon.CRUD_ROLES_ALLOWED_NAME);
        return new RequiredRoles(
                crudRolesAllowedAnno.map(anno -> requiredRole("create", anno)).orElse(new RequiredRole(true, Set.of())),
                crudRolesAllowedAnno.map(anno -> requiredRole("read", anno)).orElse(new RequiredRole(true, Set.of())),
                crudRolesAllowedAnno.map(anno -> requiredRole("update", anno)).orElse(new RequiredRole(true, Set.of())),
                crudRolesAllowedAnno
                        .map(anno -> requiredRole("delete", anno))
                        .orElse(new RequiredRole(true, Set.of())));
    }

    private static RequiredRole requiredRole(String annotationElement, MagicAnnotationElement anno) {
        var permissionAnno =
                anno.<MagicAnnotationElement>getElementValue(annotationElement).orElseThrow();
        var permissionType = permissionAnno
                .<MagicVariableElement>getElementValue("value")
                .map(MagicVariableElement::getSimpleName)
                .orElseThrow();
        return switch (permissionType) {
            case "ANONYMOUS" -> new RequiredRole(true, Set.of());
            case "AUTHENTICATED" -> new RequiredRole(false, Set.of());
            case "ROLES" -> new RequiredRole(
                    false,
                    permissionAnno
                            .<List<String>>getElementValue("roles")
                            .map(Set::copyOf)
                            .orElseThrow());
            default -> throw new IllegalArgumentException(
                    "Unknown permission type %s, did you mix different versions of Quinimbus dependencies?"
                            .formatted(permissionType));
        };
    }

    private static RequiredRole requiredRole(String annotationElement, Optional<MagicAnnotationElement> anno) {
        return anno.map(a -> requiredRole(annotationElement, a)).orElse(new RequiredRole(true, Set.of()));
    }
}
