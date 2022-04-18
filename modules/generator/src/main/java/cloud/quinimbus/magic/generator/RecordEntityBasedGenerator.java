package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.elements.MagicClassElement;
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
                .map(e -> e.asType())
                .orElseThrow(() ->
                        new IllegalArgumentException("Cannot find EntityIdField on any record field of record " + name));
        
    }
}
