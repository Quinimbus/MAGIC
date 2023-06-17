package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.elements.MagicClassElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.Locale;
import javax.lang.model.type.TypeMirror;

public abstract class RecordEntityBasedGenerator {
    
    final MagicClassElement recordElement;
    final String name;
    final String packageName;
    final TypeMirror idType;
    final TypeMirror owningType;

    RecordEntityBasedGenerator(MagicClassElement recordElement) {
        this.recordElement = recordElement;
        this.name = recordElement.getSimpleName();
        this.packageName = recordElement.getPackageName();
        this.idType = recordElement.findFieldsAnnotatedWith("cloud.quinimbus.persistence.api.annotation.EntityIdField")
                .findFirst()
                .map(e -> e.getElement().asType())
                .orElseThrow(() ->
                        new IllegalArgumentException("Cannot find EntityIdField on any record field of record " + name));
        this.owningType = recordElement
                .findAnnotation(QuiNimbusCommon.OWNER_ANNOTATION_NAME)
                .flatMap(a -> a.getElementValue("owningEntity").map(MagicClassElement.class::cast))
                .map(e -> e.getElement().asType())
                .orElse(null);
    }
    
    static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase(Locale.US).concat(str.substring(1));
    }
    
    static String uncapitalize(String str) {
        return str.substring(0, 1).toLowerCase(Locale.US).concat(str.substring(1));
    }
    
    public String relativizeToName(String str) {
        if (str.startsWith(name)) {
            return str.substring(name.length());
        }
        return str;
    }
    
    TypeName entityTypeName() {
        return ClassName.get(packageName, name);
    }
    
    TypeName idTypeName() {
        return ClassName.get(idType);
    }
    
    boolean weak() {
        return owningType != null;
    }
    
    TypeName owningTypeName() {
        return ClassName.get(owningType);
    }
}
