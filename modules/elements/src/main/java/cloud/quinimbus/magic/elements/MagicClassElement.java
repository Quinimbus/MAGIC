package cloud.quinimbus.magic.elements;

import com.squareup.javapoet.TypeName;
import java.util.Objects;
import java.util.stream.Stream;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class MagicClassElement extends AbstractMagicElementWrapper<TypeElement> {

    private final TypeElement element;

    public MagicClassElement(TypeElement element, Elements elementUtils, Types typeUtils) {
        super(element, elementUtils, typeUtils);
        this.element = element;
    }

    public String getPackageName() {
        return this.elementUtils().getPackageOf(this.element).toString();
    }

    public Stream<MagicVariableElement> findFields() {
        return this.element.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD))
                .map(e -> (VariableElement) e)
                .map(e -> new MagicVariableElement(e, this.elementUtils(), this.typeUtils()));
    }

    public Stream<MagicVariableElement> findFieldsAnnotatedWith(String annotationName) {
        return this.findFields().filter(e -> e.isAnnotatedWith(annotationName));
    }

    public Stream<MagicVariableElement> findFieldsOfType(TypeName type) {
        return this.findFields().filter(ve -> ve.getType().equals(type));
    }

    public Stream<MagicExecutableElement> findMethods() {
        return this.element.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.METHOD))
                .map(e -> (ExecutableElement) e)
                .map(e -> new MagicExecutableElement(e, this.elementUtils(), this.typeUtils()));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MagicClassElement otherElement) {
            return element.equals(otherElement.element);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.element);
        return hash;
    }
}
