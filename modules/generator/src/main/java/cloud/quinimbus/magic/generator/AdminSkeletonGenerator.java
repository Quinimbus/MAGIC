package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.util.TemplateRenderer;
import io.marioslab.basis.template.TemplateContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AdminSkeletonGenerator {
    
    private final Path adminUiPath;
    private final AdminUIConfig config;
    private final TemplateRenderer templateRenderer;

    public AdminSkeletonGenerator(Path adminUiPath, AdminUIConfig config) {
        this.adminUiPath = adminUiPath;
        this.config = config;
        this.templateRenderer = new TemplateRenderer(adminUiPath);
    }
    
    public void generateSkeleton() {
        try {
            generatePackageJson();
            generateIndexHtml();
            generateAppVue();
            Files.createDirectories(adminUiPath.resolve("src/assets"));
            Files.createDirectories(adminUiPath.resolve("src/qn/components/dialog"));
            Files.createDirectories(adminUiPath.resolve("src/qn/components/form"));
            Files.createDirectories(adminUiPath.resolve("src/qn/components/view"));
            Files.createDirectories(adminUiPath.resolve("src/qn/datasource"));
            Files.createDirectories(adminUiPath.resolve("src/qn/types"));
            Files.createDirectories(adminUiPath.resolve("src/qn/ui"));
            Files.createDirectories(adminUiPath.resolve("src/router"));
            copyFile("env.d.ts");
            copyFile("tsconfig.json");
            copyFile("tsconfig.app.json");
            copyFile("tsconfig.node.json");
            copyFile("tsconfig.vitest.json");
            copyFile("vite.config.ts");
            copyFile("src/assets/main.css");
            copyFile("src/main.ts");
            copyFile("src/qn/components/dialog/EntityAddDialog.vue");
            copyFile("src/qn/components/dialog/EntityEditDialog.vue");
            copyFile("src/qn/components/dialog/index.ts");
            copyFile("src/qn/components/form/EntityForm.vue");
            copyFile("src/qn/components/form/DateField.vue");
            copyFile("src/qn/components/form/DateTimeField.vue");
            copyFile("src/qn/components/form/NumberField.vue");
            copyFile("src/qn/components/form/StringField.vue");
            copyFile("src/qn/components/form/index.ts");
            copyFile("src/qn/components/view/EntityView.vue");
            copyFile("src/qn/components/view/EntityViewDataTable.vue");
            copyFile("src/qn/components/view/EntityViewToolbar.vue");
            copyFile("src/qn/components/view/index.ts");
            copyFile("src/qn/datasource/EntityListDataSource.ts");
            copyFile("src/qn/types/entities.ts");
            copyFile("src/qn/ui/UI.ts");
            copyFile("src/router/index.ts");
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private void generatePackageJson() {
        var context = new TemplateContext();
        context.set("appname", config.app().name());
        context.set("appversion", config.app().version());
        templateRenderer.generateFromTemplate("package.json", context);
    }
    
    private void generateIndexHtml() {
        var context = new TemplateContext();
        context.set("apptitle", config.app().name());
        templateRenderer.generateFromTemplate("index.html", context);
    }
    
    private void generateAppVue() {
        var context = new TemplateContext();
        context.set("apptitle", config.app().name());
        context.set("appversion", config.app().version());
        templateRenderer.generateFromTemplate("src/App.vue", context);
    }
    
    private void copyFile(String path) {
        try (var outputStream = Files.newOutputStream(adminUiPath.resolve(path));
            var inputStream = getClass().getResourceAsStream("/static/admin/%s".formatted(path))) {
            inputStream.transferTo(outputStream);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
