package cloud.quinimbus.magic.util;

import cloud.quinimbus.magic.classnames.Java;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;

public class CodeGeneratorUtil {
    public static TypeSpec.Builder classBuilder(String name, Class<?> generatorClass) {
        return TypeSpec.classBuilder(name).addAnnotation(generatedAnnotation(generatorClass));
    }

    public static TypeSpec.Builder interfaceBuilder(String name, Class<?> generatorClass) {
        return TypeSpec.interfaceBuilder(name).addAnnotation(generatedAnnotation(generatorClass));
    }

    private static AnnotationSpec generatedAnnotation(Class<?> generatorClass) {
        return AnnotationSpec.builder(Java.GENERATED)
                .addMember("value", "\"%s\"".formatted(generatorClass.getCanonicalName()))
                .build();
    }
}
