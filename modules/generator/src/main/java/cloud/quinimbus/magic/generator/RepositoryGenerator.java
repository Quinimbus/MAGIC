package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.classnames.QuiNimbusPersistence;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import static cloud.quinimbus.magic.util.Strings.*;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public class RepositoryGenerator extends RecordEntityBasedGenerator {

    public RepositoryGenerator(MagicClassElement recordElement) {
        super(recordElement);
    }

    public MagicTypeSpec generateRepositoryType() {
        var repositoryTypeBuilder = interfaceBuilder(name + "Repository")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(QuiNimbusPersistence.ENTITY_TYPE_CLASS)
                        .addMember("value", CodeBlock.of("$L.class", name))
                        .build());
        if (weak()) {
            repositoryTypeBuilder.addSuperinterface(ParameterizedTypeName.get(
                    QuiNimbusPersistence.WEAK_CRUD_REPOSITORY, entityTypeName(), idTypeName(), owningTypeName()));
        } else {
            repositoryTypeBuilder.addSuperinterface(
                    ParameterizedTypeName.get(QuiNimbusPersistence.CRUD_REPOSITORY, entityTypeName(), idTypeName()));
        }
        recordElement
                .findFieldsAnnotatedWith(QuiNimbusCommon.SEARCHABLE_ANNOTATION_NAME)
                .forEach(ve -> repositoryTypeBuilder.addMethod(createFindAllByMethod(ve)));
        return new MagicTypeSpec(repositoryTypeBuilder.build(), packageName);
    }

    private MethodSpec createFindAllByMethod(MagicVariableElement ve) {
        var method = MethodSpec.methodBuilder("findAllBy%s".formatted(capitalize(ve.getSimpleName())))
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Stream.class), entityTypeName()));
        if (weak()) {
            method.addParameter(owningTypeName(), "owner");
        }
        method.addParameter(TypeName.get(ve.getElement().asType()), "value");
        return method.build();
    }
}
