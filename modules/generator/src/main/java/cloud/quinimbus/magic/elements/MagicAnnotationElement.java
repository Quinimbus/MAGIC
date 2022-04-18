package cloud.quinimbus.magic.elements;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

public class MagicAnnotationElement {

    private final AnnotationMirror annotationMirror;
    private final ProcessingEnvironment processingEnvironment;

    public MagicAnnotationElement(AnnotationMirror annotationMirror, ProcessingEnvironment processingEnvironment) {
        this.annotationMirror = annotationMirror;
        this.processingEnvironment = processingEnvironment;
    }

    public AnnotationMirror getAnnotationMirror() {
        return this.annotationMirror;
    }

    public String getSimpleName() {
        return this.annotationMirror.getAnnotationType().asElement().getSimpleName().toString();
    }

    public String getPackageName() {
        return this.processingEnvironment.getElementUtils()
                .getPackageOf(this.annotationMirror.getAnnotationType().asElement()).toString();
    }
    
    public String getName() {
        return "%s.%s".formatted(this.getPackageName(), this.getSimpleName());
    }
}
