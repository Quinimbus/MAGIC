package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Jakarta;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicExecutableElement;
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
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public class EntitySingleRestResourceGenerator extends AbstractEntityRestResourceGenerator {

    private final List<MagicClassElement> entityChildren;

    private final List<MagicClassElement> entityMappers;

    public EntitySingleRestResourceGenerator(
            MagicClassElement recordElement,
            List<MagicClassElement> entityChildren,
            List<MagicClassElement> entityMappers) {
        super(recordElement);
        this.entityChildren = entityChildren != null ? entityChildren : List.of();
        this.entityMappers = entityMappers != null ? entityMappers : List.of();
    }

    public MagicTypeSpec generateSingleResource() {
        var singleResourceTypeBuilder = classBuilder(name + "SingleResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superclass())
                .addMethod(constructor());
        if (!weak()) {
            singleResourceTypeBuilder
                    .addAnnotation(Jakarta.REQUEST_SCOPED)
                    .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                            .addMember("value", "\"%s/{%s}\"".formatted(Records.idFromType(recordElement), name + "Id"))
                            .build());
        }
        singleResourceTypeBuilder.addMethod(MethodSpec.methodBuilder("idPathParameter")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Java.OVERRIDE)
                .returns(Java.STRING)
                .addCode(CodeBlock.of("return $S;", name + "Id"))
                .build());
        entityChildren.stream()
                .map(e -> {
                    var repository = repository(e);
                    return FieldSpec.builder(
                                    repository, uncapitalize(repository.simpleName()), Modifier.PRIVATE, Modifier.FINAL)
                            .build();
                })
                .forEach(singleResourceTypeBuilder::addField);
        entityChildren.stream()
                .flatMap(child -> Stream.of(createSubResourceAllMethod(child), createSubResourceSingleMethod(child)))
                .forEach(singleResourceTypeBuilder::addMethod);
        entityMappers.stream()
                .map(e -> FieldSpec.builder(
                                e.getType(), uncapitalize(e.getSimpleName()), Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .forEach(singleResourceTypeBuilder::addField);
        this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .forEach(ve -> singleResourceTypeBuilder.addMethod(createBinaryDownload(ve)));
        entityMappers.stream()
                .flatMap(e -> e.findMethods())
                .filter(m -> m.parameterCount() == 1)
                .filter(m -> m.parameters().findFirst().orElseThrow().getType().equals(this.entityTypeName()))
                .map(e -> createMappedAsMethod(e))
                .forEach(singleResourceTypeBuilder::addMethod);
        return new MagicTypeSpec(singleResourceTypeBuilder.build(), packageName);
    }

    private ParameterizedTypeName superclass() {
        if (weak()) {
            return ParameterizedTypeName.get(
                    QuiNimbusRest.ABSTRACT_WEAK_CRUD_SINGLE_RESOURCE, entityTypeName(), idTypeName(), owningTypeName());
        } else {
            return ParameterizedTypeName.get(
                    QuiNimbusRest.ABSTRACT_CRUD_SINGLE_RESOURCE, entityTypeName(), idTypeName());
        }
    }

    private MethodSpec constructor() {
        var additionalParameters = Stream.concat(
                        entityChildren.stream().map(e -> ParameterSpec.builder(
                                        repository(e),
                                        uncapitalize(repository(e).simpleName()))
                                .build()),
                        entityMappers.stream()
                                .map(e -> ParameterSpec.builder(e.getType(), uncapitalize(e.getSimpleName()))
                                        .build()))
                .toList();
        var constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        var code = CodeBlock.builder();
        if (weak()) {
            constructor.addParameter(ParameterSpec.builder(
                            ParameterizedTypeName.get(
                                    Java.FUNCTION,
                                    Jakarta.RS_URIINFO,
                                    ParameterizedTypeName.get(Java.OPTIONAL, owningTypeName())),
                            "owner")
                    .build());
            code.add("super($T.class, $T.class, owner, repository);", entityTypeName(), idTypeName());
        } else {
            constructor.addAnnotation(Jakarta.INJECT);
            code.add("super($T.class, $T.class, repository);", entityTypeName(), idTypeName());
        }
        constructor.addParameter(ParameterSpec.builder(ClassName.get(packageName, name + "Repository"), "repository")
                .build());
        additionalParameters.forEach(constructor::addParameter);
        additionalParameters.forEach(p -> code.add("this.%s = %s;".formatted(p.name, p.name)));
        return constructor.addCode(code.build()).build();
    }

    private MethodSpec createSubResourceAllMethod(MagicClassElement child) {
        var pluralName = IDs.toPlural(relativizeToName(child.getSimpleName()));
        var resourceClass = ClassName.get(child.getPackageName(), "%sAllResource".formatted(child.getSimpleName()));
        return MethodSpec.methodBuilder("subResource%s".formatted(capitalize(pluralName)))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                        .addMember("value", "\"/%s/\"".formatted(uncapitalize(pluralName)))
                        .build())
                .returns(resourceClass)
                .addCode(CodeBlock.of(
                        """
                        return new $T(this::findEntityById, $L);
                        """,
                        resourceClass,
                        uncapitalize(repository(child).simpleName())))
                .build();
    }

    private MethodSpec createSubResourceSingleMethod(MagicClassElement child) {
        var singluarName = relativizeToName(child.getSimpleName());
        var resourceClass = ClassName.get(child.getPackageName(), "%sSingleResource".formatted(child.getSimpleName()));
        return MethodSpec.methodBuilder("subResource%s".formatted(capitalize(singluarName)))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                        .addMember(
                                "value",
                                "\"/%s/{%s}/\"".formatted(uncapitalize(singluarName), child.getSimpleName() + "Id"))
                        .build())
                .returns(resourceClass)
                .addCode(CodeBlock.of(
                        """
                        return new $T(this::findEntityById, $L);
                        """,
                        resourceClass,
                        uncapitalize(repository(child).simpleName())))
                .build();
    }

    private MethodSpec createBinaryDownload(MagicVariableElement field) {
        return MethodSpec.methodBuilder("download%s".formatted(capitalize(field.getSimpleName())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH)
                        .addMember("value", "\"/%s/download\"".formatted(uncapitalize(field.getSimpleName())))
                        .build())
                .addParameter(ParameterSpec.builder(Jakarta.RS_URIINFO, "uriInfo")
                        .addAnnotation(
                                AnnotationSpec.builder(Jakarta.RS_CONTEXT).build())
                        .build())
                .returns(Jakarta.RS_RESPONSE)
                .addCode("return downloadBinary(uriInfo, $T::$L);", entityTypeName(), field.getSimpleName())
                .build();
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
                        "return getByIdMapped(uriInfo, $L::$L);",
                        uncapitalize(method.enclosingElement().getSimpleName()),
                        method.getSimpleName())
                .build();
    }
}
