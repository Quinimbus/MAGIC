package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class Quarkus {

    public static final ClassName QUARKUS = ClassName.get("io.quarkus.runtime", "Quarkus");

    public static final ClassName QUARKUS_MAIN = ClassName.get("io.quarkus.runtime.annotations", "QuarkusMain");
}
