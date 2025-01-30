package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class Microprofile {

    public static final ClassName OPENAPI_API_RESPONSE =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.responses", "APIResponse");

    public static final ClassName OPENAPI_CONTENT =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.media", "Content");

    public static final ClassName OPENAPI_OPERATION =
            ClassName.get("org.eclipse.microprofile.openapi.annotations", "Operation");

    public static final ClassName OPENAPI_PARAMETER =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.parameters", "Parameter");

    public static final ClassName OPENAPI_PARAMETER_IN =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.enums", "ParameterIn");

    public static final ClassName OPENAPI_REQUEST_BODY =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.parameters", "RequestBody");

    public static final ClassName OPENAPI_SCHEMA =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.media", "Schema");

    public static final ClassName OPENAPI_SCHEMA_PROPERTY =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.media", "SchemaProperty");

    public static final ClassName OPENAPI_SCHEMA_TYPE =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.enums", "SchemaType");

    public static final ClassName OPENAPI_TAG =
            ClassName.get("org.eclipse.microprofile.openapi.annotations.tags", "Tag");
}
