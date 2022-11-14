package cn.hippo4j.config.springboot.starter.refresher.event;

import cn.hippo4j.common.toolkit.StringUtil;
import cn.hippo4j.config.springboot.starter.config.ExecutorProperties;
import cn.hippo4j.config.springboot.starter.config.PluginProperties;
import cn.hippo4j.core.plugin.manager.GlobalThreadPoolPluginRegistrarManager;
import cn.hippo4j.core.plugin.manager.GlobalThreadPoolPluginSupportManager;
import cn.hippo4j.core.plugin.manager.ThreadPoolPluginRegistrar;
import cn.hippo4j.core.plugin.manager.ThreadPoolPluginSupport;
import lombok.NonNull;
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
public class ThreadPoolPluginRefresher {

    /**
     * All registrar matched.
     */
    protected static final String ALL = "*";

    /**
     * Separator.
     */
    protected static final String SEPARATOR = ",";

    /**
     * Global thread pool plugin registrar manager.
     */
    protected final GlobalThreadPoolPluginRegistrarManager globalThreadPoolPluginRegistrarManager;

    /**
     * Refresh the plugin configuration of the specified thread-pool according
     * to the default configuration and custom configuration.
     *
     * @param support {@link ThreadPoolPluginSupport}
     * @param beforeResolvedProperties  before resolved properties
     * @param afterDefaultProperties default properties
     * @param afterCustomProperties custom properties
     * @return current resolved {@link PluginProperties}
     */
    @Nullable
    public final PluginProperties refresh(
        @NonNull ThreadPoolPluginSupport support, PluginProperties beforeResolvedProperties,
        PluginProperties afterDefaultProperties, PluginProperties afterCustomProperties) {
        boolean hasDefaultProperties = Objects.nonNull(afterDefaultProperties);
        boolean hasCustomProperties = Objects.nonNull(afterCustomProperties);

        // nothing to refresh
        if (!hasCustomProperties && !hasDefaultProperties) {
            if (log.isDebugEnabled()) {
                log.debug("ThreadPoolPluginSupport [{}] does not have any plugin configuration to refresh", support.getThreadPoolId());
            }
            return null;
        }

        PluginProperties afterResolvedProperties;
        // only has default properties
        if (!hasCustomProperties) {
            afterResolvedProperties = afterDefaultProperties;
        }
        // only has custom properties
        else if (!hasDefaultProperties) {
            afterResolvedProperties = afterCustomProperties;
        }
        // both properties exist
        else {
            afterResolvedProperties = resolveProperties(afterDefaultProperties, afterCustomProperties);
        }

        // clear all registered plugins, and use registrar for thread pool once again
        doRefresh(support, beforeResolvedProperties, afterResolvedProperties);
        return afterResolvedProperties;
    }

    /**
     * <p>Refresh the plugin config for specified {@link ThreadPoolPluginSupport}. <br />
     * By default, only when {@code beforeResolvedProperties} and {@code afterResolvedProperties}
     * <b>have any different attribute values</b>,
     * all registered plugins are <b>always unregistered first</b>,
     * and then re-registered plugin to {@link ThreadPoolPluginSupport} through the registrar.
     *
     * @param support support
     * @param beforeResolvedProperties before resolved properties
     * @param afterResolvedProperties after resolved properties
     */
    protected void doRefresh(
        ThreadPoolPluginSupport support, PluginProperties beforeResolvedProperties, PluginProperties afterResolvedProperties) {
        if (log.isInfoEnabled()) {
            log.info("Clear all registered plugins of ThreadPoolPluginSupport [{}], and use registrar for thread pool once again", support.getThreadPoolId());
        }
        // refresh config only when property changed
        if (!isNothingChanged(beforeResolvedProperties, afterResolvedProperties)) {
            support.clear();
            resolveEnableRegistrars(afterResolvedProperties)
                .forEach(registrar -> registrar.doRegister(support));
        }
    }

    private static boolean isNothingChanged(PluginProperties beforeResolvedProperties, PluginProperties afterResolvedProperties) {
        return Stream.<BiPredicate<PluginProperties, PluginProperties>> of(
            (before, after) -> Objects.equals(before.getEnable(), after.getEnable()),
                (before, after) -> Objects.equals(before.getIncludes(), after.getIncludes()),
                (before, after) -> Objects.equals(before.getExcludes(), after.getExcludes())
            ).allMatch(condition -> condition.test(beforeResolvedProperties, afterResolvedProperties));
    }

    private PluginProperties resolveProperties(PluginProperties defaultProperties, PluginProperties customProperties) {
        PluginProperties resolved = new PluginProperties();
        resolved.setEnable(getDefaultIfNotCustom(PluginProperties::getEnable, defaultProperties, customProperties));
        resolved.setIncludes(getDefaultIfNotCustom(PluginProperties::getIncludes, defaultProperties, customProperties));
        resolved.setExcludes(getDefaultIfNotCustom(PluginProperties::getExcludes, defaultProperties, customProperties));
        return resolved;
    }

    private Collection<ThreadPoolPluginRegistrar> resolveEnableRegistrars(PluginProperties resolvedProperties) {
        // plugin mechanism is not enabled
        if (Objects.equals(resolvedProperties.getEnable(), Boolean.FALSE)) {
            return Collections.emptyList();
        }

        // parse excludes and includes
        Set<String> excludes = parseRegistrarIds(resolvedProperties.getExcludes());
        if (isAll(excludes)) {
            return Collections.emptyList();
        }
        Set<String> includes = parseRegistrarIds(resolvedProperties.getIncludes());

        // enable all plugins
        if (isAll(includes)) {
            return globalThreadPoolPluginRegistrarManager.getAllRegistrars().stream()
                .filter(registrar -> !excludes.contains(registrar.getId()))
                .collect(Collectors.toList());
        }

        // enable some plugins only
        includes.removeAll(excludes);
        return includes.stream()
            .map(globalThreadPoolPluginRegistrarManager::getRegistrar)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static <T, R> R getDefaultIfNotCustom(Function<T, R> mapper, T defaultVal, T customVal) {
        return Objects.isNull(customVal) ? mapper.apply(defaultVal) : mapper.apply(customVal);
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
