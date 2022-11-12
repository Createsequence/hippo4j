package cn.hippo4j.config.springboot.starter.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Plugin properties.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PluginProperties {

    /**
     * Enable plugin.
     */
    private boolean enable = true;

    /**
     * Plugins allowed by default，Two input formats are supported:
     * <ul>
     *     <li>one or more plugin ids, separate multiple ids with commas, eg: {@code xxx, xxx, xxx};</li>
     *     <li>*, indicates that all registrars are enabled by default;</li>
     * </ul>
     */
    private String enablePlugins = "*";

    /**
     * Plugins not allowed by default，Threed input formats are supported:
     * <ul>
     *     <li>one or more plugin ids, separate multiple ids with commas, eg: {@code xxx, xxx, xxx};</li>
     *     <li>*, indicates that all registrars are enabled by default;</li>
     *     <li>empty, Do not disable any plugin;</li>
     * </ul>
     *
     * <b>NOTE: This configuration has priority over {@link #enablePlugins}.</b>
     */
    private String disablePlugins = "";

    /**
     * Registers allowed by default，Two input formats are supported:
     * <ul>
     *     <li>one or more registrar ids, separate multiple ids with commas, eg: {@code xxx, xxx, xxx};</li>
     *     <li>*, indicates that all registrars are enabled by default;</li>
     * </ul>
     */
    private String enableRegistrars = "*";

    /**
     * Plugins not allowed by default，Threed input formats are supported:
     * <ul>
     *     <li>one or more registrar ids, separate multiple ids with commas, eg: {@code xxx, xxx, xxx};</li>
     *     <li>*, indicates that all registrars are enabled by default;</li>
     *     <li>empty, Do not disable any registrar;</li>
     * </ul>
     *
     * <b>NOTE: This configuration has priority over {@link #enableRegistrars}.</b>
     */
    private String disableRegistrars = "";

    /**
     * Plugins config
     */
    private Map<String, Map<String, String>> plugins;

}
