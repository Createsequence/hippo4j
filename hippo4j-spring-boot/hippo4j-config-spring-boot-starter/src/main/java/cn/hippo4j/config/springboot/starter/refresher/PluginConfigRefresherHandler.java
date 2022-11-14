package cn.hippo4j.config.springboot.starter.refresher;

import cn.hippo4j.common.toolkit.CollectionUtil;
import cn.hippo4j.common.toolkit.StringUtil;
import cn.hippo4j.config.springboot.starter.config.PluginProperties;
import cn.hippo4j.core.plugin.manager.GlobalThreadPoolPluginManager;
import cn.hippo4j.core.plugin.manager.ThreadPoolPluginSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin config refresher handler.
 */
@RequiredArgsConstructor
public class PluginConfigRefresherHandler {

    private static final String SEPARATOR = ",";
    private static final String ALL = "*";

    private final GlobalThreadPoolPluginManager globalThreadPoolPluginManager;

    /**
     * Refresh the plugin configuration data of the thread pool:
     * <ul>
     *     <li>使用插件注册器重新向XX注册插件；</li>
     *     <li>移除；</li>
     * </ul>
     *
     * @param executor executor
     * @param properties properties
     */
    public void dynamicRefresh(ThreadPoolExecutor executor, PluginProperties properties) {
        if (executor instanceof ThreadPoolPluginSupport && Objects.nonNull(properties)) {
            ThreadPoolPluginSupport support = (ThreadPoolPluginSupport)executor;
            applyRegistrars(support, properties);
            processPlugins(support, properties);
            refreshPluginConfig(support, properties);
        }
    }

    private void processPlugins(ThreadPoolPluginSupport support, PluginProperties properties) {
        Set<String> disablePlugins = parseIds(properties.getDisablePlugins());
        if (CollectionUtil.isNotEmpty(disablePlugins)) {
            // disable all plugin
            if (isAll(disablePlugins)) {
                support.clear();
                return;
            }
            // disable something plugin
            disablePlugins.forEach(support::unregister);
        }

        // enable all
        Set<String> enablePlugins = parseIds(properties.getEnablePlugins());
        if (isAll(enablePlugins)) {
            globalThreadPoolPluginManager.getAllThreadPoolPlugins().stream()
                .filter(plugin -> !disablePlugins.contains(plugin.getId()))
                .forEach(support::register);
            return;
        }

        // enable something
        enablePlugins.removeAll(disablePlugins);
        if (CollectionUtil.isNotEmpty(enablePlugins)) {
            enablePlugins.stream()
                .map(globalThreadPoolPluginManager::getThreadPoolPlugin)
                .filter(Objects::nonNull)
                .forEach(support::register);
        }
    }

    private void refreshPluginConfig(ThreadPoolPluginSupport support, PluginProperties properties) {
        properties.getPlugins().forEach(
            (id, config) -> support.getPlugin(id).ifPresent(plugin -> {
                Binder binder = new Binder(new MapConfigurationPropertySource(config));
                binder.bind("", Bindable.ofInstance(plugin));
            })
        );
    }

    private void applyRegistrars(ThreadPoolPluginSupport support, PluginProperties properties) {
        Set<String> disableRegistrars = parseIds(properties.getDisableRegistrars());
        // disable all registrar
        if (CollectionUtil.isNotEmpty(disableRegistrars) && isAll(disableRegistrars)) {
            return;
        }

        // enable all
        Set<String> enableRegistrars = parseIds(properties.getEnableRegistrars());
        if (isAll(enableRegistrars)) {
            globalThreadPoolPluginManager.getAllThreadPoolPluginRegistrars().stream()
                .filter(registrar -> !disableRegistrars.contains(registrar.getId()))
                .forEach(registrar -> registrar.doRegister(support));
            return;
        }

        // enable something
        enableRegistrars.removeAll(disableRegistrars);
        if (CollectionUtil.isNotEmpty(enableRegistrars)) {
            enableRegistrars.stream()
                .map(globalThreadPoolPluginManager::getThreadPoolPluginRegistrar)
                .filter(Objects::nonNull)
                .forEach(registrar -> registrar.doRegister(support));
        }
    }

    private Set<String> parseIds(String exp) {
        if (StringUtil.isEmpty(exp)) {
            return Collections.emptySet();
        }
        return Stream.of(StringUtil.split(exp, SEPARATOR))
            .map(String::trim)
            .filter(StringUtil::isNotEmpty)
            .collect(Collectors.toSet());
    }

    private boolean isAll(Collection<String> ids) {
        return ids.stream().anyMatch(id -> Objects.equals(ALL, id));
    }
}
