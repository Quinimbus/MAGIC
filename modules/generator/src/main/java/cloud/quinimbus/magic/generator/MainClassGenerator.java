package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.Quarkus;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import cloud.quinimbus.magic.util.CodeGeneratorUtil;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import javax.lang.model.element.Modifier;

public class MainClassGenerator {

    private final String basePackage;

    public MainClassGenerator(String basePackage) {
        this.basePackage = basePackage;
    }

    public MagicTypeSpec generate() {
        var mainClass = CodeGeneratorUtil.classBuilder("Main", this.getClass())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Quarkus.QUARKUS_MAIN)
                .addMethod(MethodSpec.methodBuilder("main")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(ParameterSpec.builder(ArrayTypeName.of(Java.STRING), "args")
                                .build())
                        .addStatement(CodeBlock.of("$T.run(args)", Quarkus.QUARKUS))
                        .build())
                .build();
        return new MagicTypeSpec(mainClass, this.basePackage);
    }
}
