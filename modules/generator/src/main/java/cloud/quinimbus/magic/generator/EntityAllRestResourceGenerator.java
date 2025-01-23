package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Jakarta;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import static cloud.quinimbus.magic.util.Strings.*;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public class EntityAllRestResourceGenerator extends AbstractEntityRestResourceGenerator {

    private final List<EntityMapperDefinition> entityMappers;

    private final Set<RecordContextActionDefinition> recordContextActionDefinitions;

    public EntityAllRestResourceGenerator(
            MagicClassElement recordElement,
            List<EntityMapperDefinition> entityMappers,
            Set<RecordContextActionDefinition> recordContextActionDefinitions) {
        super(recordElement);
        this.entityMappers = entityMappers != null ? entityMappers : List.of();
        this.recordContextActionDefinitions =
                recordContextActionDefinitions != null ? recordContextActionDefinitions : Set.of();
    }

    public MagicTypeSpec generateAllResource() {
        var allResourceTypeBuilder = classBuilder(name + "AllResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superclass())
                .addField(FieldSpec.builder(repository(), "repository", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(constructor());
        if (!weak()) {
            allResourceTypeBuilder
                    .addAnnotation(Jakarta.REQUEST_SCOPED)
                    .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                            .addMember("value", "\"%s\"".formatted(IDs.toPlural(Records.idFromType(recordElement))))
                            .build());
        }
        entityMappers.stream()
                .map(e -> FieldSpec.builder(e.type(), uncapitalize(e.name()), Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .forEach(allResourceTypeBuilder::addField);
        recordContextActionDefinitions.stream()
                .map(e -> FieldSpec.builder(
                                e.type(), uncapitalize(e.type().simpleName()), Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .forEach(allResourceTypeBuilder::addField);
        this.recordElement
                .findFieldsAnnotatedWith(QuiNimbusCommon.SEARCHABLE_ANNOTATION_NAME)
                .filter(ve -> ve.isClass(String.class))
                .forEach(ve -> allResourceTypeBuilder.addMethod(createByPropertyEndpoint(ve)));
        this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .forEach(ve -> allResourceTypeBuilder.addMethod(createBinaryWither(ve)));
        this.recordElement
                .findFieldsOfType(ParameterizedTypeName.get(Java.LIST, QuiNimbusBinarystore.EMBEDDABLE_BINARY))
                .forEach(ve -> allResourceTypeBuilder.addMethod(createBinaryListWither(ve)));
        entityMappers.stream()
                .flatMap(e -> e.methods().stream())
                .map(e -> createMappedAsMethod(e))
                .forEach(allResourceTypeBuilder::addMethod);
        recordContextActionDefinitions.stream()
                .map(e -> createActionMethod(e))
                .forEach(allResourceTypeBuilder::addMethod);
        return new MagicTypeSpec(allResourceTypeBuilder.build(), packageName);
    }

    private ParameterizedTypeName superclass() {
        if (weak()) {
            return ParameterizedTypeName.get(
                    QuiNimbusRest.ABSTRACT_WEAK_CRUD_ALL_RESOURCE, entityTypeName(), idTypeName(), owningTypeName());
        } else {
            return ParameterizedTypeName.get(QuiNimbusRest.ABSTRACT_CRUD_ALL_RESOURCE, entityTypeName(), idTypeName());
        }
    }

    private MethodSpec constructor() {
        var additionalParameters = Stream.concat(
                        entityMappers.stream().map(e -> ParameterSpec.builder(e.type(), uncapitalize(e.name()))
                                .build()),
                        recordContextActionDefinitions.stream().map(e -> ParameterSpec.builder(
                                        e.type(), uncapitalize(e.type().simpleName()))
                                .build()))
                .distinct()
                .toList();
        var constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        var code = CodeBlock.builder();
        if (weak()) {
            constructor
                    .addParameter(ParameterSpec.builder(
                                    ParameterizedTypeName.get(
                                            Java.FUNCTION,
                                            Jakarta.RS_URIINFO,
                                            ParameterizedTypeName.get(Java.OPTIONAL, owningTypeName())),
                                    "owner")
                            .build())
                    .addParameter(
                            ParameterSpec.builder(repository(), "repository").build())
                    .build();
            code.addStatement("super(owner, repository)");
            code.addStatement("this.repository = repository");
        } else {
            constructor
                    .addAnnotation(Jakarta.INJECT)
                    .addParameter(
                            ParameterSpec.builder(repository(), "repository").build())
                    .build();
            code.addStatement("super($T.class, repository)", entityTypeName());
            code.addStatement("this.repository = repository;");
            code.add(initBinaryWither());
        }
        additionalParameters.forEach(constructor::addParameter);
        additionalParameters.forEach(p -> code.addStatement("this.%s = %s".formatted(p.name, p.name)));
        return constructor.addCode(code.build()).build();
    }

    private MethodSpec createByPropertyEndpoint(MagicVariableElement ve) {
        var method = MethodSpec.methodBuilder("by%s".formatted(capitalize(ve.getSimpleName())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                        .addMember("value", "\"/by/%s/{%s}\"".formatted(ve.getSimpleName(), ve.getSimpleName()))
                        .build())
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PRODUCES)
                        .addMember(
                                "value",
                                CodeBlock.builder()
                                        .add("$T.APPLICATION_JSON", Jakarta.RS_MEDIATYPE)
                                        .build())
                        .build())
                .addParameter(
                        ParameterSpec.builder(ClassName.get(ve.getElement().asType()), ve.getSimpleName())
                                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH_PARAM)
                                        .addMember("value", "\"%s\"".formatted(ve.getSimpleName()))
                                        .build())
                                .build())
                .returns(Jakarta.RS_RESPONSE);
        String code;
        if (weak()) {
            method.addParameter(ParameterSpec.builder(Jakarta.RS_URIINFO, "uriInfo")
                    .addAnnotation(AnnotationSpec.builder(Jakarta.RS_CONTEXT).build())
                    .build());
            code =
                    """
                    return this.getByProperty(uriInfo, %s, this.repository::findAllBy%s);
                    """
                            .formatted(ve.getSimpleName(), capitalize(ve.getSimpleName()));
        } else {
            code =
                    """
                    return this.getByProperty(%s, this.repository::findAllBy%s);
                    """
                            .formatted(ve.getSimpleName(), capitalize(ve.getSimpleName()));
        }
        method.addCode(code);
        return method.build();
    }

    private MethodSpec createMappedAsMethod(EntityMapperDefinition.Method method) {
        return MethodSpec.methodBuilder("as%s".formatted(method.returnType()))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                        .addMember("value", "\"/as/%s\"".formatted(uncapitalize(method.returnType())))
                        .build())
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PRODUCES)
                        .addMember(
                                "value",
                                CodeBlock.builder()
                                        .add("$T.APPLICATION_JSON", Jakarta.RS_MEDIATYPE)
                                        .build())
                        .build())
                .addParameter(ParameterSpec.builder(Jakarta.RS_URIINFO, "uriInfo")
                        .addAnnotation(
                                AnnotationSpec.builder(Jakarta.RS_CONTEXT).build())
                        .build())
                .returns(Jakarta.RS_RESPONSE)
                .addCode("return getAllMapped($L::$L);", uncapitalize(method.mapperName()), method.methodName())
                .build();
    }

    private MethodSpec createActionMethod(RecordContextActionDefinition definition) {
        return MethodSpec.methodBuilder("callAction%s".formatted(capitalize(definition.name())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_POST).build())
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                        .addMember("value", "\"/action/%s\"".formatted(uncapitalize(definition.name())))
                        .build())
                .addCode(
                        "this.$L.$L();",
                        uncapitalize(definition.type().simpleName()),
                        definition.method().name())
                .build();
    }
}
