package cloud.quinimbus.magic.elements;

import cloud.quinimbus.common.tools.NamedType;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class MagicClassElement implements NamedType {

    private final TypeElement element;
    private final ProcessingEnvironment processingEnvironment;

    public MagicClassElement(TypeElement element, ProcessingEnvironment processingEnvironment) {
        this.element = element;
        this.processingEnvironment = processingEnvironment;
    }

    public TypeElement getElement() {
        return this.element;
    }
    
    @Override
    public String getSimpleName() {
        return this.element.getSimpleName().toString();
    }
    
    public String getPackageName() {
        return this.processingEnvironment.getElementUtils().getPackageOf(this.element).toString();
    }

    public Stream<VariableElement> findFields() {
        return this.element.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD))
                .map(e -> (VariableElement) e);
    }

    public Stream<VariableElement> findFieldsAnnotatedWith(String annotationName) {
        return this.findFields()
                .filter(e -> e.getAnnotationMirrors().stream()
                        .map(a -> new MagicAnnotationElement(a, this.processingEnvironment))
                        .anyMatch(a -> a.getName().equals(annotationName)));
    }
}
