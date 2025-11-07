package cloud.quinimbus.magic.generator;

import static cloud.quinimbus.magic.util.Strings.*;

import cloud.quinimbus.common.tools.IDs;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.magic.classnames.Jakarta;
import cloud.quinimbus.magic.classnames.Java;
import cloud.quinimbus.magic.classnames.Quarkus;
import cloud.quinimbus.magic.classnames.QuiNimbusBinarystore;
import cloud.quinimbus.magic.classnames.QuiNimbusCommon;
import cloud.quinimbus.magic.classnames.QuiNimbusRest;
import cloud.quinimbus.magic.elements.MagicClassElement;
import cloud.quinimbus.magic.elements.MagicVariableElement;
import cloud.quinimbus.magic.spec.MagicTypeSpec;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public class EntityAllRestResourceGenerator extends AbstractEntityRestResourceGenerator {

    private final List<EntityMapperDefinition> entityMappers;

    private final Set<RecordContextActionDefinition> recordContextActionDefinitions;

    private final boolean quarkusRestReactiveWorkaround;

    public EntityAllRestResourceGenerator(
            MagicClassElement recordElement,
            List<EntityMapperDefinition> entityMappers,
            Set<RecordContextActionDefinition> recordContextActionDefinitions,
            boolean quarkusRestReactiveWorkaround) {
        super(recordElement);
        this.entityMappers = entityMappers != null ? entityMappers : List.of();
        this.recordContextActionDefinitions =
                recordContextActionDefinitions != null ? recordContextActionDefinitions : Set.of();
        this.quarkusRestReactiveWorkaround = quarkusRestReactiveWorkaround;
    }

    public MagicTypeSpec generateAllResource() {
        var allResourceTypeBuilder = classBuilder(name + "AllResource")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superclass())
                .addAnnotation(tagByType())
                .addField(FieldSpec.builder(repository(), "repository", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(constructor())
                .addMethod(createGetAll(weak()))
                .addMethod(createGetAllIDs(weak()))
                .addMethod(createPostNew(weak()));
        if (!weak()) {
            allResourceTypeBuilder
                    .addAnnotation(Jakarta.REQUEST_SCOPED)
                    .addAnnotation(path(IDs.toPlural(Records.idFromType(recordElement))))
                    .addMethod(createPostNewByMultipart());
        }
        entityMappers.stream()
                .map(e -> FieldSpec.builder(e.type(), uncapitalize(e.name()), Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .forEach(allResourceTypeBuilder::addField);
        recordContextActionDefinitions.stream()
                .map(e -> FieldSpec.builder(
                                e.type(), uncapitalize(e.type().simpleName()), Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .forEach(allResourceTypeBuilder::addField);
        this.recordElement
                .findFieldsAnnotatedWith(QuiNimbusCommon.SEARCHABLE_ANNOTATION_NAME)
                .filter(ve -> ve.isClass(String.class))
                .forEach(ve -> allResourceTypeBuilder.addMethod(createByPropertyEndpoint(ve)));
        this.recordElement
                .findFieldsOfType(QuiNimbusBinarystore.EMBEDDABLE_BINARY)
                .forEach(ve -> allResourceTypeBuilder.addMethod(createBinaryWither(ve)));
        this.recordElement
                .findFieldsOfType(ParameterizedTypeName.get(Java.LIST, QuiNimbusBinarystore.EMBEDDABLE_BINARY))
                .forEach(ve -> allResourceTypeBuilder.addMethod(createBinaryListWither(ve)));
        entityMappers.stream()
                .flatMap(e -> e.methods().stream())
                .map(e -> createMappedAsMethod(e))
                .forEach(allResourceTypeBuilder::addMethod);
        recordContextActionDefinitions.stream()
                .map(e -> createActionMethod(e))
                .forEach(allResourceTypeBuilder::addMethod);
        return new MagicTypeSpec(allResourceTypeBuilder.build(), packageName);
    }

    private ParameterizedTypeName superclass() {
        if (weak()) {
            return ParameterizedTypeName.get(
                    QuiNimbusRest.ABSTRACT_WEAK_CRUD_ALL_RESOURCE, entityTypeName(), idTypeName(), owningTypeName());
        } else {
            return ParameterizedTypeName.get(QuiNimbusRest.ABSTRACT_CRUD_ALL_RESOURCE, entityTypeName(), idTypeName());
        }
    }

    private MethodSpec constructor() {
        var additionalParameters = Stream.concat(
                        entityMappers.stream().map(e -> ParameterSpec.builder(e.type(), uncapitalize(e.name()))
                                .build()),
                        recordContextActionDefinitions.stream().map(e -> ParameterSpec.builder(
                                        e.type(), uncapitalize(e.type().simpleName()))
                                .build()))
                .distinct()
                .toList();
        var constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        var code = CodeBlock.builder();
        if (weak()) {
            constructor
                    .addParameter(ParameterSpec.builder(
                                    ParameterizedTypeName.get(
                                            Java.FUNCTION,
                                            Jakarta.RS_URIINFO,
                                            ParameterizedTypeName.get(Java.OPTIONAL, owningTypeName())),
                                    "owner")
                            .build())
                    .addParameter(
                            ParameterSpec.builder(repository(), "repository").build())
                    .build();
            code.addStatement("super(owner, repository)");
            code.addStatement("this.repository = repository");
        } else {
            constructor
                    .addAnnotation(Jakarta.INJECT)
                    .addParameter(
                            ParameterSpec.builder(repository(), "repository").build())
                    .build();
            code.addStatement("super($T.class, repository)", entityTypeName());
            code.addStatement("this.repository = repository;");
            code.add(initBinaryWither());
        }
        additionalParameters.forEach(constructor::addParameter);
        additionalParameters.forEach(p -> code.addStatement("this.%s = %s".formatted(p.name, p.name)));
        return constructor.addCode(code.build()).build();
    }

    private MethodSpec createGetAll(boolean uriInfoParameter) {
        var spec = MethodSpec.methodBuilder("getAll")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(producesJson())
                .addAnnotation(secureCRUDEndpoint(CRUDType.READ))
                .addAnnotation(operation(
                        "GetAll%s".formatted(capitalize(IDs.toPlural(Records.idFromType(recordElement)))),
                        "Get all entries of type %s".formatted(name)))
                .addAnnotation(response("200", arraySchema(entityTypeName())))
                .returns(Jakarta.RS_RESPONSE);
        if (uriInfoParameter) {
            spec.addParameter(injectUriInfo()).addStatement("return super.getAll(uriInfo)");
        } else {
            spec.addStatement("return super.getAll()");
        }
        if (weak()) {
            spec.addAnnotation(ownerIdPathParameter());
        }
        return spec.build();
    }

    private MethodSpec createGetAllIDs(boolean uriInfoParameter) {
        var spec = MethodSpec.methodBuilder("getAllIDs")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(path("/as/ids"))
                .addAnnotation(producesJson())
                .addAnnotation(secureCRUDEndpoint(CRUDType.READ))
                .addAnnotation(operation(
                        "GetAll%sIDs".formatted(capitalize(Records.idFromType(recordElement))),
                        "Get ids of all entries of type %s".formatted(name)))
                .addAnnotation(response("200", arraySchema(idTypeName())))
                .returns(Jakarta.RS_RESPONSE);
        if (uriInfoParameter) {
            spec.addParameter(injectUriInfo()).addStatement("return super.getAllIDs(uriInfo)");
        } else {
            spec.addStatement("return super.getAllIDs()");
        }
        if (weak()) {
            spec.addAnnotation(ownerIdPathParameter());
        }
        return spec.build();
    }

    private MethodSpec createPostNew(boolean uriInfoParameter) {
        var spec = MethodSpec.methodBuilder("postNew")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_POST).build())
                .addAnnotation(consumesJson())
                .addAnnotation(secureCRUDEndpoint(CRUDType.CREATE))
                .addAnnotation(operation(
                        "PostNew%s".formatted(capitalize(IDs.toPlural(Records.idFromType(recordElement)))),
                        "Create a new entry of type %s".formatted(name)))
                .addAnnotation(emptyResponse("202"))
                .addParameter(ParameterSpec.builder(entityTypeName(), "entity").build())
                .returns(Jakarta.RS_RESPONSE);
        if (uriInfoParameter) {
            spec.addParameter(injectUriInfo()).addStatement("return super.postNew(uriInfo, entity)");
        } else {
            spec.addStatement("return super.postNew(entity)");
        }
        if (weak()) {
            spec.addAnnotation(ownerIdPathParameter());
        }
        return spec.build();
    }

    private MethodSpec createPostNewByMultipart() {
        var parameterSpec = ParameterSpec.builder(
                        Java.listOf(quarkusRestReactiveWorkaround ? Quarkus.FILE_UPLOAD : Jakarta.RS_ENTITY_PART),
                        "parts")
                .addAnnotation(requestBody(parametersSchema(parameter("entity", "The entity data as JSON object"))));
        if (quarkusRestReactiveWorkaround) {
            parameterSpec.addAnnotation(restFormAll());
        }
        var spec = MethodSpec.methodBuilder(
                        quarkusRestReactiveWorkaround ? "postNewByMultipartWorkaround" : "postNewByMultipart")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_POST).build())
                .addAnnotation(consumesMultipart())
                .addAnnotation(secureCRUDEndpoint(CRUDType.CREATE))
                .addAnnotation(operation(
                        "PostNew%sByMP".formatted(capitalize(IDs.toPlural(Records.idFromType(recordElement)))),
                        "Create a new entry of type %s".formatted(name)))
                .addParameter(parameterSpec.build())
                .returns(Jakarta.RS_RESPONSE)
                .addException(Java.IO_EXCEPTION);
        if (quarkusRestReactiveWorkaround) {
            spec.addParameter(injectReactiveRequestContext());
            spec.addStatement(
                    "return super.postNewByMultipart($T.convert(parts, context))",
                    QuiNimbusRest.QUARKUS_MULTIPART_SUPPORT);
        } else {
            spec.addStatement("return super.postNewByMultipart(parts)");
        }
        if (weak()) {
            spec.addAnnotation(ownerIdPathParameter());
        }
        return spec.build();
    }

    private MethodSpec createByPropertyEndpoint(MagicVariableElement ve) {
        var spec = MethodSpec.methodBuilder("by%s".formatted(capitalize(ve.getSimpleName())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(path("/by/%s/{%s}".formatted(ve.getSimpleName(), ve.getSimpleName())))
                .addAnnotation(producesJson())
                .addAnnotation(secureCRUDEndpoint(CRUDType.READ))
                .addAnnotation(operation(
                        "Get%sBy%s"
                                .formatted(
                                        capitalize(IDs.toPlural(Records.idFromType(recordElement))),
                                        capitalize(ve.getSimpleName())),
                        "Get all entries of type %s filtered by property %s".formatted(name, ve.getSimpleName())))
                .addAnnotation(response("200", arraySchema(entityTypeName())))
                .addParameter(
                        ParameterSpec.builder(ClassName.get(ve.getElement().asType()), ve.getSimpleName())
                                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_PATH_PARAM)
                                        .addMember("value", "\"%s\"".formatted(ve.getSimpleName()))
                                        .build())
                                .build())
                .returns(Jakarta.RS_RESPONSE);
        String code;
        if (weak()) {
            spec.addParameter(ParameterSpec.builder(Jakarta.RS_URIINFO, "uriInfo")
                    .addAnnotation(AnnotationSpec.builder(Jakarta.RS_CONTEXT).build())
                    .build());
            spec.addAnnotation(ownerIdPathParameter());
            code = """
                   return this.getByProperty(uriInfo, %s, this.repository::findAllBy%s);
                   """.formatted(ve.getSimpleName(), capitalize(ve.getSimpleName()));
        } else {
            code = """
                   return this.getByProperty(%s, this.repository::findAllBy%s);
                   """.formatted(ve.getSimpleName(), capitalize(ve.getSimpleName()));
        }
        spec.addCode(code);
        return spec.build();
    }

    private MethodSpec createMappedAsMethod(EntityMapperDefinition.Method method) {
        var spec = MethodSpec.methodBuilder("as%s".formatted(method.returnType()))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_GET).build())
                .addAnnotation(path("/as/%s".formatted(uncapitalize(method.returnType()))))
                .addAnnotation(producesJson())
                .addAnnotation(secureCRUDEndpoint(CRUDType.READ))
                .addAnnotation(operation(
                        "Get%sAs%s"
                                .formatted(
                                        capitalize(IDs.toPlural(Records.idFromType(recordElement))),
                                        capitalize(capitalize(method.returnType()))),
                        "Get all entries of type %s mapped as %s".formatted(name, capitalize(method.returnType()))))
                .addAnnotation(response("200", arraySchema(method.returnTypeName())))
                .addParameter(ParameterSpec.builder(Jakarta.RS_URIINFO, "uriInfo")
                        .addAnnotation(
                                AnnotationSpec.builder(Jakarta.RS_CONTEXT).build())
                        .build())
                .returns(Jakarta.RS_RESPONSE)
                .addCode("return getAllMapped($L::$L);", uncapitalize(method.mapperName()), method.methodName());
        if (weak()) {
            spec.addAnnotation(ownerIdPathParameter());
        }
        return spec.build();
    }

    private MethodSpec createActionMethod(RecordContextActionDefinition definition) {
        var spec = MethodSpec.methodBuilder("callAction%s".formatted(capitalize(definition.name())))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Jakarta.RS_POST).build())
                .addAnnotation(path("/action/%s".formatted(uncapitalize(definition.name()))))
                .addAnnotation(secureActionEndpoint(definition.method().element(), ActionType.CALL))
                .addAnnotation(operation(
                        "Call%sAction%s"
                                .formatted(
                                        capitalize(IDs.toPlural(Records.idFromType(recordElement))),
                                        capitalize(definition.name())),
                        "Call the global type action %s of type %s".formatted(definition.name(), name)))
                .addAnnotation(emptyResponse("204"))
                .addCode(
                        "this.$L.$L();",
                        uncapitalize(definition.type().simpleName()),
                        definition.method().name());
        if (weak()) {
            spec.addAnnotation(ownerIdPathParameter());
        }
        return spec.build();
    }

    private AnnotationSpec ownerIdPathParameter() {
        return pathParameter(
                owningType.getSimpleName() + "Id", "The id of the owning %s".formatted(owningType.getSimpleName()));
    }
}
