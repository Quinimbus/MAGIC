package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.generator.EntityAllRestResourceGenerator;
import cloud.quinimbus.magic.generator.EntitySingleRestResourceGenerator;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("cloud.quinimbus.magic.annotations.GenerateRestEndpoints")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class GenerateRestEndpointsProcessor extends MagicClassProcessor {

    private Map<MagicClassElement, List<MagicClassElement>> entityChildren = new LinkedHashMap<>();

    @Override
    public void beforeProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {
        entityChildren = elements.stream()
                .filter(e -> e.isAnnotatedWith(QuiNimbusCommon.OWNER_ANNOTATION_NAME))
                .collect(Collectors.groupingBy(e -> e.findAnnotation(QuiNimbusCommon.OWNER_ANNOTATION_NAME)
                        .flatMap(a -> a.getElementValue("owningEntity"))
                        .map(MagicClassElement.class::cast)
                        .orElseThrow()));
    }

    @Override
    public void process(TypeElement annotation, MagicClassElement element) {
        if (annotation.getSimpleName().contentEquals("GenerateRestEndpoints")) {
            try {
                var singleGen = new EntitySingleRestResourceGenerator(element, entityChildren.get(element));
                this.writeTypeFile(singleGen.generateSingleResource());
                var allGen = new EntityAllRestResourceGenerator(element);
                this.writeTypeFile(allGen.generateAllResource());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    @Override
    public void afterProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {}
}
