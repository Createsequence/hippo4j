package cn.hippo4j.config.springboot.starter.support;

import cn.hippo4j.common.toolkit.StringUtil;
import cn.hippo4j.config.springboot.starter.config.ExecutorProperties;
import cn.hippo4j.config.springboot.starter.config.PluginProperties;
import cn.hippo4j.core.plugin.manager.GlobalThreadPoolPluginRegistrarManager;
import cn.hippo4j.core.plugin.manager.GlobalThreadPoolPluginSupportManager;
import cn.hippo4j.core.plugin.manager.ThreadPoolPluginRegistrar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Thread pool plugin refresher.
 *
 * @see ExecutorProperties
 * @see GlobalThreadPoolPluginRegistrarManager
 * @see GlobalThreadPoolPluginSupportManager
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadPoolPluginPropertiesResolver {

    /**
     * All registrar matched.
     */
    protected static final String ALL = "*";

    /**
     * Separator.
     */
    protected static final String SEPARATOR = ",";

    /**
     * Refresh the plugin configuration of the specified thread-pool according
     * to the default configuration and custom configuration.
     *
     * @param beforeProperties default properties
     * @param afterProperties custom properties
     * @return current resolved {@link PluginProperties}
     */
    @Nullable
    public static PluginProperties resolve(
        PluginProperties beforeProperties, PluginProperties afterProperties) {
        boolean hasBeforeProperties = Objects.nonNull(beforeProperties);
        boolean hasAfterProperties = Objects.nonNull(afterProperties);

        // nothing to refresh
        if (!hasAfterProperties && !hasBeforeProperties) {
            return null;
        }

        PluginProperties resolvedProperties;
        // only has before properties
        if (!hasAfterProperties) {
            resolvedProperties = beforeProperties;
        }
        // only has after properties
        else if (!hasBeforeProperties) {
            resolvedProperties = afterProperties;
        }
        // both properties exist
        else {
            resolvedProperties = resolveProperties(beforeProperties, afterProperties);
        }
        return resolvedProperties;
    }

    /**
     * whether the configuration has not changed.
     *
     * @param beforeProperties before properties
     * @param afterProperties after properties
     * @return true if configuration has not changed, false otherwise
     */
    public static boolean isNothingChanged(PluginProperties beforeProperties, PluginProperties afterProperties) {
        return Stream.<BiPredicate<PluginProperties, PluginProperties>> of(
            (before, after) -> Objects.equals(before.getEnable(), after.getEnable()),
            (before, after) -> Objects.equals(before.getIncludes(), after.getIncludes()),
            (before, after) -> Objects.equals(before.getExcludes(), after.getExcludes())
        ).allMatch(condition -> condition.test(beforeProperties, afterProperties));
    }

    /**
     * Get the required registrar according to the configuration.
     *
     * @param properties properties
     * @param manager manager
     * @return required {@link ThreadPoolPluginRegistrar}
     */
    public static Collection<ThreadPoolPluginRegistrar> resolveEnableRegistrars(
        PluginProperties properties, GlobalThreadPoolPluginRegistrarManager manager) {
        // plugin mechanism is not enabled
        if (Objects.equals(properties.getEnable(), Boolean.FALSE)) {
            return Collections.emptyList();
        }

        // parse excludes and includes
        Set<String> excludes = parseRegistrarIds(properties.getExcludes());
        if (isAll(excludes)) {
            return Collections.emptyList();
        }
        Set<String> includes = parseRegistrarIds(properties.getIncludes());

        // enable all plugins
        if (isAll(includes)) {
            return manager.getAllRegistrars().stream()
                .filter(registrar -> !excludes.contains(registrar.getId()))
                .collect(Collectors.toList());
        }

        // enable some plugins only
        includes.removeAll(excludes);
        return includes.stream()
            .map(manager::getRegistrar)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static PluginProperties resolveProperties(PluginProperties beforeProperties, PluginProperties afterProperties) {
        // noting changed, return before
        if (isNothingChanged(beforeProperties, afterProperties)) {
            return beforeProperties;
        }
        // something changed, resolve diff
        PluginProperties resolved = new PluginProperties();
        resolved.setEnable(getBeforeIfNotAfter(PluginProperties::getEnable, beforeProperties, afterProperties));
        resolved.setIncludes(getBeforeIfNotAfter(PluginProperties::getIncludes, beforeProperties, afterProperties));
        resolved.setExcludes(getBeforeIfNotAfter(PluginProperties::getExcludes, beforeProperties, afterProperties));
        return resolved;
    }

    private static <T, R> R getBeforeIfNotAfter(Function<T, R> mapper, T before, T after) {
        return Objects.isNull(after) ? mapper.apply(before) : mapper.apply(after);
    }

    private static Set<String> parseRegistrarIds(String exp) {
        return Stream.of(StringUtil.split(exp, SEPARATOR))
            .map(String::trim)
            .filter(StringUtil::isNotEmpty)
            .collect(Collectors.toSet());
    }

    private static boolean isAll(Set<String> ids) {
        return ids.contains(ALL);
    }
}
