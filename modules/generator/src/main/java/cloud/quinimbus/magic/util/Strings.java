package cloud.quinimbus.magic.util;

import java.util.Locale;

public class Strings {

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase(Locale.US).concat(str.substring(1));
    }

    public static String uncapitalize(String str) {
        return str.substring(0, 1).toLowerCase(Locale.US).concat(str.substring(1));
    }
}
