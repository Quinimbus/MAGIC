package cloud.quinimbus.magic.generator;

import static cloud.quinimbus.magic.util.Strings.*;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.util.TemplateRenderer;
import io.marioslab.basis.template.TemplateContext;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

public class AdminListViewTypeGenerator extends RecordEntityBasedGenerator {

    private final AdminUIConfig config;
    private final TemplateRenderer templateRenderer;

    public static record TSField(
            String name, String type, String label, String fieldType, boolean owningField, boolean hiddenInForm) {}

    public AdminListViewTypeGenerator(MagicClassElement recordElement, Path domainPath, AdminUIConfig config) {
        super(recordElement);
        this.config = config;
        this.templateRenderer = new TemplateRenderer(domainPath);
    }

    public void generateType() {
        var typeConfig = this.getTypeConfig();
        var context = new TemplateContext();
        context.set("keyField", typeConfig.keyField());
        context.set("icon", typeConfig.icon());
        context.set("labelSingular", typeConfig.labelSingular());
        context.set("labelPlural", typeConfig.labelPlural());
        context.set("typeNameLC", uncapitalize(name));
        context.set("typeNameLCPlural", uncapitalize(IDs.toPlural(name)));
        context.set("typeNameUC", capitalize(name));
        context.set("typeNameUCPlural", capitalize(IDs.toPlural(name)));
        context.set("weak", weak());
        context.set("owningType", weak() ? uncapitalize(owningType.getSimpleName()) : null);
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
                                e.getSimpleName(),
                                toTSType(e.getElement().asType().toString()),
                                getFieldConfig(e.getSimpleName()).label(),
                                toFieldType(e.getElement().asType().toString()),
                                weak() && e.getSimpleName().equals(ownerField()),
                                idFieldName.equals(e.getSimpleName()) ? idGenerated : false))
                        .toList());
        this.templateRenderer.generateFromTemplate("src/domain/type.ts", "%s.ts".formatted(capitalize(name)), context);
    }

    private record ParsedType(String type, String genericParam) {}

    private ParsedType parseType(String type) {
        var pattern = Pattern.compile("([\\w\\.]+)(?:<([\\w\\.]+)>)?");
        var matcher = pattern.matcher(type);
        if (!matcher.matches()) {
            throw new IllegalStateException("invalid type: " + type);
        }
        return new ParsedType(matcher.group(1), matcher.group(2));
    }

    private String toTSType(String type) {
        var parsedType = parseType(type);
        return switch (parsedType.type()) {
            case "java.lang.String", "java.time.LocalDateTime", "java.time.LocalDate" -> "string";
            case "java.util.List" -> "%s[]".formatted(toTSType(parsedType.genericParam));
            case "cloud.quinimbus.binarystore.persistence.EmbeddableBinary" -> "EmbeddableBinary | File";
            default -> "any";
        };
    }

    private String toFieldType(String type) {
        var parsedType = parseType(type);
        return switch (parsedType.type()) {
            case "java.lang.String" -> "STRING";
            case "java.lang.Integer" -> "NUMBER";
            case "java.time.LocalDate" -> "LOCALDATE";
            case "java.time.LocalDateTime" -> "LOCALDATETIME";
            case "cloud.quinimbus.binarystore.persistence.EmbeddableBinary" -> "BINARY";
            default -> "UNKNOWN";
        };
    }

    private AdminUIConfig.Type getTypeConfig() {
        var defaultConfig = new cloud.quinimbus.magic.config.AdminUIConfig.Type(
                "database", capitalize(name), capitalize(IDs.toPlural(name)), this.idFieldName, Map.of());
        var providedConfig = this.config.types().get(uncapitalize(name));
        if (providedConfig == null) {
            return defaultConfig;
        }
        return new cloud.quinimbus.magic.config.AdminUIConfig.Type(
                providedConfig.icon() == null ? defaultConfig.icon() : providedConfig.icon(),
                providedConfig.labelSingular() == null ? defaultConfig.labelSingular() : providedConfig.labelSingular(),
                providedConfig.labelPlural() == null ? defaultConfig.labelPlural() : providedConfig.labelPlural(),
                providedConfig.keyField() == null ? defaultConfig.keyField() : providedConfig.keyField(),
                providedConfig.fields() == null ? defaultConfig.fields() : providedConfig.fields());
    }

    private AdminUIConfig.Field getFieldConfig(String field) {
        return getTypeConfig()
                .fields()
                .getOrDefault(field, new cloud.quinimbus.magic.config.AdminUIConfig.Field(capitalize(field)));
    }
}
