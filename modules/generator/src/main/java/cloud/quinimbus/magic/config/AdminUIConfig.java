package cloud.quinimbus.magic.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminUIConfig(
        App app, Map<String, Type> types, Dependencies dependencies, Oidc oidc, Map<String, MenuGroup> menugroups) {

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
        if (menugroups == null) {
            menugroups = Map.of();
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
            String group,
            Integer orderKey,
            Map<String, Field> fields,
            Map<String, FieldGroup> fieldGroups,
            Map<String, Action> globalActions,
            Map<String, Action> instanceActions) {

        public Type {
            if (icon == null || icon.isBlank()) {
                icon = "database";
            }
            if (fields == null) {
                fields = Map.of();
            }
            if (orderKey == null) {
                orderKey = Integer.MAX_VALUE;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Field(
            String label,
            Integer orderKey,
            String group,
            Map<String, AllowedValue> allowedValues,
            FieldTableConfig table,
            boolean multilineText) {
        public Field {
            if (orderKey == null) {
                orderKey = Integer.MAX_VALUE;
            }
            if (allowedValues == null) {
                allowedValues = Map.of();
            }
            if (table == null) {
                table = new cloud.quinimbus.magic.config.AdminUIConfig.FieldTableConfig(null);
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record AllowedValue(String label) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record FieldTableConfig(FieldTableColumnVisibility visibility) {
        public FieldTableConfig {
            if (visibility == null) {
                visibility = FieldTableColumnVisibility.ALWAYS;
            }
        }
    }

    public static enum FieldTableColumnVisibility {
        ALWAYS,
        NEVER,
        DEFAULT_VISIBLE,
        DEFAULT_HIDDEN
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Action(String label, String icon) {
        public Action withLabel(String label) {
            return new Action(label, icon);
        }

        public Action withIcon(String icon) {
            return new Action(label, icon);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record MenuGroup(String label, Integer orderKey) {
        public MenuGroup {
            if (orderKey == null) {
                orderKey = Integer.MAX_VALUE;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record FieldGroup(String label, Integer orderKey) {
        public FieldGroup {
            if (orderKey == null) {
                orderKey = Integer.MAX_VALUE;
            }
        }
    }
}
