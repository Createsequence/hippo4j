package cn.hippo4j.core.plugin.manager;

import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link GlobalThreadPoolPluginManager}.
 */
public class DefaultGlobalThreadPoolPluginManager implements GlobalThreadPoolPluginManager {

    /**
     * registered thread pool plugins
     */
    @Getter
    private final Set<ThreadPoolPlugin> registeredThreadPoolPlugins = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /**
     * registrar
     */
    @Getter
    private final Set<ThreadPoolPluginRegistrar> appliedThreadPoolPluginRegistrar = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /**
     * registered supports
     */
    private final Map<String, ThreadPoolPluginSupport> registeredThreadPoolPluginSupports = new ConcurrentHashMap<>(32);

    /**
     * Register support, then register all registered {@link ThreadPoolPlugin}
     * and applied all {@link ThreadPoolPluginRegistrar} to {@link ThreadPoolPluginSupport}.
     *
     * @param support thread pool plugin manager delegate
     */
    @Override
    public void doRegister(ThreadPoolPluginSupport support) {
        registeredThreadPoolPluginSupports.put(support.getThreadPoolId(), support);
        doRegisterToSupport(support);
    }

    /**
     * Get all registered {@link ThreadPoolPluginSupport}
     *
     * @return all registered {@link ThreadPoolPluginSupport}
     */
    @Override
    public Collection<ThreadPoolPluginSupport> getRegisteredThreadPoolPluginSupports() {
        return registeredThreadPoolPluginSupports.values();
    }

    /**
     * Get {@link ThreadPoolPluginSupport} by thread-pool id
     *
     * @param threadPoolId thread-pool id
     * @return specified {@link ThreadPoolPluginSupport}, null if not registered.
     */
    @Override
    public ThreadPoolPluginSupport getThreadPoolPluginSupport(String threadPoolId) {
        return registeredThreadPoolPluginSupports.get(threadPoolId);
    }

    /**
     * Register plugin, then register to all registered {@link ThreadPoolPluginSupport}.
     * If {@link #enableRegister} return false, the plugin register only.
     *
     * @param plugin plugin
     */
    @Override
    public void registerThreadPoolPlugin(ThreadPoolPlugin plugin) {
        if (registeredThreadPoolPlugins.add(plugin) && enableRegister()) {
            registeredThreadPoolPluginSupports.values().forEach(support -> support.register(plugin));
        }
    }

    /**
     * Register registrar, then apply to all registered {@link ThreadPoolPluginSupport}.
     * If {@link #enableRegister} return false, the registrar register only.
     *
     * @param registrar registrar
     */
    @Override
    public void applyThreadPoolPluginRegistrar(ThreadPoolPluginRegistrar registrar) {
        if (appliedThreadPoolPluginRegistrar.add(registrar) && enableRegister()) {
            registeredThreadPoolPluginSupports.values().forEach(registrar::doRegister);
        }
    }

    /**
     * Register all registered {@link ThreadPoolPlugin} and applied all {@link ThreadPoolPluginRegistrar}
     * for all registered {@link ThreadPoolPluginSupport}.
     */
    public void completedRegister() {
        registeredThreadPoolPluginSupports.values().forEach(this::doRegisterToSupport);
    }

    /**
     * Whether enable register.
     *
     * @return true if enable register now, false otherwise
     */
    protected boolean enableRegister() {
        return true;
    }
    
    /**
     * Do register to support
     *
     * @param support support
     */
    protected void doRegisterToSupport(ThreadPoolPluginSupport support) {
        registeredThreadPoolPlugins.forEach(support::tryRegister);
        appliedThreadPoolPluginRegistrar.forEach(registrar -> registrar.doRegister(support));
    }

    /**
     * Register support, but component registration is not performed.
     *
     * @param support support
     * @return true if support not registered, false otherwise
     */
    protected boolean registerThreadPoolPluginSupport(ThreadPoolPluginSupport support) {
        return Objects.isNull(registeredThreadPoolPluginSupports.putIfAbsent(support.getThreadPoolId(), support));
    }

}
