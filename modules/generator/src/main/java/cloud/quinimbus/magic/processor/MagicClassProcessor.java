package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.elements.MagicClassElement;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

public abstract class MagicClassProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
        set.forEach(annotation -> {
            var elements = re.getElementsAnnotatedWith(annotation)
                    .stream()
                    .filter(element -> element instanceof TypeElement)
                    .map(element -> new MagicClassElement((TypeElement) element, this.processingEnv))
                    .collect(Collectors.toSet());
            elements.forEach(element -> process(annotation, element));
            afterProcessAll(annotation, elements);
        });
        return false;
    }

    public abstract void process(TypeElement annotation, MagicClassElement element);

    public abstract void afterProcessAll(TypeElement annotation, Set<MagicClassElement> elements);

    public void writeTypeFile(String packageName, TypeSpec type) throws IOException {
        var file = JavaFile.builder(packageName, type)
                .skipJavaLangImports(true)
                .build();
        file.writeTo(processingEnv.getFiler());
    }
}
