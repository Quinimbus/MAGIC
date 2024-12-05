package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.magic.classnames.Jakarta;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import static cloud.quinimbus.magic.util.Strings.capitalize;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public class AbstractEntityRestResourceGenerator extends RecordEntityBasedGenerator {

    public AbstractEntityRestResourceGenerator(MagicClassElement recordElement) {
        super(recordElement);
    }

    ClassName repository() {
        return ClassName.get(packageName, name + "Repository");
    }

    ClassName repository(MagicClassElement e) {
        return ClassName.get(e.getPackageName(), e.getSimpleName().concat("Repository"));
    }

    CodeBlock initBinaryWither() {
        var binaryWithers = this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .map(e -> CodeBlock.of(
                        "$[super.addBinaryWither(\"$L\", this::withBinaryFor$L);\n$]",
                        e.getSimpleName(),
                        capitalize(e.getSimpleName())))
                .collect(CodeBlock.joining(""));
        var binaryListWithers = this.recordElement
                .findFieldsOfType(ParameterizedTypeName.get(Java.LIST, QuiNimbusBinarystore.EMBEDDABLE_BINARY))
                .map(e -> CodeBlock.of(
                        "$[super.addBinaryListWither(\"$L\", this::withBinaryFor$L);\n$]",
                        e.getSimpleName(),
                        capitalize(e.getSimpleName())))
                .collect(CodeBlock.joining(""));
        return binaryWithers.toBuilder().add(binaryListWithers).build();
    }

    MethodSpec createBinaryWither(MagicVariableElement ve) {
        var constructorParams = this.recordElement
                .findFields()
                .map(field -> {
                    if (field.getSimpleName().equals(ve.getSimpleName())) {
                        return ve.getSimpleName();
                    } else {
                        return "entity.%s()".formatted(field.getSimpleName());
                    }
                })
                .collect(Collectors.joining(", "));
        return MethodSpec.methodBuilder("withBinaryFor%s".formatted(capitalize(ve.getSimpleName())))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterSpec.builder(entityTypeName(), "entity").build())
                .addParameter(ParameterSpec.builder(QuiNimbusBinarystore.EMBEDDABLE_BINARY, ve.getSimpleName())
                        .build())
                .addCode("return new $T($L);", entityTypeName(), constructorParams)
                .returns(entityTypeName())
                .build();
    }

    MethodSpec createBinaryListWither(MagicVariableElement ve) {
        var binaryName = IDs.toSingular(ve.getSimpleName());
        var constructorParams = this.recordElement
                .findFields()
                .map(field -> {
                    if (field.getSimpleName().equals(ve.getSimpleName())) {
                        return "list";
                    } else {
                        return "entity.%s()".formatted(field.getSimpleName());
                    }
                })
                .collect(Collectors.joining(", "));
        return MethodSpec.methodBuilder("withBinaryFor%s".formatted(capitalize(ve.getSimpleName())))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterSpec.builder(entityTypeName(), "entity").build())
                .addParameter(ParameterSpec.builder(ClassName.INT, "index").build())
                .addParameter(ParameterSpec.builder(QuiNimbusBinarystore.EMBEDDABLE_BINARY, binaryName)
                        .build())
                .addStatement("var list = entity.$L()", ve.getSimpleName())
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if (index > list.size())")
                        .addStatement(
                                "throw new $T($T.BINARY_LIST_ENTRY_OUT_OF_BOUNDS_MESSAGE.formatted(index, \"$L\", list.size()))",
                                Jakarta.RS_WEBAPPLICATIONEXCEPTION,
                                QuiNimbusRest.MESSAGES,
                                ve.getSimpleName())
                        .endControlFlow()
                        .beginControlFlow("if (index == list.size())")
                        .addStatement("list.addLast($L)", binaryName)
                        .nextControlFlow("else")
                        .addStatement("list.set(index, $L)", binaryName)
                        .endControlFlow()
                        .build())
                .addCode("return new $T($L);", entityTypeName(), constructorParams)
                .returns(entityTypeName())
                .build();
    }
}
