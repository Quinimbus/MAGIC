package cloud.quinimbus.magic.elements;

import com.karuslabs.elementary.junit.Tools;
import com.karuslabs.elementary.junit.ToolsExtension;
import com.karuslabs.elementary.junit.annotations.Generation;
import com.karuslabs.elementary.junit.annotations.Inline;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ToolsExtension.class)
@Inline(
        name = "my.pack.MyTestClass",
        source = {
            """
            package my.pack;

            import java.util.List;

            public class MyTestClass {
                private final String field;
                private final List<String> listField;
            }
            """
        })
@Generation(
        retain = true,
        classes = "target/elementary/generated-classes",
        sources = "target/elementary/generated-sources")
public class MagicVariableElementTest {
    @Test
    public void test() {
        var element = Tools.elements().getTypeElement("my.pack.MyTestClass");
        var mce = new MagicClassElement(element, Tools.elements(), Tools.types());
        var stringField = mce.findFields()
                .filter(ve -> ve.getSimpleName().equals("field"))
                .findAny()
                .orElseThrow();
        var listField = mce.findFields()
                .filter(ve -> ve.getSimpleName().equals("listField"))
                .findAny()
                .orElseThrow();
        assertEquals("field", stringField.getSimpleName());
        assertEquals("java.lang.String", stringField.typeElement().getQualifiedName());
        assertEquals(
                "java.lang.String",
                listField
                        .typeParameters()
                        .findFirst()
                        .map(MagicClassElement::getQualifiedName)
                        .orElseThrow());
    }
}
