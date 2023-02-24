package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Javax;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;

public class EntityRestResourceGenerator extends RecordEntityBasedGenerator {

    public EntityRestResourceGenerator(MagicClassElement recordElement) {
        super(recordElement);
    }

    public MagicTypeSpec generateSingleResource() {
        var singleResourceTypeBuilder = TypeSpec.classBuilder(name + "SingleResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(
                        QuiNimbusRest.ABSTRACT_CRUD_SINGLE_RESOURCE,
                        entityTypeName(),
                        idTypeName()))
                .addAnnotation(Javax.REQUEST_SCOPED)
                .addAnnotation(AnnotationSpec.builder(Javax.RS_PATH)
                        .addMember("value", "\"%s/{entityid}\"".formatted(Records.idFromType(recordElement)))
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Javax.INJECT)
                        .addParameter(ParameterSpec
                                .builder(ClassName.get(packageName, name + "Repository"), "repository")
                                .build())
                        .addCode("super(repository);")
                        .build());
        return new MagicTypeSpec(singleResourceTypeBuilder.build(), packageName);
    }

    public MagicTypeSpec generateAllResource() {
        var allResourceTypeBuilder = TypeSpec.classBuilder(name + "AllResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(
                        QuiNimbusRest.ABSTRACT_CRUD_ALL_RESOURCE,
                        entityTypeName(),
                        idTypeName()))
                .addAnnotation(Javax.REQUEST_SCOPED)
                .addAnnotation(AnnotationSpec.builder(Javax.RS_PATH)
                        .addMember("value", "\"%s\"".formatted(IDs.toPlural(Records.idFromType(recordElement))))
                        .build())
                .addField(FieldSpec.builder(repository(), "repository", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Javax.INJECT)
                        .addParameter(ParameterSpec
                                .builder(repository(), "repository")
                                .build())
                        .addCode("""
                                 super(repository);
                                 this.repository = repository;
                                 """)
                        .build());
        this.recordElement.findFieldsAnnotatedWith("cloud.quinimbus.persistence.api.annotation.Searchable")
                .filter(ve -> ve.isClass(String.class))
                .forEach(ve -> createByPropertyEndpoint(allResourceTypeBuilder, ve));
        return new MagicTypeSpec(allResourceTypeBuilder.build(), packageName);
    }

    private void createByPropertyEndpoint(TypeSpec.Builder builder, MagicVariableElement ve) {
        builder.addMethod(MethodSpec.methodBuilder("by%s".formatted(capitalize(ve.getSimpleName())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Javax.RS_GET)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Javax.RS_PATH)
                        .addMember("value", "\"/by/%s/{%s}\"".formatted(ve.getSimpleName(), ve.getSimpleName()))
                        .build())
                .addAnnotation(AnnotationSpec.builder(Javax.RS_PRODUCES)
                        .addMember("value", CodeBlock.builder()
                                .add("$T.APPLICATION_JSON", Javax.RS_MEDIATYPE)
                                .build())
                        .build())
                .addParameter(ParameterSpec
                        .builder(ClassName.get(ve.getElement().asType()), ve.getSimpleName())
                        .addAnnotation(AnnotationSpec.builder(Javax.RS_PATH_PARAM)
                                .addMember("value", "\"%s\"".formatted(ve.getSimpleName()))
                                .build())
                        .build())
                .returns(Javax.RS_RESPONSE)
                .addCode("""
                         return this.getByProperty(%s, this.repository::findAllBy%s);
                         """
                        .formatted(
                                ve.getSimpleName(),
                                capitalize(ve.getSimpleName())))
                .build());
    }
    
    private ClassName repository() {
        return ClassName.get(packageName, name + "Repository");
    }
}
