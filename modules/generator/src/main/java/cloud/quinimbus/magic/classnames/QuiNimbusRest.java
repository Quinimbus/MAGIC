package cloud.quinimbus.magic.classnames;

import com.squareup.javapoet.ClassName;

public class QuiNimbusRest {

    public static final ClassName ABSTRACT_CRUD_SINGLE_RESOURCE =
            ClassName.get("cloud.quinimbus.rest.crud", "AbstractCrudSingleResource");

    public static final ClassName ABSTRACT_CRUD_ALL_RESOURCE =
            ClassName.get("cloud.quinimbus.rest.crud", "AbstractCrudAllResource");

    public static final ClassName ABSTRACT_WEAK_CRUD_SINGLE_RESOURCE =
            ClassName.get("cloud.quinimbus.rest.crud", "AbstractWeakCrudSingleResource");

    public static final ClassName ABSTRACT_WEAK_CRUD_ALL_RESOURCE =
            ClassName.get("cloud.quinimbus.rest.crud", "AbstractWeakCrudAllResource");
}
