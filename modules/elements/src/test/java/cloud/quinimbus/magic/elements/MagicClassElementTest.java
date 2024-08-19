package cloud.quinimbus.magic.elements;

import com.karuslabs.elementary.junit.Tools;
import com.karuslabs.elementary.junit.ToolsExtension;
import com.karuslabs.elementary.junit.annotations.Generation;
import com.karuslabs.elementary.junit.annotations.Inline;
import com.karuslabs.elementary.junit.annotations.Inlines;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ToolsExtension.class)
@Inlines({
    @Inline(
            name = "my.pack.MyTestClass",
            source = {
                """
                package my.pack;

                public class MyTestClass {
                    private final String field;
                }
                """
            }),
    @Inline(
            name = "my.pack.MyTestEnum",
            source = {
                """
                package my.pack;

                public enum MyTestEnum {
                    A, B, C
                }
                """
            })
})
@Generation(
        retain = true,
        classes = "target/elementary/generated-classes",
        sources = "target/elementary/generated-sources")
public class MagicClassElementTest {
    @Test
    public void testClass() {
        var element = Tools.elements().getTypeElement("my.pack.MyTestClass");
        var mce = new MagicClassElement(element, Tools.elements(), Tools.types());
        assertEquals("my.pack", mce.getPackageName());
        assertEquals(1, mce.findFields().count());
        assertEquals(1, mce.findFieldsOfType(ClassName.get(String.class)).count());
        assertEquals(0, mce.findFieldsOfType(TypeName.BOOLEAN).count());
    }

    @Test
    public void testEnum() {
        var element = Tools.elements().getTypeElement("my.pack.MyTestEnum");
        var mce = new MagicClassElement(element, Tools.elements(), Tools.types());
        assertTrue(mce.isEnum());
        assertEquals(Set.of("A", "B", "C"), mce.enumValues().collect(Collectors.toSet()));
    }
}
