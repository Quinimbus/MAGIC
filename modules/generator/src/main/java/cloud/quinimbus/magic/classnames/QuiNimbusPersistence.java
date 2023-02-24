package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class QuiNimbusPersistence {
    
    public static final ClassName CRUD_REPOSITORY = ClassName.get("cloud.quinimbus.persistence.repositories", "CRUDRepository");
    
    public static final ClassName ENTITY_TYPE_CLASS = ClassName.get("cloud.quinimbus.persistence.api.annotation", "EntityTypeClass");
}
