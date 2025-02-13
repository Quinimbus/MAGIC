package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class Jakarta {

    public static final ClassName INJECT = ClassName.get("jakarta.inject", "Inject");

    public static final ClassName APPLICATION_SCOPED = ClassName.get("jakarta.enterprise.context", "ApplicationScoped");

    public static final ClassName REQUEST_SCOPED = ClassName.get("jakarta.enterprise.context", "RequestScoped");

    public static final ClassName PRODUCES = ClassName.get("jakarta.enterprise.inject", "Produces");

    public static final ClassName RS_CONSUMES = ClassName.get("jakarta.ws.rs", "Consumes");

    public static final ClassName RS_CONTEXT = ClassName.get("jakarta.ws.rs.core", "Context");

    public static final ClassName RS_DELETE = ClassName.get("jakarta.ws.rs", "DELETE");

    public static final ClassName RS_ENTITY_PART = ClassName.get("jakarta.ws.rs.core", "EntityPart");

    public static final ClassName RS_GET = ClassName.get("jakarta.ws.rs", "GET");

    public static final ClassName RS_MEDIATYPE = ClassName.get("jakarta.ws.rs.core", "MediaType");

    public static final ClassName RS_PATH = ClassName.get("jakarta.ws.rs", "Path");

    public static final ClassName RS_PATH_PARAM = ClassName.get("jakarta.ws.rs", "PathParam");

    public static final ClassName RS_POST = ClassName.get("jakarta.ws.rs", "POST");

    public static final ClassName RS_PRODUCES = ClassName.get("jakarta.ws.rs", "Produces");

    public static final ClassName RS_PUT = ClassName.get("jakarta.ws.rs", "PUT");

    public static final ClassName RS_RESPONSE = ClassName.get("jakarta.ws.rs.core", "Response");

    public static final ClassName RS_URIINFO = ClassName.get("jakarta.ws.rs.core", "UriInfo");

    public static final ClassName RS_WEBAPPLICATIONEXCEPTION =
            ClassName.get("jakarta.ws.rs", "WebApplicationException");

    public static final ClassName SEC_PERMIT_ALL = ClassName.get("jakarta.annotation.security", "PermitAll");

    public static final ClassName SEC_ROLES_ALLOWED = ClassName.get("jakarta.annotation.security", "RolesAllowed");
}
