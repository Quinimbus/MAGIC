package cloud.quinimbus.magic.elements;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class MagicVariableElement extends AbstractMagicElementWrapper<VariableElement> {

    private final VariableElement element;

    public MagicVariableElement(VariableElement element, Elements elementUtil, Types typeUtils) {
        super(element, elementUtil, typeUtils);
        this.element = element;
    }

    public boolean isClass(Class cls) {
        if (this.element.asType() instanceof DeclaredType dtype) {
            if (dtype.asElement() instanceof TypeElement te) {
                return cls.getName().equals(te.getQualifiedName().toString());
            }
        }
        return false;
    }
}
