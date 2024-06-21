package cloud.quinimbus.magic.generator;

import static cloud.quinimbus.magic.util.Strings.*;

import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.util.TemplateRenderer;
import io.marioslab.basis.template.TemplateContext;
import java.nio.file.Path;
import java.util.Set;

public class AdminListViewIndexGenerator {

    private final Set<MagicClassElement> recordElements;

    private final TemplateRenderer templateRenderer;

    public static record Type(String nameLC, String nameUC) {}

    public AdminListViewIndexGenerator(Set<MagicClassElement> recordElements, Path domainPath) {
        this.recordElements = recordElements;
        this.templateRenderer = new TemplateRenderer(domainPath);
    }

    public void generateIndex() {
        var context = new TemplateContext();
        context.set(
                "types",
                this.recordElements.stream()
                        .map(e -> new Type(uncapitalize(e.getSimpleName()), capitalize(e.getSimpleName())))
                        .toList());
        this.templateRenderer.generateFromTemplate("src/domain/index.ts", "index.ts", context);
    }
}
