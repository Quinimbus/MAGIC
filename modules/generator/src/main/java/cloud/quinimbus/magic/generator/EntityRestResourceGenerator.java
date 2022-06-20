package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.elements.MagicClassElement;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
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
                        .addMember("value", "\"%s\"".formatted(Records.idFromType(recordElement)))
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(ClassName.get("javax.inject", "Inject"))
                        .addParameter(ParameterSpec
                                .builder(ClassName.get(packageName, name + "Repository"), "repository")
                                .build())
                        .addCode("super(repository);")
                        .build());
        return allResourceTypeBuilder.build();
    }
}
