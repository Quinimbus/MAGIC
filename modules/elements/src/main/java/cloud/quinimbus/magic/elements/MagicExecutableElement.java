package cloud.quinimbus.magic.elements;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.stream.Stream;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class MagicExecutableElement extends AbstractMagicElementWrapper<ExecutableElement> {

    private final ExecutableElement element;

    public MagicExecutableElement(ExecutableElement element, Elements elementUtils, Types typeUtils) {
        super(element, elementUtils, typeUtils);
        this.element = element;
    }

    public Stream<MagicVariableElement> parameters() {
        return this.element.getParameters().stream()
                .map(ve -> new MagicVariableElement(ve, this.elementUtils(), this.typeUtils()));
    }

    public int parameterCount() {
        return this.element.getParameters().size();
    }

    public MagicClassElement returnType() {
        return new MagicClassElement(
                (TypeElement) this.typeUtils().asElement(this.element.getReturnType()),
                this.elementUtils(),
                this.typeUtils());
    }

    public boolean returns(TypeName typeName) {
        if (this.element.getReturnType() instanceof DeclaredType type) {
            return ClassName.get(type).equals(typeName);
        }
        return false;
    }

    public MagicClassElement enclosingElement() {
        return new MagicClassElement(
                (TypeElement) this.element.getEnclosingElement(), this.elementUtils(), this.typeUtils());
    }
}
