package cn.hippo4j.core.plugin.manager;

import cn.hippo4j.core.plugin.ThreadPoolPlugin;

import java.util.Collection;

/**
 * A publisher of {@link ThreadPoolPlugin}.
 */
public interface GlobalThreadPoolPluginPublisher extends ThreadPoolPluginRegistrar {

    /**
     * Register {@link ThreadPoolPlugin} for all registered {@link ThreadPoolPluginSupport}.
     *
     * @param plugin plugin
     */
    void registerThreadPoolPlugin(ThreadPoolPlugin plugin);

    /**
     * Get all published {@link ThreadPoolPlugin}.
     *
     * @return all published {@link ThreadPoolPlugin}
     */
    Collection<ThreadPoolPlugin> getRegisteredThreadPoolPlugins();

    /**
     * Apply {@link ThreadPoolPluginRegistrar} for all registered {@link ThreadPoolPluginSupport}.
     *
     * @param registrar registrar
     */
    void applyThreadPoolPluginRegistrar(ThreadPoolPluginRegistrar registrar);

    /**
     * Get all {@link ThreadPoolPluginRegistrar}.
     *
     * @return all {@link ThreadPoolPluginRegistrar}.
     */
    Collection<ThreadPoolPluginRegistrar> getAppliedThreadPoolPluginRegistrar();

    /**
     * Register all registered {@link ThreadPoolPlugin} and applied all {@link ThreadPoolPluginRegistrar}
     * for the specified thread-pool instance.
     *
     * @param support thread pool plugin manager delegate
     */
    @Override
    void doRegister(ThreadPoolPluginSupport support);

}
