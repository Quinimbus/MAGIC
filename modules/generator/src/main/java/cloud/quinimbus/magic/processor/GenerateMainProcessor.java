package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicExecutableElement;
import cloud.quinimbus.magic.generator.MainClassGenerator;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_24)
public class GenerateMainProcessor extends MagicClassProcessor {

    @Override
    public void setup(RoundEnvironment re) {
        if (re.processingOver()) {
            var basePackage = processingEnv.getOptions().get("magic.basepackage");
            if (basePackage == null) {
                throw new IllegalStateException("-Amagic.basepackage is not configured");
            }
            var gen = new MainClassGenerator(basePackage);
            try {
                this.writeTypeFile(gen.generate());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    @Override
    public void beforeProcessAll(
            TypeElement annotation,
            Set<MagicClassElement> typeElements,
            Set<MagicExecutableElement> executableElements) {}

    @Override
    public void process(TypeElement annotation, MagicClassElement element) {}

    @Override
    public void afterProcessAll(
            TypeElement annotation,
            Set<MagicClassElement> typeElements,
            Set<MagicExecutableElement> executableElements) {}
}
