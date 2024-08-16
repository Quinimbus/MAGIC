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
            generateInitialState();
            Files.createDirectories(adminUiPath.resolve("src/assets"));
            Files.createDirectories(adminUiPath.resolve("src/router"));
            copyFile("env.d.ts");
            copyFile("tsconfig.json");
            copyFile("tsconfig.app.json");
            copyFile("tsconfig.node.json");
            copyFile("tsconfig.vitest.json");
            copyFile("vite.config.ts");
            copyFile("src/assets/main.css");
            copyFile("src/App.vue");
            copyFile("src/main.ts");
            copyFile("src/router/index.ts");
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void generatePackageJson() {
        var context = new TemplateContext();
        context.set("appname", config.app().name());
        context.set("appversion", config.app().version());
        context.set("adminuidependency", config.adminUiDependency().version());
        templateRenderer.generateFromTemplate("package.json", context);
    }

    private void generateIndexHtml() {
        var context = new TemplateContext();
        context.set("apptitle", config.app().name());
        templateRenderer.generateFromTemplate("index.html", context);
    }

    private void generateInitialState() {
        var context = new TemplateContext();
        context.set("apptitle", config.app().name());
        context.set("appversion", config.app().version());
        templateRenderer.generateFromTemplate("src/initialState.ts", context);
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
