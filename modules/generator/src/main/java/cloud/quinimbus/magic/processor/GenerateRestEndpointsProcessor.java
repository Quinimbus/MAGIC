package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.generator.EntityRestResourceGenerator;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("cloud.quinimbus.magic.annotations.GenerateRestEndpoints")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class GenerateRestEndpointsProcessor extends MagicClassProcessor {

    public void process(TypeElement annotation, MagicClassElement element) {
        if (annotation.getSimpleName().contentEquals("GenerateRestEndpoints")) {
            try {
                var gen = new EntityRestResourceGenerator(element);
                this.writeTypeFile(gen.generateSingleResource());
                this.writeTypeFile(gen.generateAllResource());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    @Override
    public void afterProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {
        
    }
}
