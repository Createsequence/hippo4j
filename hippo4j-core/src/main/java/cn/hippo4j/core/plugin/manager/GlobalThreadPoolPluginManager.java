package cn.hippo4j.core.plugin.manager;

import cn.hippo4j.core.plugin.ThreadPoolPlugin;

import java.util.Collection;

/**
 * A manager of {@link ThreadPoolPlugin} and {@link ThreadPoolPluginRegistrar},
 * used to support centralized registration of plugins to all {@link ThreadPoolPluginSupport}.
 */
public interface GlobalThreadPoolPluginManager extends ThreadPoolPluginRegistrar {

    /**
     * Get all registered {@link ThreadPoolPluginSupport}
     *
     * @return all registered {@link ThreadPoolPluginSupport}
     */
    Collection<ThreadPoolPluginSupport> getRegisteredThreadPoolPluginSupports();

    /**
     * Get {@link ThreadPoolPluginSupport} by thread-pool id
     *
     * @param threadPoolId thread-pool id
     * @return specified {@link ThreadPoolPluginSupport}, null if not registered.
     */
    ThreadPoolPluginSupport getThreadPoolPluginSupport(String threadPoolId);

    /**
     * Register plugin, then register to all registered {@link ThreadPoolPluginSupport}.
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
     * Register registrar, then apply to all registered {@link ThreadPoolPluginSupport}.
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
     * Register support, then register all registered {@link ThreadPoolPlugin}
     * and applied all {@link ThreadPoolPluginRegistrar} to {@link ThreadPoolPluginSupport}.
     *
     * @param support thread pool plugin manager support
     */
    @Override
    void doRegister(ThreadPoolPluginSupport support);

}
