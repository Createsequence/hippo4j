package cn.hippo4j.core.plugin.manager;

import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link GlobalThreadPoolPluginPublisher}.
 */
public class DefaultGlobalThreadPoolPluginPublisher implements GlobalThreadPoolPluginPublisher {

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
    @Getter
    private final Set<ThreadPoolPluginSupport> registeredThreadPoolPluginSupports = Collections.newSetFromMap(new ConcurrentHashMap<>(32));

    /**
     * Register all registered {@link ThreadPoolPlugin} and applied all {@link ThreadPoolPluginRegistrar}
     * for the specified thread-pool instance.
     *
     * @param support thread pool plugin manager delegate
     */
    @Override
    public void doRegister(ThreadPoolPluginSupport support) {
        registeredThreadPoolPluginSupports.add(support);
        doRegisterToSupport(support);
    }

    /**
     * Register {@link ThreadPoolPlugin} for all registered {@link ThreadPoolPluginSupport}.
     * If {@link #enableRegister} return false, the plugin register only.
     *
     * @param plugin plugin
     */
    @Override
    public void registerThreadPoolPlugin(ThreadPoolPlugin plugin) {
        if (registeredThreadPoolPlugins.add(plugin) && enableRegister()) {
            registeredThreadPoolPluginSupports.forEach(support -> support.register(plugin));
        }
    }

    /**
     * Apply {@link ThreadPoolPluginRegistrar} for all registered {@link ThreadPoolPluginSupport}.
     * If {@link #enableRegister} return false, the registrar register only.
     *
     * @param registrar registrar
     */
    @Override
    public void applyThreadPoolPluginRegistrar(ThreadPoolPluginRegistrar registrar) {
        if (appliedThreadPoolPluginRegistrar.add(registrar) && enableRegister()) {
            registeredThreadPoolPluginSupports.forEach(registrar::doRegister);
        }
    }

    /**
     * Register all registered {@link ThreadPoolPlugin} and applied all {@link ThreadPoolPluginRegistrar}
     * for all registered {@link ThreadPoolPluginSupport}.
     */
    public void completedRegister() {
        registeredThreadPoolPluginSupports.forEach(this::doRegisterToSupport);
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

}
