package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
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

    public TypeSpec generateSingleResource() {
        var singleResourceTypeBuilder = TypeSpec.classBuilder(name + "SingleResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(
                        ClassName.get("cloud.quinimbus.rest.crud", "AbstractCrudSingleResource"),
                        ClassName.get(packageName, name),
                        ClassName.get(idType)))
                .addAnnotation(ClassName.get("javax.enterprise.context", "RequestScoped"))
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "\"%s/{entityid}\"".formatted(Records.idFromType(recordElement)))
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(ClassName.get("javax.inject", "Inject"))
                        .addParameter(ParameterSpec
                                .builder(ClassName.get(packageName, name + "Repository"), "repository")
                                .build())
                        .addCode("super(repository);")
                        .build());
        return singleResourceTypeBuilder.build();
    }

    public TypeSpec generateAllResource() {
        var allResourceTypeBuilder = TypeSpec.classBuilder(name + "AllResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(
                        ClassName.get("cloud.quinimbus.rest.crud", "AbstractCrudAllResource"),
                        ClassName.get(packageName, name),
                        ClassName.get(idType)))
                .addAnnotation(ClassName.get("javax.enterprise.context", "RequestScoped"))
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "\"%s\"".formatted(IDs.toPlural(Records.idFromType(recordElement))))
                        .build())
                .addField(FieldSpec.builder(repository(), "repository", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(ClassName.get("javax.inject", "Inject"))
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
        return allResourceTypeBuilder.build();
    }

    private void createByPropertyEndpoint(TypeSpec.Builder builder, MagicVariableElement ve) {
        builder.addMethod(MethodSpec.methodBuilder("by%s".formatted(capitalize(ve.getSimpleName())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "GET"))
                        .build())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "\"/by/%s/{%s}\"".formatted(ve.getSimpleName(), ve.getSimpleName()))
                        .build())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Produces"))
                        .addMember("value", CodeBlock.builder()
                                .add("$T.APPLICATION_JSON", ClassName.get("javax.ws.rs.core", "MediaType"))
                                .build())
                        .build())
                .addParameter(ParameterSpec
                        .builder(ClassName.get(ve.getElement().asType()), ve.getSimpleName())
                        .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "PathParam"))
                                .addMember("value", "\"%s\"".formatted(ve.getSimpleName()))
                                .build())
                        .build())
                .returns(ClassName.get("javax.ws.rs.core", "Response"))
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
