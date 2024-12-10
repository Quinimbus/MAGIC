package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.generator.RepositoryGenerator;
import cloud.quinimbus.magic.generator.RepositoryProducerGenerator;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("cloud.quinimbus.magic.annotations.GenerateRepository")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class GenerateRepositoryProcessor extends MagicClassProcessor {

    @Override
    public void beforeProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {}

    @Override
    public void setup(RoundEnvironment re) {}

    @Override
    public void process(TypeElement annotation, MagicClassElement element) {
        if (annotation.getSimpleName().contentEquals("GenerateRepository")) {
            try {
                var gen = new RepositoryGenerator(element);
                this.writeTypeFile(gen.generateRepositoryType());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    @Override
    public void afterProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {
        var packageName = elements.stream()
                .map(MagicClassElement::getPackageName)
                .reduce(this::commonPackageName)
                .orElseThrow();
        if (annotation.getSimpleName().contentEquals("GenerateRepository")) {
            try {
                var gen = new RepositoryProducerGenerator(elements);
                this.writeTypeFile(packageName, gen.generateRepositoryProducerType());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    public String commonPackageName(String pn1, String pn2) {
        int i = 0;
        var pna1 = pn1.split("\\.");
        var pna2 = pn2.split("\\.");
        var length = Math.min(pna1.length, pna2.length);
        while (i < length && pna1[i].equals(pna2[i])) {
            i++;
        }
        return Arrays.stream(pna2).limit(i).collect(Collectors.joining("."));
    }
}
