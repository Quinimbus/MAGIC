package cloud.quinimbus.magic.elements;

import java.util.Map;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class MagicAnnotationElement {

    private final AnnotationMirror annotationMirror;
    private final ProcessingEnvironment processingEnvironment;

    public MagicAnnotationElement(AnnotationMirror annotationMirror, ProcessingEnvironment processingEnvironment) {
        this.annotationMirror = annotationMirror;
        this.processingEnvironment = processingEnvironment;
    }

    public AnnotationMirror getAnnotationMirror() {
        return this.annotationMirror;
    }

    public String getSimpleName() {
        return this.annotationMirror.getAnnotationType().asElement().getSimpleName().toString();
    }

    public String getPackageName() {
        return this.processingEnvironment.getElementUtils()
                .getPackageOf(this.annotationMirror.getAnnotationType().asElement()).toString();
    }
    
    public String getName() {
        return "%s.%s".formatted(this.getPackageName(), this.getSimpleName());
    }
    
    public <T> Optional<T> getElementValue(String elementName) {
        return this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(this.annotationMirror).entrySet().stream()
                .filter(e -> e.getKey().getSimpleName().toString().equals(elementName))
                .map(Map.Entry::getValue)
                .map(e -> {
                    if (e.getValue() instanceof DeclaredType type) {
                        return (T) new MagicClassElement((TypeElement) type.asElement(), processingEnvironment);
                    }
                    return (T)e.getValue();
                })
                .findFirst();
    }
}
