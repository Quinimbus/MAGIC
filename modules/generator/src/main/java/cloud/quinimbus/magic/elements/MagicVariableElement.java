package cloud.quinimbus.magic.elements;

import cloud.quinimbus.common.tools.NamedType;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

public class MagicVariableElement implements NamedType {
    
    private final VariableElement element;
    private final ProcessingEnvironment processingEnvironment;

    public MagicVariableElement(VariableElement element, ProcessingEnvironment processingEnvironment) {
        this.element = element;
        this.processingEnvironment = processingEnvironment;
    }

    public VariableElement getElement() {
        return this.element;
    }
    
    @Override
    public String getSimpleName() {
        return this.element.getSimpleName().toString();
    }
    
    public String getPackageName() {
        return this.processingEnvironment.getElementUtils().getPackageOf(this.element).toString();
    }
    
    public boolean isAnnotatedWith(String annotationName) {
        return this.element.getAnnotationMirrors().stream()
                        .map(a -> new MagicAnnotationElement(a, this.processingEnvironment))
                        .anyMatch(a -> a.getName().equals(annotationName));
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
