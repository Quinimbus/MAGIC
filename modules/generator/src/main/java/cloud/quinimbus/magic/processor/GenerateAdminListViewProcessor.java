package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.generator.AdminListViewGenerator;
import cloud.quinimbus.magic.generator.AdminListViewIndexGenerator;
import cloud.quinimbus.magic.generator.AdminListViewTypeGenerator;
import cloud.quinimbus.magic.generator.AdminSkeletonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("cloud.quinimbus.magic.annotations.GenerateAdminListView")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class GenerateAdminListViewProcessor extends MagicClassProcessor {

    private AdminUIConfig config;

    private Path srcPath;

    @Override
    public void setup(RoundEnvironment re) {}

    @Override
    public void beforeProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {
        try {
            var rootPath = Path.of(processingEnv
                            .getFiler()
                            .createResource(StandardLocation.SOURCE_OUTPUT, "", "foo")
                            .toUri())
                    .getParent()
                    .getParent()
                    .getParent()
                    .getParent();
            var configPath = rootPath.resolve("src/main/resources/META-INF/cloud.quinimbus.magic/admin-ui.yml");
            if (Files.exists(configPath)) {
                try (var is = Files.newInputStream(configPath)) {
                    var mapper = new ObjectMapper(new YAMLFactory());
                    config = mapper.readValue(is, AdminUIConfig.class);
                }
            } else {
                config = new AdminUIConfig(null, null, null);
            }
            var outPath = rootPath.resolve("target/magic/admin-ui/");
            srcPath = outPath.resolve("src");
            Files.createDirectories(srcPath);
            var skeletonGen = new AdminSkeletonGenerator(outPath, config, this.processingEnv.getOptions());
            skeletonGen.generateSkeleton();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void process(TypeElement annotation, MagicClassElement element) {
        try {
            var domainPath = srcPath.resolve("domain");
            Files.createDirectories(domainPath);
            var viewsPath = srcPath.resolve("views");
            Files.createDirectories(viewsPath);
            var typegen = new AdminListViewTypeGenerator(element, domainPath, config);
            typegen.generateType();
            var viewgen = new AdminListViewGenerator(element, viewsPath, config);
            viewgen.generateView();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void afterProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {
        var domainPath = srcPath.resolve("domain");
        var gen = new AdminListViewIndexGenerator(elements, domainPath);
        gen.generateIndex();
    }
}
