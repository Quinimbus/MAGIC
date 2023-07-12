package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.elements.MagicClassElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
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
