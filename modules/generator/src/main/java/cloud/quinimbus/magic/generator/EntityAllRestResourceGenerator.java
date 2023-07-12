package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.Javax;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import static cloud.quinimbus.magic.util.Strings.*;
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

public class EntityAllRestResourceGenerator extends AbstractEntityRestResourceGenerator {

    public EntityAllRestResourceGenerator(MagicClassElement recordElement) {
        super(recordElement);
    }

    public MagicTypeSpec generateAllResource() {
        var allResourceTypeBuilder = TypeSpec.classBuilder(name + "AllResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superclass())
                .addField(FieldSpec.builder(repository(), "repository", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(constructor());
        if (!weak()) {
            allResourceTypeBuilder
                    .addAnnotation(Javax.REQUEST_SCOPED)
                    .addAnnotation(AnnotationSpec.builder(Javax.RS_PATH)
                            .addMember("value", "\"%s\"".formatted(IDs.toPlural(Records.idFromType(recordElement))))
                            .build());
        }
        this.recordElement.findFieldsAnnotatedWith("cloud.quinimbus.persistence.api.annotation.Searchable")
                .filter(ve -> ve.isClass(String.class))
                .forEach(ve -> allResourceTypeBuilder.addMethod(createByPropertyEndpoint(ve)));
        return new MagicTypeSpec(allResourceTypeBuilder.build(), packageName);
    }
    
    private ParameterizedTypeName superclass() {
        if (weak()) {
            return ParameterizedTypeName.get(
                    QuiNimbusRest.ABSTRACT_WEAK_CRUD_ALL_RESOURCE,
                    entityTypeName(),
                    idTypeName(),
                    owningTypeName());
        } else {
            return ParameterizedTypeName.get(
                    QuiNimbusRest.ABSTRACT_CRUD_ALL_RESOURCE,
                    entityTypeName(),
                    idTypeName());
        }
    }
    
    private MethodSpec constructor() {
        var constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        if (weak()) {
            constructor
                    .addParameter(ParameterSpec
                            .builder(ParameterizedTypeName.get(
                                    Java.FUNCTION,
                                    Javax.RS_URIINFO,
                                    ParameterizedTypeName.get(
                                            Java.OPTIONAL,
                                            owningTypeName())),
                                    "owner")
                            .build())
                    .addParameter(ParameterSpec
                            .builder(repository(), "repository")
                            .build())
                    .addCode("""
                                 super(owner, repository);
                                 this.repository = repository;
                                 """)
                    .build();
        } else {
            constructor
                    .addAnnotation(Javax.INJECT)
                    .addParameter(ParameterSpec
                            .builder(repository(), "repository")
                            .build())
                    .addCode("""
                                 super(repository);
                                 this.repository = repository;
                                 """)
                    .build();
        }
        return constructor.build();
    }

    private MethodSpec createByPropertyEndpoint(MagicVariableElement ve) {
        var method = MethodSpec.methodBuilder("by%s".formatted(capitalize(ve.getSimpleName())))
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
                .returns(Javax.RS_RESPONSE);
        String code;
        if (weak()) {
            method.addParameter(ParameterSpec.builder(Javax.RS_URIINFO, "uriInfo")
                    .addAnnotation(AnnotationSpec.builder(Javax.RS_CONTEXT).build())
                    .build());
            code = """
                   return this.getByProperty(uriInfo, %s, this.repository::findAllBy%s);
                   """
                    .formatted(
                            ve.getSimpleName(),
                            capitalize(ve.getSimpleName()));
        } else {
            code = """
                   return this.getByProperty(%s, this.repository::findAllBy%s);
                   """
                    .formatted(
                            ve.getSimpleName(),
                            capitalize(ve.getSimpleName()));
        }
        method.addCode(code);
        return method.build();
    }
}
