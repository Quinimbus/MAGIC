package cloud.quinimbus.magic.generator;

import static cloud.quinimbus.magic.util.Strings.*;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.Javax;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public class EntitySingleRestResourceGenerator extends AbstractEntityRestResourceGenerator {

    private final List<MagicClassElement> entityChildren;

    public EntitySingleRestResourceGenerator(MagicClassElement recordElement, List<MagicClassElement> entityChildren) {
        super(recordElement);
        this.entityChildren = entityChildren != null ? entityChildren : List.of();
    }

    public MagicTypeSpec generateSingleResource() {
        var singleResourceTypeBuilder = TypeSpec.classBuilder(name + "SingleResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superclass())
                .addMethod(constructor());
        var additionalFields = entityChildren.stream()
                .map(e -> {
                    var repository = repository(e);
                    return FieldSpec.builder(
                                    repository, uncapitalize(repository.simpleName()), Modifier.PRIVATE, Modifier.FINAL)
                            .build();
                })
                .toList();
        if (!weak()) {
            singleResourceTypeBuilder
                    .addAnnotation(Javax.REQUEST_SCOPED)
                    .addAnnotation(AnnotationSpec.builder(Javax.RS_PATH)
                            .addMember("value", "\"%s/{%s}\"".formatted(Records.idFromType(recordElement), name + "Id"))
                            .build());
        }
        singleResourceTypeBuilder.addMethod(MethodSpec.methodBuilder("idPathParameter")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Java.OVERRIDE)
                .returns(Java.STRING)
                .addCode(CodeBlock.of("return $S;", name + "Id"))
                .build());
        additionalFields.forEach(singleResourceTypeBuilder::addField);
        entityChildren.stream()
                .flatMap(child -> Stream.of(createSubResourceAllMethod(child), createSubResourceSingleMethod(child)))
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
        var additionalParameters = entityChildren.stream()
                .map(e -> {
                    var repository = repository(e);
                    return ParameterSpec.builder(repository, uncapitalize(repository.simpleName()))
                            .build();
                })
                .toList();
        var constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        var code = CodeBlock.builder();
        if (weak()) {
            constructor.addParameter(ParameterSpec.builder(
                            ParameterizedTypeName.get(
                                    Java.FUNCTION,
                                    Javax.RS_URIINFO,
                                    ParameterizedTypeName.get(Java.OPTIONAL, owningTypeName())),
                            "owner")
                    .build());
            code.add("super($T.class, $T.class, owner, repository);", entityTypeName(), idTypeName());
        } else {
            constructor.addAnnotation(Javax.INJECT);
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
                .addAnnotation(AnnotationSpec.builder(Javax.RS_PATH)
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
                .addAnnotation(AnnotationSpec.builder(Javax.RS_PATH)
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
}
