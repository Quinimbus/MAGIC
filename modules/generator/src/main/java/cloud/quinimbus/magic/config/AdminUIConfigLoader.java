package cloud.quinimbus.magic.config;

import cloud.quinimbus.common.tools.IDs;
import static cloud.quinimbus.magic.util.Strings.*;
import java.util.Map;

public class AdminUIConfigLoader {
    public static AdminUIConfig.Type getTypeConfig(AdminUIConfig config, String name, String idFieldName) {
        var defaultConfig = new cloud.quinimbus.magic.config.AdminUIConfig.Type(
                "database", capitalize(name), capitalize(IDs.toPlural(name)), idFieldName, Map.of(), Map.of());
        var providedConfig = config.types().get(uncapitalize(name));
        if (providedConfig == null) {
            return defaultConfig;
        }
        return new cloud.quinimbus.magic.config.AdminUIConfig.Type(
                providedConfig.icon() == null ? defaultConfig.icon() : providedConfig.icon(),
                providedConfig.labelSingular() == null ? defaultConfig.labelSingular() : providedConfig.labelSingular(),
                providedConfig.labelPlural() == null ? defaultConfig.labelPlural() : providedConfig.labelPlural(),
                providedConfig.keyField() == null ? defaultConfig.keyField() : providedConfig.keyField(),
                providedConfig.fields() == null ? defaultConfig.fields() : providedConfig.fields(),
                providedConfig.globalActions() == null
                        ? defaultConfig.globalActions()
                        : providedConfig.globalActions());
    }

    public static AdminUIConfig.Field getFieldConfig(AdminUIConfig.Type typeConfig, String field) {
        return typeConfig
                .fields()
                .getOrDefault(field, new cloud.quinimbus.magic.config.AdminUIConfig.Field(capitalize(field), null));
    }

    public static AdminUIConfig.GlobalAction getGlobalActionConfig(AdminUIConfig.Type typeConfig, String action) {
        var config = typeConfig
                .globalActions()
                .getOrDefault(action, new AdminUIConfig.GlobalAction("Action: " + action, "cog-play"));
        if (config.icon() == null || config.icon().isEmpty()) {
            config = config.withIcon("cog-play");
        }
        if (config.label() == null || config.label().isEmpty()) {
            config = config.withLabel("Action: " + action);
        }
        return config;
    }
}
