package cloud.quinimbus.magic.spec;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class MagicTypeSpec {

    private final TypeSpec typeSpec;
    private final String packageName;

    public MagicTypeSpec(TypeSpec typeSpec, String packageName) {
        this.typeSpec = typeSpec;
        this.packageName = packageName;
    }

    public TypeName toTypeName() {
        return ClassName.get(this.packageName, this.typeSpec.name);
    }

    public String getPackageName() {
        return this.packageName;
    }

    public TypeSpec getTypeSpec() {
        return this.typeSpec;
    }
}
