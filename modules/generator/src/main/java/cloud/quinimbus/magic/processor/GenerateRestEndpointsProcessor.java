package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.classnames.QuiNimbusMagic;
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

@SupportedAnnotationTypes({QuiNimbusMagic.GENERATE_REST_ENDPOINTS, QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class GenerateRestEndpointsProcessor extends MagicClassProcessor {

    private Map<MagicClassElement, List<MagicClassElement>> entityChildren = new LinkedHashMap<>();

    private Map<MagicClassElement, List<MagicClassElement>> entityMappers = new LinkedHashMap<>();

    @Override
    public void beforeProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {
        entityChildren.putAll(elements.stream()
                .filter(e -> e.isAnnotatedWith(QuiNimbusMagic.GENERATE_REST_ENDPOINTS))
                .filter(e -> e.isAnnotatedWith(QuiNimbusCommon.OWNER_ANNOTATION_NAME))
                .collect(Collectors.groupingBy(e -> e.findAnnotation(QuiNimbusCommon.OWNER_ANNOTATION_NAME)
                        .flatMap(a -> a.getElementValue("owningEntity"))
                        .map(MagicClassElement.class::cast)
                        .orElseThrow())));
        entityMappers.putAll(elements.stream()
                .filter(e -> e.isAnnotatedWith(QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT))
                .collect(Collectors.groupingBy(
                        e -> e.findAnnotation(QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT)
                                .flatMap(a -> a.getElementValue("forEntity"))
                                .map(MagicClassElement.class::cast)
                                .orElseThrow())));
    }

    @Override
    public void process(TypeElement annotation, MagicClassElement element) {
        if (annotation.getQualifiedName().contentEquals(QuiNimbusMagic.GENERATE_REST_ENDPOINTS)) {
            try {
                var singleGen = new EntitySingleRestResourceGenerator(
                        element, entityChildren.get(element), entityMappers.get(element));
                this.writeTypeFile(singleGen.generateSingleResource());
                var allGen = new EntityAllRestResourceGenerator(element, entityMappers.get(element));
                this.writeTypeFile(allGen.generateAllResource());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    @Override
    public void afterProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {}
}
