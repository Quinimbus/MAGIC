package cloud.quinimbus.magic.elements;

import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class MagicClassElement extends AbstractMagicElementWrapper<TypeElement> {

    private final TypeElement element;
    private final ProcessingEnvironment processingEnvironment;

    public MagicClassElement(TypeElement element, ProcessingEnvironment processingEnvironment) {
        super(element, processingEnvironment);
        this.element = element;
        this.processingEnvironment = processingEnvironment;
    }
    
    public String getPackageName() {
        return this.processingEnvironment.getElementUtils().getPackageOf(this.element).toString();
    }

    public Stream<MagicVariableElement> findFields() {
        return this.element.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD))
                .map(e -> (VariableElement) e)
                .map(e -> new MagicVariableElement(e, this.processingEnvironment));
    }

    public Stream<MagicVariableElement> findFieldsAnnotatedWith(String annotationName) {
        return this.findFields()
                .filter(e -> e.isAnnotatedWith(annotationName));
    }
}
