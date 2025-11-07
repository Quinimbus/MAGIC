package cloud.quinimbus.magic.generator;

import static cloud.quinimbus.magic.util.Strings.*;

import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.util.TemplateRenderer;
import io.marioslab.basis.template.TemplateContext;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class AdminListViewIndexGenerator {

    private final List<MagicClassElement> recordElements;
    private final AdminUIConfig config;
    private final TemplateRenderer templateRenderer;

    public static record Type(String nameLC, String nameUC) {}

    public static record Group(String key, String label) {}

    public AdminListViewIndexGenerator(List<MagicClassElement> recordElements, Path domainPath, AdminUIConfig config) {
        this.recordElements = recordElements;
        this.config = config;
        this.templateRenderer = new TemplateRenderer(domainPath);
    }

    public void generateIndex() {
        var context = new TemplateContext();
        context.set(
                "types",
                this.recordElements.stream()
                        .map(e -> new Type(uncapitalize(e.getSimpleName()), capitalize(e.getSimpleName())))
                        .toList());
        context.set(
                "groups",
                config.menugroups().entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getValue().orderKey()))
                        .map(e -> new Group(e.getKey(), e.getValue().label()))
                        .toList());
        this.templateRenderer.generateFromTemplate("src/domain/index.ts", "index.ts", context);
    }
}
