package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class QuiNimbusCommon {

    public static final String OWNER_ANNOTATION_NAME = "cloud.quinimbus.common.annotations.modelling.Owner";

    public static final ClassName OWNER_ANNOTATION =
            ClassName.get("cloud.quinimbus.common.annotations.modelling", "Owner");

    public static final String NAMING_ANNOTATION_NAME = "cloud.quinimbus.common.annotations.modelling.Naming";

    public static final String REFERENCES_ANNOTATION_NAME = "cloud.quinimbus.common.annotations.modelling.References";

    public static final String SEARCHABLE_ANNOTATION_NAME = "cloud.quinimbus.common.annotations.modelling.Searchable";
}
