package cloud.quinimbus.magic.elements;

import cloud.quinimbus.common.tools.NamedType;
import com.squareup.javapoet.TypeName;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class AbstractMagicElementWrapper<E extends Element> implements NamedType {

    private final E element;
    private final Elements elementUtils;
    private final Types typeUtils;

    public AbstractMagicElementWrapper(E element, Elements elementUtils, javax.lang.model.util.Types typeUtils) {
        if (element == null) {
            throw new IllegalArgumentException("element may not be null");
        }
        this.element = element;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    public E getElement() {
        return this.element;
    }

    Elements elementUtils() {
        return this.elementUtils;
    }

    Types typeUtils() {
        return this.typeUtils;
    }

    @Override
    public String getSimpleName() {
        return this.element.getSimpleName().toString();
    }

    public boolean isAnnotatedWith(String annotationName) {
        return this.element.getAnnotationMirrors().stream()
                .map(a -> new MagicAnnotationElement(a, this.elementUtils, this.typeUtils))
                .anyMatch(a -> a.getName().equals(annotationName));
    }

    public Optional<MagicAnnotationElement> findAnnotation(String annotationName) {
        return this.element.getAnnotationMirrors().stream()
                .map(a -> new MagicAnnotationElement(a, this.elementUtils, this.typeUtils))
                .filter(a -> a.getName().equals(annotationName))
                .findAny();
    }

    @Override
    public String toString() {
        return "%s(%s)".formatted(this.getClass().getSimpleName(), this.element);
    }

    public TypeName getType() {
        return TypeName.get(this.element.asType());
    }
}
