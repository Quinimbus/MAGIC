package cloud.quinimbus.magic.elements;

import cloud.quinimbus.common.tools.NamedType;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

public abstract class AbstractMagicElementWrapper<E extends Element> implements NamedType {

    private final ProcessingEnvironment processingEnvironment;

    private final E element;

    public AbstractMagicElementWrapper(E element, ProcessingEnvironment processingEnvironment) {
        this.element = element;
        this.processingEnvironment = processingEnvironment;
    }

    public E getElement() {
        return this.element;
    }

    @Override
    public String getSimpleName() {
        return this.element.getSimpleName().toString();
    }

    public boolean isAnnotatedWith(String annotationName) {
        return this.element.getAnnotationMirrors().stream()
                .map(a -> new MagicAnnotationElement(a, this.processingEnvironment))
                .anyMatch(a -> a.getName().equals(annotationName));
    }

    public Optional<MagicAnnotationElement> findAnnotation(String annotationName) {
        return this.element.getAnnotationMirrors().stream()
                .map(a -> new MagicAnnotationElement(a, this.processingEnvironment))
                .filter(a -> a.getName().equals(annotationName))
                .findAny();
    }

    @Override
    public String toString() {
        return "%s(%s)".formatted(this.getClass().getSimpleName(), this.element);
    }
}
