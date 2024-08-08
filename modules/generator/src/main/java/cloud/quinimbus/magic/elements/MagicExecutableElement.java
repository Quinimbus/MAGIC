package cloud.quinimbus.magic.elements;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class MagicExecutableElement extends AbstractMagicElementWrapper<ExecutableElement> {

    private final ExecutableElement element;
    private final ProcessingEnvironment processingEnvironment;

    public MagicExecutableElement(ExecutableElement element, ProcessingEnvironment processingEnvironment) {
        super(element, processingEnvironment);
        this.element = element;
        this.processingEnvironment = processingEnvironment;
    }

    public Stream<MagicVariableElement> parameters() {
        return this.element.getParameters().stream()
                .map(ve -> new MagicVariableElement(ve, this.processingEnvironment));
    }

    public int parameterCount() {
        return this.element.getParameters().size();
    }

    public MagicClassElement returnType() {
        return new MagicClassElement(
                (TypeElement) this.processingEnvironment.getTypeUtils().asElement(this.element.getReturnType()),
                processingEnvironment);
    }

    public boolean returns(TypeName typeName) {
        if (this.element.getReturnType() instanceof DeclaredType type) {
            return ClassName.get(type).equals(typeName);
        }
        return false;
    }

    public MagicClassElement enclosingElement() {
        return new MagicClassElement((TypeElement) this.element.getEnclosingElement(), this.processingEnvironment);
    }
}
