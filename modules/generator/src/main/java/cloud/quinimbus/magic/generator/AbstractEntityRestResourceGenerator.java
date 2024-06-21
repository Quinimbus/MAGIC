package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.elements.MagicClassElement;
import com.squareup.javapoet.ClassName;

public class AbstractEntityRestResourceGenerator extends RecordEntityBasedGenerator {

    public AbstractEntityRestResourceGenerator(MagicClassElement recordElement) {
        super(recordElement);
    }

    ClassName repository() {
        return ClassName.get(packageName, name + "Repository");
    }

    ClassName repository(MagicClassElement e) {
        return ClassName.get(e.getPackageName(), e.getSimpleName().concat("Repository"));
    }
}
