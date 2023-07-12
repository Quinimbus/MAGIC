package cloud.quinimbus.magic.util;

import io.marioslab.basis.template.TemplateContext;
import io.marioslab.basis.template.TemplateLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TemplateRenderer {
    
    private final Path targetPath;

    public TemplateRenderer(Path targetPath) {
        this.targetPath = targetPath;
    }
    
    public void generateFromTemplate(String path, TemplateContext context) {
        generateFromTemplate(path, path, context);
    }
    
    public void generateFromTemplate(String templatePath, String resultPath, TemplateContext context) {
        try (var outputStream = Files.newOutputStream(targetPath.resolve(resultPath), StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING)) {
            var loader = new TemplateLoader.ClasspathTemplateLoader();
            var template = loader.load("/templates/admin/%s.bt".formatted(templatePath));
            template.render(context, outputStream);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
