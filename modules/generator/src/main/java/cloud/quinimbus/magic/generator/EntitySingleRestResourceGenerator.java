package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Jakarta;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
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
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public class EntitySingleRestResourceGenerator extends AbstractEntityRestResourceGenerator {

    private final List<MagicClassElement> entityChildren;

    private final List<EntityMapperDefinition> entityMappers;

    public EntitySingleRestResourceGenerator(
            MagicClassElement recordElement,
            List<MagicClassElement> entityChildren,
            List<EntityMapperDefinition> entityMappers) {
        super(recordElement);
        this.entityChildren = entityChildren != null ? entityChildren : List.of();
        this.entityMappers = entityMappers != null ? entityMappers : List.of();
    }

    public MagicTypeSpec generateSingleResource() {
        var singleResourceTypeBuilder = classBuilder(name + "SingleResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superclass())
                .addMethod(constructor())
                .addMethod(createGetById(weak()))
                .addMethod(createReplace())
                .addMethod(createDeleteById());
        if (!weak()) {
            singleResourceTypeBuilder
                    .addAnnotation(Jakarta.REQUEST_SCOPED)
                    .addAnnotation(path("%s/{%s}".formatted(Records.idFromType(recordElement), name + "Id")))
                    .addMethod(createReplaceByMultipart());
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
                .map(e -> FieldSpec.builder(e.type(), uncapitalize(e.name()), Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .forEach(singleResourceTypeBuilder::addField);
        this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .forEach(ve -> singleResourceTypeBuilder.addMethod(createBinaryDownload(ve)));
        this.recordElement
                .findFieldsOfType(ParameterizedTypeName.get(Java.LIST, QuiNimbusBinarystore.EMBEDDABLE_BINARY))
                .forEach(ve -> singleResourceTypeBuilder.addMethod(createListBinaryDownload(ve)));
        this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .forEach(ve -> singleResourceTypeBuilder.addMethod(createBinaryWither(ve)));
        this.recordElement
                .findFieldsOfType(ParameterizedTypeName.get(Java.LIST, QuiNimbusBinarystore.EMBEDDABLE_BINARY))
                .forEach(ve -> singleResourceTypeBuilder.addMethod(createBinaryListWither(ve)));
        entityMappers.stream()
                .flatMap(e -> e.methods().stream())
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
                        entityMappers.stream().map(e -> ParameterSpec.builder(e.type(), uncapitalize(e.name()))
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
            code.addStatement("super($T.class, $T.class, owner, repository)", entityTypeName(), idTypeName());
        } else {
            constructor.addAnnotation(Jakarta.INJECT);
            code.addStatement("super($T.class, $T.class, repository)", entityTypeName(), idTypeName());
            code.add(initBinaryWither());
        }
        constructor.addParameter(ParameterSpec.builder(ClassName.get(packageName, name + "Repository"), "repository")
                .build());
        additionalParameters.forEach(constructor::addParameter);
        additionalParameters.forEach(p -> code.addStatement("this.%s = %s".formatted(p.name, p.name)));
        return constructor.addCode(code.build()).build();
    }

    private MethodSpec createSubResourceAllMethod(MagicClassElement child) {
        var pluralName = IDs.toPlural(relativizeToName(child.getSimpleName()));
        var resourceClass = ClassName.get(child.getPackageName(), "%sAllResource".formatted(child.getSimpleName()));
        return MethodSpec.methodBuilder("subResource%s".formatted(capitalize(pluralName)))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(path("/%s/".formatted(uncapitalize(pluralName))))
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
                .addAnnotation(path("/%s/{%s}/".formatted(uncapitalize(singluarName), child.getSimpleName() + "Id")))
                .returns(resourceClass)
                .addCode(CodeBlock.of(
                        """
                        return new $T(this::findEntityById, $L);
                        """,
                        resourceClass,
                        uncapitalize(repository(child).simpleName())))
                .build();
    }

    private MethodSpec createGetById(boolean uriInfoParameter) {
        var method = MethodSpec.methodBuilder("getById")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(producesJson())
                .addParameter(injectUriInfo())
                .returns(Jakarta.RS_RESPONSE)
                .addStatement("return super.getById(uriInfo)");
        return method.build();
    }

    private MethodSpec createReplace() {
        return MethodSpec.methodBuilder("replace")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PUT).build())
                .addAnnotation(consumesJson())
                .addParameter(ParameterSpec.builder(entityTypeName(), "entity").build())
                .returns(Jakarta.RS_RESPONSE)
                .addStatement("return super.replace(entity)")
                .build();
    }

    private MethodSpec createReplaceByMultipart() {
        return MethodSpec.methodBuilder("replaceByMultipart")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PUT).build())
                .addAnnotation(consumesMultipart())
                .addParameter(
                        ParameterSpec.builder(ParameterizedTypeName.get(Java.LIST, Jakarta.RS_ENTITY_PART), "parts")
                                .build())
                .returns(Jakarta.RS_RESPONSE)
                .addException(Java.IO_EXCEPTION)
                .addStatement("return super.replaceByMultipart(parts)")
                .build();
    }

    private MethodSpec createDeleteById() {
        return MethodSpec.methodBuilder("deleteById")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_DELETE).build())
                .addParameter(injectUriInfo())
                .returns(Jakarta.RS_RESPONSE)
                .addStatement("return super.deleteById(uriInfo)")
                .build();
    }

    private MethodSpec createBinaryDownload(MagicVariableElement field) {
        return MethodSpec.methodBuilder("download%s".formatted(capitalize(field.getSimpleName())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(path("/%s/download".formatted(uncapitalize(field.getSimpleName()))))
                .addParameter(injectUriInfo())
                .returns(Jakarta.RS_RESPONSE)
                .addCode("return downloadBinary(uriInfo, $T::$L);", entityTypeName(), field.getSimpleName())
                .build();
    }

    private MethodSpec createListBinaryDownload(MagicVariableElement field) {
        return MethodSpec.methodBuilder("download%s".formatted(capitalize(field.getSimpleName())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(
                        path("/%s/{binaryPropertyIndex}/download".formatted(uncapitalize(field.getSimpleName()))))
                .addParameter(ParameterSpec.builder(Jakarta.RS_URIINFO, "uriInfo")
                        .addAnnotation(
                                AnnotationSpec.builder(Jakarta.RS_CONTEXT).build())
                        .build())
                .returns(Jakarta.RS_RESPONSE)
                .addCode("return downloadBinaryFromList(uriInfo, $T::$L);", entityTypeName(), field.getSimpleName())
                .build();
    }

    private MethodSpec createMappedAsMethod(EntityMapperDefinition.Method method) {
        return MethodSpec.methodBuilder("as%s".formatted(method.returnType()))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(path("/as/%s".formatted(uncapitalize(method.returnType()))))
                .addAnnotation(producesJson())
                .addParameter(ParameterSpec.builder(Jakarta.RS_URIINFO, "uriInfo")
                        .addAnnotation(
                                AnnotationSpec.builder(Jakarta.RS_CONTEXT).build())
                        .build())
                .returns(Jakarta.RS_RESPONSE)
                .addCode(
                        "return getByIdMapped(uriInfo, $L::$L);",
                        uncapitalize(method.mapperName()),
                        method.methodName())
                .build();
    }
}
