package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import static cloud.quinimbus.magic.util.Strings.capitalize;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
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
        return this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .map(e -> CodeBlock.of(
                        "super.addBinaryWither(\"$L\", this::withBinaryFor$L);",
                        e.getSimpleName(),
                        capitalize(e.getSimpleName())))
                .collect(CodeBlock.joining(";\n"));
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
}
