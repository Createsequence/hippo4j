package cn.hippo4j.config.springboot.starter.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Executor plugin properties.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ExecutorPluginProperties {

    /**
     * plugin id
     */
    private String id;

    /**
     * enable plugin
     */
    private Boolean enable = true;

    /**
     * plugin props
     */
    public Map<String, String> props;

}
