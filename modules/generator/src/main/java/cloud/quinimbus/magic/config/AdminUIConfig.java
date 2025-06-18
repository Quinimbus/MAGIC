package cloud.quinimbus.magic.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminUIConfig(App app, Map<String, Type> types, Dependencies dependencies, Oidc oidc) {

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
        if (oidc == null) {
            oidc = new Oidc(null, null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Dependencies(Dependency adminUi, Dependency nginx, Dependency node) {
        public Dependencies {
            if (adminUi == null) {
                adminUi = new Dependency("0.2.0");
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
    public static record Oidc(String authority, String clientId) {

        public Oidc {
            if (authority == null || authority.isBlank()) {
                authority = "http://you-forgot-to-set-oidc-authority";
            }
            if (clientId == null || clientId.isBlank()) {
                clientId = "you-forgot-to-set-oidc-clientId";
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Type(
            String icon,
            String labelSingular,
            String labelPlural,
            String keyField,
            Map<String, Field> fields,
            Map<String, GlobalAction> globalActions) {

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
    public static record Field(String label, Map<String, AllowedValue> allowedValues) {
        public Field {
            if (allowedValues == null) {
                allowedValues = Map.of();
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record AllowedValue(String label) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record GlobalAction(String label, String icon) {
        public GlobalAction withLabel(String label) {
            return new cloud.quinimbus.magic.config.AdminUIConfig.GlobalAction(label, icon);
        }

        public GlobalAction withIcon(String icon) {
            return new cloud.quinimbus.magic.config.AdminUIConfig.GlobalAction(label, icon);
        }
    }
}
