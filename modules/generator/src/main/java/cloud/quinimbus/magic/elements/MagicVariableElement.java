package cloud.quinimbus.magic.elements;

import com.squareup.javapoet.TypeName;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

public class MagicVariableElement extends AbstractMagicElementWrapper<VariableElement> {
    
    private final VariableElement element;

    public MagicVariableElement(VariableElement element, ProcessingEnvironment processingEnvironment) {
        super(element, processingEnvironment);
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
    
    public TypeName getType() {
        return TypeName.get(this.element.asType());
    }
}
