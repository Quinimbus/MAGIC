package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.elements.MagicClassElement;
import static cloud.quinimbus.magic.util.Strings.*;
import cloud.quinimbus.magic.util.TemplateRenderer;
import io.marioslab.basis.template.TemplateContext;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

public class AdminListViewTypeGenerator extends RecordEntityBasedGenerator {
    
    private final AdminUIConfig config;
    private final TemplateRenderer templateRenderer;
    
    public static record TSField(String name, String type, String label, String fieldType) {
        
    }
    
    public AdminListViewTypeGenerator(MagicClassElement recordElement, Path domainPath, AdminUIConfig config) {
        super(recordElement);
        this.config = config;
        this.templateRenderer = new TemplateRenderer(domainPath);
    }

    public void generateType() {
        var typeConfig = this.getTypeConfig();
        var context = new TemplateContext();
        context.set("icon", typeConfig.icon());
        context.set("labelSingular", typeConfig.labelSingular());
        context.set("labelPlural", typeConfig.labelPlural());
        context.set("typeNameLC", uncapitalize(name));
        context.set("typeNameLCPlural", uncapitalize(IDs.toPlural(name)));
        context.set("typeNameUC", capitalize(name));
        context.set("typeNameUCPlural", capitalize(IDs.toPlural(name)));
        context.set("fields", recordElement.findFields()
                .map(e -> new TSField(
                        e.getSimpleName(),
                        toTSType(e.getElement().asType().toString()),
                        getFieldConfig(e.getSimpleName()).label(),
                        toFieldType(e.getElement().asType().toString())))
                .toList());
        this.templateRenderer.generateFromTemplate(
                "src/domain/type.ts",
                "%s.ts".formatted(capitalize(name)),
                context);
    }
    
    private record ParsedType(String type, String genericParam) {
        
    }
    
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
            default -> "any";
        };
    }
    
    private String toFieldType(String type) {
        var parsedType = parseType(type);
        return switch (parsedType.type()) {
            case "java.lang.String" -> "STRING";
            case "java.lang.Integer" -> "NUMBER";
            default -> "UNKNOWN";
        };
    }
    
    private AdminUIConfig.Type getTypeConfig() {
        return this.config.types().getOrDefault(
                uncapitalize(name),
                new cloud.quinimbus.magic.config.AdminUIConfig.Type(
                        null,
                        capitalize(name),
                        capitalize(IDs.toPlural(name)),
                        Map.of()));
    }
    
    private AdminUIConfig.Field getFieldConfig(String field) {
        return getTypeConfig().fields().getOrDefault(
                field,
                new cloud.quinimbus.magic.config.AdminUIConfig.Field(capitalize(field)));
    }
}
