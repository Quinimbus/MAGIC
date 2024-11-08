package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.util.TemplateRenderer;
import io.marioslab.basis.template.TemplateContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class AdminSkeletonGenerator {

    private final Path adminUiPath;
    private final AdminUIConfig config;
    private final TemplateRenderer templateRenderer;
    private final Map<String, String> processorOptions;

    public AdminSkeletonGenerator(Path adminUiPath, AdminUIConfig config, Map<String, String> processorOptions) {
        this.adminUiPath = adminUiPath;
        this.config = config;
        this.templateRenderer = new TemplateRenderer(adminUiPath);
        this.processorOptions = processorOptions;
    }

    public void generateSkeleton() {
        try {
            var adminUiDependencyLocal =
                    config.dependencies().adminUi().version().startsWith("file:");
            generateDockerfile();
            generatePackageJson(adminUiDependencyLocal);
            generateIndexHtml();
            generateInitialState();
            Files.createDirectories(adminUiPath.resolve("docker"));
            Files.createDirectories(adminUiPath.resolve("src/assets"));
            Files.createDirectories(adminUiPath.resolve("src/router"));
            copyFile("docker/nginx.default.conf");
            copyFile(".npmrc");
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
            if (adminUiDependencyLocal) {
                var source = Paths.get(config.dependencies().adminUi().version().substring(5));
                var target = adminUiPath.resolve("quinimbus-admin-ui-local.tgz");
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void generatePackageJson(boolean additionalDockerVariant) {
        var context = new TemplateContext();
        context.set("appname", config.app().name());
        context.set("appversion", config.app().version());
        var adminUiDependencyVersion = this.processorOptions.getOrDefault(
                "qn.magic.admin-ui.dependencies.adminUi.version",
                config.dependencies().adminUi().version());
        context.set("adminuidependency", adminUiDependencyVersion);
        templateRenderer.generateFromTemplate("package.json", context);
        if (additionalDockerVariant) {
            context.set("adminuidependency", "file:/tmp/quinimbus-admin-ui-local.tgz");
            templateRenderer.generateFromTemplate("package.json", "package-docker.json", context);
        }
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

    private void generateDockerfile() {
        var context = new TemplateContext();
        context.set("nginxVersion", config.dependencies().nginx().version());
        context.set("nodeVersion", config.dependencies().node().version());
        context.set("adminUiLocal", config.dependencies().adminUi().version().startsWith("file:"));
        templateRenderer.generateFromTemplate("Dockerfile", context);
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
