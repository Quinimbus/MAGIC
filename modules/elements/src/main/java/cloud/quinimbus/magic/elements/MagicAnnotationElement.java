package cloud.quinimbus.magic.elements;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
                .map(e -> (T)
                        switch (e.getValue()) {
                            case DeclaredType type ->
                                new MagicClassElement(
                                        (TypeElement) type.asElement(), this.elementUtils, this.typeUtils);
                            case AnnotationMirror mirror ->
                                new MagicAnnotationElement(mirror, this.elementUtils, this.typeUtils);
                            case VariableElement variableElement ->
                                new MagicVariableElement(variableElement, this.elementUtils, this.typeUtils);
                            case List<?> list ->
                                list.stream()
                                        .map(le -> switch (le) {
                                            case AnnotationValue av -> av.getValue();
                                            default -> le;
                                        })
                                        .toList();
                            default -> e.getValue();
                        })
                .findFirst();
    }
}
