package cn.hippo4j.config.springboot.starter.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
     * Allow plugin
     */
    private Boolean enable = Boolean.TRUE;

    /**
     * Enable plugins. three input formats are supported:
     * <ul>
     *     <li>Null or empty: use only the default plugins；</li>
     *     <li><em>*</em>: enable all plugins；</li>
     *     <li><em>xxx, xxx, xxx</em>: use only specified plugins；</li>
     * </ul>
     */
    private String includes = "";

    /**
     * Disable plugins. three input formats are supported:
     * <ul>
     *     <li>Null or empty: do not disable any plugins；</li>
     *     <li><em>*</em>: disable any plugins；</li>
     *     <li><em>xxx, xxx, xxx</em>: disable only specified plugins；</li>
     * </ul>
     */
    private String excludes = "";

}
