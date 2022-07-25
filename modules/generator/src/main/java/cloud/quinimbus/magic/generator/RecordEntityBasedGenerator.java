package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.elements.MagicClassElement;
import java.util.Locale;
import javax.lang.model.type.TypeMirror;

public abstract class RecordEntityBasedGenerator {
    
    final MagicClassElement recordElement;
    final String name;
    final String packageName;
    final TypeMirror idType;

    RecordEntityBasedGenerator(MagicClassElement recordElement) {
        this.recordElement = recordElement;
        this.name = recordElement.getSimpleName();
        this.packageName = recordElement.getPackageName();
        this.idType = recordElement.findFieldsAnnotatedWith("cloud.quinimbus.persistence.api.annotation.EntityIdField")
                .findFirst()
                .map(e -> e.getElement().asType())
                .orElseThrow(() ->
                        new IllegalArgumentException("Cannot find EntityIdField on any record field of record " + name));
        
    }
    
    static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase(Locale.US).concat(str.substring(1));
    }
}
