package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.classnames.QuiNimbusMagic;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.generator.EntityAllRestResourceGenerator;
import cloud.quinimbus.magic.generator.EntityMapperDefinition;
import cloud.quinimbus.magic.generator.EntityNamingMapperGenerator;
import cloud.quinimbus.magic.generator.EntitySingleRestResourceGenerator;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import com.squareup.javapoet.ClassName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.apache.commons.lang3.function.Failable;

@SupportedAnnotationTypes({
    QuiNimbusMagic.GENERATE_REST_ENDPOINTS,
    QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT_NAME
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class GenerateRestEndpointsProcessor extends MagicClassProcessor {

    private Map<MagicClassElement, List<MagicClassElement>> entityChildren = new LinkedHashMap<>();

    private Map<MagicClassElement, List<EntityMapperDefinition>> entityMappers = new LinkedHashMap<>();

    @Override
    public void setup(RoundEnvironment re) {}

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
                .filter(e -> e.isAnnotatedWith(QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT_NAME))
                .collect(Collectors.groupingBy(
                        e -> e.findAnnotation(QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT_NAME)
                                .flatMap(a -> a.getElementValue("forEntity"))
                                .map(MagicClassElement.class::cast)
                                .orElseThrow(),
                        Collectors.mapping(this::toMapperDefinition, Collectors.toList()))));
    }

    @Override
    public void process(TypeElement annotation, MagicClassElement element) {
        if (annotation.getQualifiedName().contentEquals(QuiNimbusMagic.GENERATE_REST_ENDPOINTS)) {
            try {
                var namingMapperGen = new EntityNamingMapperGenerator(element);
                namingMapperGen.generaterMapper().ifPresent(Failable.asConsumer(spec -> {
                    this.writeTypeFile(spec);
                    entityMappers
                            .computeIfAbsent(element, e -> new ArrayList<>())
                            .add(toMapperDefinition(spec, element));
                }));
                var singleGen = new EntitySingleRestResourceGenerator(
                        element, entityChildren.get(element), entityMappers.get(element));
                this.writeTypeFile(singleGen.generateSingleResource());
                var allGen = new EntityAllRestResourceGenerator(element, entityMappers.get(element));
                this.writeTypeFile(allGen.generateAllResource());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        } else if (annotation
                .getQualifiedName()
                .contentEquals(QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT_NAME)) {
            var recordElement = element.findAnnotation(QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT_NAME)
                    .flatMap(a -> a.getElementValue("forEntity"))
                    .map(MagicClassElement.class::cast)
                    .orElseThrow();
        }
    }

    @Override
    public void afterProcessAll(TypeElement annotation, Set<MagicClassElement> elements) {}

    private EntityMapperDefinition toMapperDefinition(MagicClassElement element) {
        var recordElement = element.findAnnotation(QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT_NAME)
                .flatMap(a -> a.getElementValue("forEntity"))
                .map(MagicClassElement.class::cast)
                .orElseThrow();
        return new EntityMapperDefinition(
                element.getType(),
                element.getSimpleName(),
                element.findMethods()
                        .filter(m -> m.parameterCount() == 1)
                        .filter(m -> m.parameters()
                                .findFirst()
                                .orElseThrow()
                                .getType()
                                .equals(recordElement.getType()))
                        .map(e -> new EntityMapperDefinition.Method(
                                e.returnType().getSimpleName(),
                                e.getSimpleName(),
                                e.enclosingElement().getSimpleName()))
                        .toList());
    }

    private EntityMapperDefinition toMapperDefinition(MagicTypeSpec spec, MagicClassElement recordElement) {
        return new EntityMapperDefinition(
                spec.toTypeName(),
                spec.getTypeSpec().name,
                spec.getTypeSpec().methodSpecs.stream()
                        .filter(m -> m.parameters.size() == 1)
                        .filter(m -> m.parameters.get(0).type.equals(recordElement.getType()))
                        .map(ms -> new EntityMapperDefinition.Method(
                                ((ClassName) ms.returnType).simpleName(), ms.name, spec.getTypeSpec().name))
                        .toList());
    }
}
