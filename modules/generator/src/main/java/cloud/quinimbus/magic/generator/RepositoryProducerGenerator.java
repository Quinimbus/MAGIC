package cloud.quinimbus.magic.generator;

import cloud.quinimbus.magic.classnames.Jakarta;
import cloud.quinimbus.magic.elements.MagicClassElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Set;
import javax.lang.model.element.Modifier;

public class RepositoryProducerGenerator {

    final Set<MagicClassElement> recordElements;

    public RepositoryProducerGenerator(Set<MagicClassElement> recordElements) {
        this.recordElements = recordElements;
    }

    public TypeSpec generateRepositoryProducerType() {
        var producerTypeBuilder = TypeSpec.classBuilder("RepositoryProducer")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get("cloud.quinimbus.persistence.cdi", "AbstractRecordRepositoryProducer"))
                .addAnnotation(Jakarta.APPLICATION_SCOPED)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Jakarta.INJECT)
                        .addParameter(ParameterSpec.builder(
                                        ClassName.get("cloud.quinimbus.persistence.api", "PersistenceContext"),
                                        "persistenceContext")
                                .build())
                        .addCode("super(persistenceContext);")
                        .build());
        recordElements.forEach(e -> {
            var name = e.getSimpleName();
            producerTypeBuilder.addMethod(MethodSpec.methodBuilder(name + "Repository")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Jakarta.PRODUCES)
                    .addCode("return this.getRepository($LRepository.class);", name)
                    .returns(ClassName.get(e.getPackageName(), name + "Repository"))
                    .build());
        });
        return producerTypeBuilder.build();
    }
}
