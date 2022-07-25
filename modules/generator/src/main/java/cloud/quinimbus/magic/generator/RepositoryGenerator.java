package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.elements.MagicClassElement;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public class RepositoryGenerator extends RecordEntityBasedGenerator {

    public RepositoryGenerator(MagicClassElement recordElement) {
        super(recordElement);
    }

    public TypeSpec generateRepositoryType() {
        var repositoryTypeBuilder = TypeSpec.interfaceBuilder(name + "Repository")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName
                        .get("cloud.quinimbus.persistence.api.annotation", "EntityTypeClass"))
                        .addMember("value", CodeBlock.of("$L.class", name))
                        .build())
                .addSuperinterface(
                        ParameterizedTypeName.get(
                                ClassName.get("cloud.quinimbus.persistence.repositories", "CRUDRepository"),
                                ClassName.get(packageName, name),
                                ClassName.get(idType)));
        recordElement.findFieldsAnnotatedWith("cloud.quinimbus.persistence.api.annotation.Searchable")
                .forEach(ve -> {
                    repositoryTypeBuilder.addMethod(
                            MethodSpec.methodBuilder("findAllBy%s".formatted(capitalize(ve.getSimpleName())))
                                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                                    .addParameter(TypeName.get(ve.getElement().asType()), "value")
                                    .returns(ParameterizedTypeName.get(
                                            ClassName.get(Stream.class),
                                            ClassName.get(packageName, name)))
                                    .build());
                });
        return repositoryTypeBuilder.build();
    }
}
