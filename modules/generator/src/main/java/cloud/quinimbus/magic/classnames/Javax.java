package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class Javax {
    
    public static final ClassName INJECT = ClassName.get("javax.inject", "Inject");
    
    public static final ClassName REQUEST_SCOPED = ClassName.get("javax.enterprise.context", "RequestScoped");
    
    public static final ClassName RS_GET = ClassName.get("javax.ws.rs", "GET");
    
    public static final ClassName RS_MEDIATYPE = ClassName.get("javax.ws.rs.core", "MediaType");
    
    public static final ClassName RS_PATH = ClassName.get("javax.ws.rs", "Path");
    
    public static final ClassName RS_PATH_PARAM = ClassName.get("javax.ws.rs", "PathParam");
    
    public static final ClassName RS_PRODUCES = ClassName.get("javax.ws.rs", "Produces");
    
    public static final ClassName RS_RESPONSE = ClassName.get("javax.ws.rs.core", "Response");
}
