package cloud.quinimbus.magic.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminUIConfig(App app, Map<String, Type> types, Dependencies dependencies) {

    public AdminUIConfig {
        if (app == null) {
            app = new App(null, null, null);
        }
        if (types == null) {
            types = Map.of();
        }
        if (dependencies == null) {
            dependencies = new Dependencies(null, null, null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Dependencies(Dependency adminUi, Dependency nginx, Dependency node) {
        public Dependencies {
            if (adminUi == null) {
                adminUi = new Dependency("0.1.0");
            }
            if (nginx == null) {
                nginx = new Dependency("1.27.1");
            }
            if (node == null) {
                node = new Dependency("22.6.0");
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Dependency(String version) {

        public Dependency {
            if (version == null || version.isBlank()) {
                version = "0.0.0";
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record App(String name, String version, String title) {

        public App {
            if (name == null || name.isBlank()) {
                name = "some-admin-ui";
            }
            if (version == null || version.isBlank()) {
                version = "1.0.0";
            }
            if (title == null || title.isBlank()) {
                title = name;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Type(
            String icon, String labelSingular, String labelPlural, String keyField, Map<String, Field> fields) {

        public Type {
            if (icon == null || icon.isBlank()) {
                icon = "database";
            }
            if (fields == null) {
                fields = Map.of();
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Field(String label) {}
}
