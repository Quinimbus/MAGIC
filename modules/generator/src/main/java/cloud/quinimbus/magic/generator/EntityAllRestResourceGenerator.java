package cloud.quinimbus.magic.generator;

import static cloud.quinimbus.magic.util.Strings.*;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.Jakarta;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicExecutableElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public class EntityAllRestResourceGenerator extends AbstractEntityRestResourceGenerator {

    private final List<MagicClassElement> entityMappers;

    public EntityAllRestResourceGenerator(MagicClassElement recordElement, List<MagicClassElement> entityMappers) {
        super(recordElement);
        this.entityMappers = entityMappers != null ? entityMappers : List.of();
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
                .map(e -> FieldSpec.builder(
                                e.getType(), uncapitalize(e.getSimpleName()), Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .forEach(allResourceTypeBuilder::addField);
        this.recordElement
                .findFieldsAnnotatedWith(QuiNimbusCommon.SEARCHABLE_ANNOTATION_NAME)
                .filter(ve -> ve.isClass(String.class))
                .forEach(ve -> allResourceTypeBuilder.addMethod(createByPropertyEndpoint(ve)));
        this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .forEach(ve -> allResourceTypeBuilder.addMethod(createBinaryWither(ve)));
        entityMappers.stream()
                .flatMap(e -> e.findMethods())
                .filter(m -> m.parameterCount() == 1)
                .filter(m -> m.parameters().findFirst().orElseThrow().getType().equals(this.entityTypeName()))
                .map(e -> createMappedAsMethod(e))
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
        var additionalParameters = entityMappers.stream()
                .map(e -> ParameterSpec.builder(e.getType(), uncapitalize(e.getSimpleName()))
                        .build())
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
            code.add(
                    """
                    super(owner, repository);
                    this.repository = repository;
                    """);
        } else {
            constructor
                    .addAnnotation(Jakarta.INJECT)
                    .addParameter(
                            ParameterSpec.builder(repository(), "repository").build())
                    .build();
            code.add(
                    """
                    super($T.class, repository);
                    this.repository = repository;
                    """,
                    entityTypeName());
            code.add(initBinaryWither());
        }
        additionalParameters.forEach(constructor::addParameter);
        additionalParameters.forEach(p -> code.add("this.%s = %s;".formatted(p.name, p.name)));
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
                        .addMember("value",
                                CodeBlock.builder()
                                        .add("$T.APPLICATION_JSON", Jakarta.RS_MEDIATYPE)
                                        .build())
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.get(ve.getElement().asType()), ve.getSimpleName())
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

    private MethodSpec createBinaryWither(MagicVariableElement ve) {
        var constructorParams = this.recordElement
                .findFields()
                .map(field -> {
                    if (field.getSimpleName().equals(ve.getSimpleName())) {
                        return ve.getSimpleName();
                    } else {
                        return "entity.%s()".formatted(field.getSimpleName());
                    }
                })
                .collect(Collectors.joining(", "));
        return MethodSpec.methodBuilder("withBinaryFor%s".formatted(capitalize(ve.getSimpleName())))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterSpec.builder(entityTypeName(), "entity").build())
                .addParameter(ParameterSpec.builder(QuiNimbusBinarystore.EMBEDDABLE_BINARY, ve.getSimpleName())
                        .build())
                .addCode("return new $T($L);", entityTypeName(), constructorParams)
                .returns(entityTypeName())
                .build();
    }

    private CodeBlock initBinaryWither() {
        return this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .map(e -> CodeBlock.of(
                        "super.addBinaryWither(\"$L\", this::withBinaryFor$L);",
                        e.getSimpleName(),
                        capitalize(e.getSimpleName())))
                .collect(CodeBlock.joining(";"));
    }

    private MethodSpec createMappedAsMethod(MagicExecutableElement method) {
        var returnType = method.returnType();
        return MethodSpec.methodBuilder("as%s".formatted(returnType.getSimpleName()))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                        .addMember("value", "\"/as/%s\"".formatted(uncapitalize(returnType.getSimpleName())))
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
                .addCode(
                        "return getAllMapped($L::$L);",
                        uncapitalize(method.enclosingElement().getSimpleName()),
                        method.getSimpleName())
                .build();
    }
}
