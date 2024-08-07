package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.elements.MagicAnnotationElement;
import cloud.quinimbus.magic.elements.MagicClassElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import javax.lang.model.type.TypeMirror;

public abstract class RecordEntityBasedGenerator {

    final MagicClassElement recordElement;
    final String name;
    final String packageName;
    final String idFieldName;
    final TypeMirror idType;
    final boolean idGenerated;
    final MagicClassElement owningType;
    private String ownerField;

    RecordEntityBasedGenerator(MagicClassElement recordElement) {
        this.recordElement = recordElement;
        this.name = recordElement.getSimpleName();
        this.packageName = recordElement.getPackageName();
        var idElement = recordElement
                .findFieldsAnnotatedWith("cloud.quinimbus.persistence.api.annotation.EntityIdField")
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find EntityIdField on any record field of record " + name));
        this.idGenerated = recordElement
                .findFieldsAnnotatedWith("cloud.quinimbus.persistence.api.annotation.EntityIdField")
                .findFirst()
                .get()
                .findAnnotation("cloud.quinimbus.persistence.api.annotation.EntityIdField")
                .flatMap(a -> a.<MagicAnnotationElement>getElementValue("generate"))
                .filter(a -> a.<Boolean>getElementValue("generate").orElse(false))
                .isPresent();
        this.idFieldName = idElement.getSimpleName();
        this.idType = idElement.getElement().asType();
        var ownerAnnotation = recordElement.findAnnotation(QuiNimbusCommon.OWNER_ANNOTATION_NAME);
        this.owningType = ownerAnnotation
                .flatMap(a -> a.getElementValue("owningEntity").map(MagicClassElement.class::cast))
                .orElse(null);
        this.ownerField = ownerAnnotation
                .flatMap(a -> a.getElementValue("field").map(String.class::cast))
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
        return ClassName.get(owningType.getElement().asType());
    }

    String ownerField() {
        return ownerField;
    }
}
