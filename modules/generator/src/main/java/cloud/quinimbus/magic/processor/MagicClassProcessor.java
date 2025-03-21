package cloud.quinimbus.magic.processor;

import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicExecutableElement;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public abstract class MagicClassProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
        var typeElementsByAnnotation = set.stream()
                .collect(Collectors.groupingBy(
                        a -> a,
                        Collectors.flatMapping(
                                a -> re.getElementsAnnotatedWith(a).stream()
                                        .filter(element -> element instanceof TypeElement)
                                        .map(element -> new MagicClassElement(
                                                (TypeElement) element,
                                                this.processingEnv.getElementUtils(),
                                                this.processingEnv.getTypeUtils())),
                                Collectors.toSet())));
        var executableElementsByAnnotation = set.stream()
                .collect(Collectors.groupingBy(
                        a -> a,
                        Collectors.flatMapping(
                                a -> re.getElementsAnnotatedWith(a).stream()
                                        .filter(element -> element instanceof ExecutableElement)
                                        .map(element -> new MagicExecutableElement(
                                                (ExecutableElement) element,
                                                this.processingEnv.getElementUtils(),
                                                this.processingEnv.getTypeUtils())),
                                Collectors.toSet())));
        setup(re);
        set.forEach(annotation -> beforeProcessAll(
                annotation, typeElementsByAnnotation.get(annotation), executableElementsByAnnotation.get(annotation)));
        set.forEach(annotation ->
                typeElementsByAnnotation.get(annotation).forEach(element -> process(annotation, element)));
        set.forEach(annotation -> afterProcessAll(
                annotation, typeElementsByAnnotation.get(annotation), executableElementsByAnnotation.get(annotation)));
        return false;
    }

    public abstract void setup(RoundEnvironment re);

    public abstract void beforeProcessAll(
            TypeElement annotation,
            Set<MagicClassElement> typeElements,
            Set<MagicExecutableElement> executableElements);

    public abstract void process(TypeElement annotation, MagicClassElement element);

    public abstract void afterProcessAll(
            TypeElement annotation,
            Set<MagicClassElement> typeElements,
            Set<MagicExecutableElement> executableElements);

    public void writeTypeFile(MagicTypeSpec type) throws IOException {
        this.writeTypeFile(type.getPackageName(), type.getTypeSpec());
    }

    public void writeTypeFile(String packageName, TypeSpec type) throws IOException {
        var file = JavaFile.builder(packageName, type).skipJavaLangImports(true).build();
        file.writeTo(processingEnv.getFiler());
    }
}
