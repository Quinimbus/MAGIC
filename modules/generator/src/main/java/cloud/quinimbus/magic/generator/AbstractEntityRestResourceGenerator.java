package cloud.quinimbus.magic.generator;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Jakarta;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.Microprofile;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import static cloud.quinimbus.magic.util.Strings.capitalize;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
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

    AnnotationSpec path(String path) {
        return AnnotationSpec.builder(Jakarta.RS_PATH)
                .addMember("value", CodeBlock.of("$S", path))
                .build();
    }

    AnnotationSpec producesJson() {
        return AnnotationSpec.builder(Jakarta.RS_PRODUCES)
                .addMember(
                        "value",
                        CodeBlock.builder()
                                .add("$T.APPLICATION_JSON", Jakarta.RS_MEDIATYPE)
                                .build())
                .build();
    }

    AnnotationSpec consumesJson() {
        return AnnotationSpec.builder(Jakarta.RS_CONSUMES)
                .addMember(
                        "value",
                        CodeBlock.builder()
                                .add("$T.APPLICATION_JSON", Jakarta.RS_MEDIATYPE)
                                .build())
                .build();
    }

    AnnotationSpec consumesMultipart() {
        return AnnotationSpec.builder(Jakarta.RS_CONSUMES)
                .addMember(
                        "value",
                        CodeBlock.builder()
                                .add("$T.MULTIPART_FORM_DATA", Jakarta.RS_MEDIATYPE)
                                .build())
                .build();
    }

    ParameterSpec injectUriInfo() {
        return ParameterSpec.builder(Jakarta.RS_URIINFO, "uriInfo")
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_CONTEXT).build())
                .build();
    }

    AnnotationSpec tagByType() {
        return AnnotationSpec.builder(Microprofile.OPENAPI_TAG)
                .addMember("name", "$S", capitalize(IDs.toPlural(Records.idFromType(recordElement))))
                .build();
    }

    AnnotationSpec operation(String operationId, String summary) {
        return AnnotationSpec.builder(Microprofile.OPENAPI_OPERATION)
                .addMember("operationId", "$S", operationId)
                .addMember("summary", "$S", summary)
                .build();
    }

    AnnotationSpec emptyResponse(String responseCode) {
        return AnnotationSpec.builder(Microprofile.OPENAPI_API_RESPONSE)
                .addMember("responseCode", "$S", responseCode)
                .build();
    }

    AnnotationSpec response(String responseCode, AnnotationSpec schema) {
        return AnnotationSpec.builder(Microprofile.OPENAPI_API_RESPONSE)
                .addMember("responseCode", "$S", responseCode)
                .addMember(
                        "content",
                        "$L",
                        AnnotationSpec.builder(Microprofile.OPENAPI_CONTENT)
                                .addMember("schema", "$L", schema)
                                .build())
                .build();
    }

    AnnotationSpec arraySchema(TypeName type) {
        return AnnotationSpec.builder(Microprofile.OPENAPI_SCHEMA)
                .addMember("implementation", "$T.class", type)
                .addMember("type", "$T.ARRAY", Microprofile.OPENAPI_SCHEMA_TYPE)
                .build();
    }

    AnnotationSpec objectSchema(TypeName type) {
        return AnnotationSpec.builder(Microprofile.OPENAPI_SCHEMA)
                .addMember("implementation", "$T.class", type)
                .addMember("type", "$T.OBJECT", Microprofile.OPENAPI_SCHEMA_TYPE)
                .build();
    }

    AnnotationSpec stringSchema() {
        return AnnotationSpec.builder(Microprofile.OPENAPI_SCHEMA)
                .addMember("type", "$T.STRING", Microprofile.OPENAPI_SCHEMA_TYPE)
                .build();
    }

    AnnotationSpec requestBody(AnnotationSpec schema) {
        return AnnotationSpec.builder(Microprofile.OPENAPI_REQUEST_BODY)
                .addMember(
                        "content",
                        "$L",
                        AnnotationSpec.builder(Microprofile.OPENAPI_CONTENT)
                                .addMember("schema", "$L", schema)
                                .build())
                .build();
    }

    AnnotationSpec parametersSchema(AnnotationSpec... parameters) {
        var anno = AnnotationSpec.builder(Microprofile.OPENAPI_SCHEMA)
                .addMember("type", "$T.OBJECT", Microprofile.OPENAPI_SCHEMA_TYPE);
        for (AnnotationSpec parameter : parameters) {
            anno.addMember("properties", "$L", parameter);
        }
        return anno.build();
    }

    AnnotationSpec parameter(String name, String title) {
        return AnnotationSpec.builder(Microprofile.OPENAPI_SCHEMA_PROPERTY)
                .addMember("name", "$S", name)
                .addMember("title", "$S", title)
                .build();
    }

    AnnotationSpec pathParameter(String name, String description) {
        return AnnotationSpec.builder(Microprofile.OPENAPI_PARAMETER)
                .addMember("name", "$S", name)
                .addMember("description", "$S", description)
                .addMember("in", "$T.PATH", Microprofile.OPENAPI_PARAMETER_IN)
                .addMember(
                        "content",
                        "$L",
                        AnnotationSpec.builder(Microprofile.OPENAPI_CONTENT)
                                .addMember("schema", "$L", stringSchema())
                                .build())
                .build();
    }
}