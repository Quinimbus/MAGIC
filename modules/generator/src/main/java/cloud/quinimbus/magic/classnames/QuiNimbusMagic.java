package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class QuiNimbusMagic {
    public static final String ADD_MAPPER_TO_GENERATED_REST_ENDPOINT_NAME =
            "cloud.quinimbus.magic.annotations.AddMapperToGeneratedRestEndpoint";
    public static final String GENERATE_ADMIN_LIST_VIEW_NAME =
            "cloud.quinimbus.magic.annotations.GenerateAdminListView";
    public static final String GENERATE_REST_ENDPOINTS = "cloud.quinimbus.magic.annotations.GenerateRestEndpoints";

    public static final ClassName ADD_MAPPER_TO_GENERATED_REST_ENDPOINT =
            ClassName.get("cloud.quinimbus.magic.annotations", "AddMapperToGeneratedRestEndpoint");
}
