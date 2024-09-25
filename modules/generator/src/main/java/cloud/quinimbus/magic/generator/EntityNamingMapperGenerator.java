package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.MapStruct;
import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.classnames.QuiNimbusMagic;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import java.util.Optional;
import javax.lang.model.element.Modifier;

public class EntityNamingMapperGenerator extends RecordEntityBasedGenerator {

    public EntityNamingMapperGenerator(MagicClassElement recordElement) {
        super(recordElement);
    }

    public Optional<MagicTypeSpec> generaterMapper() {
        var namingFields = this.recordElement
                .findFieldsAnnotatedWith(QuiNimbusCommon.NAMING_ANNOTATION_NAME)
                .toList();
        if (namingFields.isEmpty()) {
            return Optional.empty();
        }
        if (namingFields.size() > 1) {
            throw new IllegalStateException("More than one @Naming field is not supported.");
        }
        var namingField = namingFields.get(0);
        var mapperTypeBuilder = interfaceBuilder(name + "ToIdAndLabelMapper")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(
                        AnnotationSpec.builder(MapStruct.MAPPER_ANNOTATION_NAME).build())
                .addAnnotation(AnnotationSpec.builder(QuiNimbusMagic.ADD_MAPPER_TO_GENERATED_REST_ENDPOINT)
                        .addMember("forEntity", CodeBlock.of("$T.class", this.entityTypeName()))
                        .build())
                .addMethod(this.createMapperMethod(namingField.getSimpleName()));
        return Optional.ofNullable(new MagicTypeSpec(mapperTypeBuilder.build(), packageName));
    }

    private MethodSpec createMapperMethod(String namingFieldName) {
        return MethodSpec.methodBuilder("toIdAndLabel")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(QuiNimbusRest.ID_AND_LABEL_RECORD)
                .addAnnotation(AnnotationSpec.builder(MapStruct.MAPPING_ANNOTATION_NAME)
                        .addMember("source", CodeBlock.of("$S", this.idFieldName))
                        .addMember("target", CodeBlock.of("$S", "id"))
                        .build())
                .addAnnotation(AnnotationSpec.builder(MapStruct.MAPPING_ANNOTATION_NAME)
                        .addMember("source", CodeBlock.of("$S", namingFieldName))
                        .addMember("target", CodeBlock.of("$S", "label"))
                        .build())
                .addParameter(this.entityTypeName(), "entity")
                .build();
    }
}
