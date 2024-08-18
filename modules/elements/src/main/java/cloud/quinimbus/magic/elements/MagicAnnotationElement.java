package cloud.quinimbus.magic.elements;

import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class MagicAnnotationElement {

    private final AnnotationMirror annotationMirror;
    private final Elements elementUtils;
    private final Types typeUtils;

    public MagicAnnotationElement(AnnotationMirror annotationMirror, Elements elementUtils, Types typeUtils) {
        this.annotationMirror = annotationMirror;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    public AnnotationMirror getAnnotationMirror() {
        return this.annotationMirror;
    }

    public String getSimpleName() {
        return this.annotationMirror
                .getAnnotationType()
                .asElement()
                .getSimpleName()
                .toString();
    }

    public String getPackageName() {
        return this.elementUtils
                .getPackageOf(this.annotationMirror.getAnnotationType().asElement())
                .toString();
    }

    public String getName() {
        return "%s.%s".formatted(this.getPackageName(), this.getSimpleName());
    }

    public <T> Optional<T> getElementValue(String elementName) {
        return this.elementUtils.getElementValuesWithDefaults(this.annotationMirror).entrySet().stream()
                .filter(e -> e.getKey().getSimpleName().toString().equals(elementName))
                .map(Map.Entry::getValue)
                .map(e -> {
                    if (e.getValue() instanceof DeclaredType type) {
                        return (T) new MagicClassElement(
                                (TypeElement) type.asElement(), this.elementUtils, this.typeUtils);
                    }
                    if (e.getValue() instanceof AnnotationMirror mirror) {
                        return (T) new MagicAnnotationElement(mirror, this.elementUtils, this.typeUtils);
                    }
                    return (T) e.getValue();
                })
                .findFirst();
    }
}
