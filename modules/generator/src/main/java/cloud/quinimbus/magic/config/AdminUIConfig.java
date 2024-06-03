package cloud.quinimbus.magic.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminUIConfig(App app, Map<String, Type> types, AdminUiDependencyConfig adminUiDependency) {
    
    public AdminUIConfig {
        if (app == null) {
            app = new App(null, null, null);
        }
        if (types == null) {
            types = Map.of();
        }
        if (adminUiDependency == null) {
            adminUiDependency = new AdminUiDependencyConfig("0.0.0");
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record AdminUiDependencyConfig(String version) {
        
        public AdminUiDependencyConfig {
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
    public static record Type(String icon, String labelSingular, String labelPlural, String keyField, Map<String, Field> fields) {
        
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
    public static record Field(String label) {
        
    }
}
