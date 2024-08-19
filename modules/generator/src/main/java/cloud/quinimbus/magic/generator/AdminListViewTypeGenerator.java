package cloud.quinimbus.magic.generator;

import static cloud.quinimbus.magic.util.Strings.*;

import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.config.AdminUIConfigLoader;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.generator.context.TSContextGenerator;
import cloud.quinimbus.magic.util.TemplateRenderer;
import java.nio.file.Path;

public class AdminListViewTypeGenerator extends RecordEntityBasedGenerator {

    private final AdminUIConfig config;
    private final TemplateRenderer templateRenderer;

    public AdminListViewTypeGenerator(MagicClassElement recordElement, Path domainPath, AdminUIConfig config) {
        super(recordElement);
        this.config = config;
        this.templateRenderer = new TemplateRenderer(domainPath);
    }

    public void generateType() {
        var typeConfig = AdminUIConfigLoader.getTypeConfig(this.config, this.name, this.idFieldName);
        var context = TSContextGenerator.createTypeContext(
                typeConfig, recordElement, owningType, name, weak(), ownerField(), idGenerated, idFieldName);
        this.templateRenderer.generateFromTemplate("src/domain/type.ts", "%s.ts".formatted(capitalize(name)), context);
    }
}
