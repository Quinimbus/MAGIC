package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class Quarkus {

    public static final ClassName QUARKUS = ClassName.get("io.quarkus.runtime", "Quarkus");

    public static final ClassName QUARKUS_MAIN = ClassName.get("io.quarkus.runtime.annotations", "QuarkusMain");

    public static final ClassName FILE_UPLOAD = ClassName.get("org.jboss.resteasy.reactive.multipart", "FileUpload");

    public static final ClassName REST_FORM = ClassName.get("org.jboss.resteasy.reactive", "RestForm");

    public static final ClassName RESTEASY_REACTIVE_REQUEST_CONTEXT =
            ClassName.get("org.jboss.resteasy.reactive.server.core", "ResteasyReactiveRequestContext");
}
