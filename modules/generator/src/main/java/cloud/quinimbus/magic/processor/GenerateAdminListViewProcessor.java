package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.classnames.QuiNimbusMagic;
import cloud.quinimbus.magic.config.AdminUIConfig;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicExecutableElement;
import cloud.quinimbus.magic.generator.AdminListViewGenerator;
import cloud.quinimbus.magic.generator.AdminListViewIndexGenerator;
import cloud.quinimbus.magic.generator.AdminListViewTypeGenerator;
import cloud.quinimbus.magic.generator.AdminSkeletonGenerator;
import cloud.quinimbus.magic.generator.RecordContextActionDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import org.apache.commons.lang3.RandomStringUtils;

@SupportedAnnotationTypes({QuiNimbusMagic.GENERATE_ADMIN_LIST_VIEW_NAME, QuiNimbusCommon.ACTION_NAME})
@SupportedSourceVersion(SourceVersion.RELEASE_23)
public class GenerateAdminListViewProcessor extends MagicClassProcessor {

    private AdminUIConfig config;

    private Path srcPath;

    private Map<MagicClassElement, Set<RecordContextActionDefinition>> recordContextActions = new LinkedHashMap<>();

    @Override
    public void setup(RoundEnvironment re) {}

    @Override
    public void beforeProcessAll(
            TypeElement annotation,
            Set<MagicClassElement> typeElements,
            Set<MagicExecutableElement> executableElements) {
        try {
            recordContextActions.putAll(RecordContextActionDefinition.fromExecutableElements(executableElements));
            var fooResource = processingEnv
                    .getFiler()
                    .createResource(
                            StandardLocation.SOURCE_OUTPUT,
                            "",
                            "qnmagic" + RandomStringUtils.insecure().next(10));
            var rootPath = Path.of(fooResource.toUri())
                    .getParent()
                    .getParent()
                    .getParent()
                    .getParent();
            fooResource.delete();
            var configPath = rootPath.resolve("src/main/resources/META-INF/cloud.quinimbus.magic/admin-ui.yml");
            if (Files.exists(configPath)) {
                try (var is = Files.newInputStream(configPath)) {
                    var mapper = new ObjectMapper(new YAMLFactory());
                    config = mapper.readValue(is, AdminUIConfig.class);
                }
            } else {
                config = new AdminUIConfig(null, null, null, null);
            }
            var outPath = rootPath.resolve("target/magic/admin-ui/");
            srcPath = outPath.resolve("src");
            Files.createDirectories(srcPath);
            var skeletonGen = new AdminSkeletonGenerator(outPath, config, this.processingEnv.getOptions(), true);
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
            var globalActions = recordContextActions.getOrDefault(element, Set.of()).stream()
                    .toList();
            var typegen = new AdminListViewTypeGenerator(element, domainPath, config, globalActions);
            typegen.generateType();
            var viewgen = new AdminListViewGenerator(element, viewsPath, config, globalActions);
            viewgen.generateView();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void afterProcessAll(
            TypeElement annotation,
            Set<MagicClassElement> typeElements,
            Set<MagicExecutableElement> executableElements) {
        var domainPath = srcPath.resolve("domain");
        var gen = new AdminListViewIndexGenerator(typeElements, domainPath);
        gen.generateIndex();
    }
}
