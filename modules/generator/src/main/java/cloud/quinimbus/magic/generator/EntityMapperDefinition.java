package cloud.quinimbus.magic.generator;

import com.squareup.javapoet.TypeName;
import java.util.List;

public record EntityMapperDefinition(TypeName type, String name, List<Method> methods) {

    public static record Method(String returnType, String methodName, String mapperName) {}
}
