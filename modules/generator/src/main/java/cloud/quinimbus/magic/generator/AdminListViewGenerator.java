package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.magic.elements.MagicClassElement;
import static cloud.quinimbus.magic.util.Strings.*;
import cloud.quinimbus.magic.util.TemplateRenderer;
import io.marioslab.basis.template.TemplateContext;
import java.nio.file.Path;

public class AdminListViewGenerator extends RecordEntityBasedGenerator {

    private final TemplateRenderer templateRenderer;

    public AdminListViewGenerator(MagicClassElement recordElement, Path viewsPath) {
        super(recordElement);
        this.templateRenderer = new TemplateRenderer(viewsPath);
    }

    public void generateView() {
        var context = new TemplateContext();
        context.set("typeNameLC", uncapitalize(name));
        context.set("typeNameUC", capitalize(name));
        this.templateRenderer.generateFromTemplate(
                "src/views/ListView.vue",
                "%sListView.vue".formatted(capitalize(IDs.toPlural(name))),
                context);
    }
}
