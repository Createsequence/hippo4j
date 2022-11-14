package cn.hippo4j.core.plugin.manager;

import cn.hippo4j.common.toolkit.Assert;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link GlobalThreadPoolPluginRegistrarManager} and {@link GlobalThreadPoolPluginRegistrarManager}.
 */
public class GlobalThreadPoolPluginManager implements GlobalThreadPoolPluginSupportManager, GlobalThreadPoolPluginRegistrarManager {

    /**
     * Thread pool plugin registrars.
     */
    private final Map<String, ThreadPoolPluginRegistrar> threadPoolPluginRegistrars = new ConcurrentHashMap<>(16);

    /**
     * Thread pool plugin support.
     */
    private final Map<String, ThreadPoolPluginSupport> threadPoolPluginSupports = new ConcurrentHashMap<>(32);

    /**
     * Get {@link ThreadPoolPluginRegistrar}
     *
     * @param registrarId registrarId
     * @return registrar if the specified registrar has been registered, null otherwise
     */
    @Override
    public @Nullable ThreadPoolPluginRegistrar getRegistrar(String registrarId) {
        return threadPoolPluginRegistrars.get(registrarId);
    }

    /**
     * Get all {@link ThreadPoolPluginRegistrar}
     *
     * @return {@link ThreadPoolPluginRegistrar}
     */
    @Override
    public Collection<ThreadPoolPluginRegistrar> getAllRegistrars() {
        return threadPoolPluginRegistrars.values();
    }

    /**
     * Register a {@link ThreadPoolPluginRegistrar}
     *
     * @param registrar registrar
     * @throws IllegalArgumentException thrown when the plugin id is the same as
     *                                  the id of the registered registrar or the id of the registered plugin
     * @throws NullPointerException     thrown when the registrar is null
     */
    @Override
    public void registerRegistrar(@NonNull ThreadPoolPluginRegistrar registrar) {
        ThreadPoolPluginRegistrar target = getRegistrar(registrar.getId());
        Assert.isNull(target, "ThreadPoolPluginRegistrar or ThreadPoolPlugin [" + registrar.getId() + "] is already registered");
        threadPoolPluginRegistrars.put(registrar.getId(), registrar);
    }

    /**
     * Unregister the specified registrar.
     *
     * @param registrarId registrar id, also be plugin id
     * @return the previous registrar associated with id, or null if there was no mapping for id
     */
    @Nullable
    @Override
    public ThreadPoolPluginRegistrar unregisterRegistrar(String registrarId) {
        return threadPoolPluginRegistrars.remove(registrarId);
    }

    /**
     * Register a {@link ThreadPoolPluginSupport}.
     *
     * @param support thread pool plugin manager support
     * @throws IllegalArgumentException thrown when the thread pool id is same as the id of the registered {@link ThreadPoolPluginSupport}
     */
    @Override
    public void registerSupport(@NonNull ThreadPoolPluginSupport support) {
        ThreadPoolPluginSupport target = threadPoolPluginSupports.get(support.getThreadPoolId());
        Assert.isNull(target, "ThreadPoolPluginSupport [" + target.getThreadPoolId() + "] is already registered");
        threadPoolPluginSupports.put(support.getThreadPoolId(), support);
    }

    /**
     * Get {@link ThreadPoolPluginSupport}.
     *
     * @param threadPoolId thread pool id
     * @return {@link ThreadPoolPluginSupport} if the specified support has been registered, null otherwise
     */
    @Nullable
    @Override
    public ThreadPoolPluginSupport getSupport(String threadPoolId) {
        return threadPoolPluginSupports.get(threadPoolId);
    }

    /**
     * Unregister the specified {@link ThreadPoolPluginSupport}.
     *
     * @param threadPoolId thread pool id
     * @return the previous {@link ThreadPoolPluginSupport} associated with id, or null if there was no mapping for id
     */
    @Nullable
    @Override
    public ThreadPoolPluginSupport unregisterSupport(String threadPoolId) {
        return threadPoolPluginSupports.remove(threadPoolId);
    }

    /**
     * Get all registered {@link ThreadPoolPluginSupport}.
     *
     * @return all registered {@link ThreadPoolPluginSupport}
     */
    @Override
    public Collection<ThreadPoolPluginSupport> getAllSupports() {
        return threadPoolPluginSupports.values();
    }
}
