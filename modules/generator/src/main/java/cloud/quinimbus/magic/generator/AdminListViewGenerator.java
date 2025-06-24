package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.config.AdminUIConfigLoader;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.generator.context.TSContextGenerator;
import static cloud.quinimbus.magic.util.Strings.*;
import cloud.quinimbus.magic.util.TemplateRenderer;
import java.nio.file.Path;
import java.util.List;

public class AdminListViewGenerator extends RecordEntityBasedGenerator {

    private final AdminUIConfig config;
    private final TemplateRenderer templateRenderer;
    private final List<RecordContextActionDefinition> globalActions;
    private final List<RecordInstanceContextActionDefinition> instanceActions;

    public AdminListViewGenerator(
            MagicClassElement recordElement,
            Path viewsPath,
            AdminUIConfig config,
            List<RecordContextActionDefinition> globalActions,
            List<RecordInstanceContextActionDefinition> instanceActions) {
        super(recordElement);
        this.config = config;
        this.templateRenderer = new TemplateRenderer(viewsPath);
        this.globalActions = globalActions;
        this.instanceActions = instanceActions;
    }

    public void generateView() {
        var typeConfig = AdminUIConfigLoader.getTypeConfig(this.config, this.name, this.idFieldName);
        var context = TSContextGenerator.createTypeContext(
                typeConfig,
                recordElement,
                owningType,
                name,
                weak(),
                ownerField(),
                idGenerated,
                idFieldName,
                globalActions,
                instanceActions);
        this.templateRenderer.generateFromTemplate(
                "src/views/ListView.vue", "%sListView.vue".formatted(capitalize(IDs.toPlural(name))), context);
    }
}
