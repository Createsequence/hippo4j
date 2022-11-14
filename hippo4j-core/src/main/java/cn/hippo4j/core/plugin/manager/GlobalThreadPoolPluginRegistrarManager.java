package cn.hippo4j.core.plugin.manager;

import cn.hippo4j.common.toolkit.ArrayUtil;
import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * <p>Global thread pool plugin registrar. <br />
 * It can also be considered as a global manager of plugin registrar,
 * used to manage singleton plugin-registrars or singleton plugin(after being adapted as a registrar).
 *
 * @see ThreadPoolPluginRegistrar
 * @see SingletonPluginRegistrar
 */
public interface GlobalThreadPoolPluginRegistrarManager extends ThreadPoolPluginRegistrar {

    /**
     * Whether the registrar is adapted by a singleton plugin.
     *
     * @param registrar registrar
     * @return true if the registrar is adapted by a singleton plugin, false otherwise
     */
    static boolean isSingletonPluginRegistrar(ThreadPoolPluginRegistrar registrar) {
        return registrar instanceof SingletonPluginRegistrar;
    }

    /**
     * If the registrar is adapted from a singleton plugin,
     * try to obtain the original plug-in object before adaptation.
     *
     * @param registrar registrar
     * @return {@link ThreadPoolPlugin} if the registrar is adapted by a singleton plugin, null otherwise
     * @see #isSingletonPluginRegistrar
     * @see SingletonPluginRegistrar
     */
    @Nullable
    static ThreadPoolPlugin getSingletonPlugin(ThreadPoolPluginRegistrar registrar) {
        return isSingletonPluginRegistrar(registrar) ? ((SingletonPluginRegistrar)registrar).getPlugin() : null;
    }

    /**
     * Use all registered {@link ThreadPoolPluginRegistrar} to register plugins to {@link ThreadPoolPluginSupport}.
     *
     * @param support thread pool plugin manager delegate
     * @throws NullPointerException thrown when the support is null
     */
    @Override
    default void doRegister(@NonNull ThreadPoolPluginSupport support) {
        getAllRegistrars().forEach(registrar -> registrar.doRegister(support));
    }

    /**
     * Use specific {@link ThreadPoolPluginRegistrar} to register plugins to {@link ThreadPoolPluginSupport}.
     *
     * @param support thread pool plugin manager delegate
     * @param registerIds register ids
     * @throws NullPointerException thrown when the support is null
     */
    default void doRegister(@NonNull ThreadPoolPluginSupport support, String... registerIds) {
        if (ArrayUtil.isEmpty(registerIds)) {
            return;
        }
        Stream.of(registerIds)
            .map(this::getRegistrar)
            .filter(Objects::nonNull)
            .forEach(registrar -> registrar.doRegister(support));
    }

    /**
     * Get {@link ThreadPoolPluginRegistrar}
     *
     * @param registrarId registrarId
     * @return registrar if the specified registrar has been registered, null otherwise
     */
    @Nullable
    ThreadPoolPluginRegistrar getRegistrar(String registrarId);
    
    /**
     * Get all {@link ThreadPoolPluginRegistrar}
     *
     * @return {@link ThreadPoolPluginRegistrar}
     */
    Collection<ThreadPoolPluginRegistrar> getAllRegistrars();

    /**
     * Register a {@link ThreadPoolPluginRegistrar}
     *
     * @param registrar registrar
     * @throws IllegalArgumentException thrown when the plugin id is the same as
     * the id of the registered registrar or the id of the registered plugin
     * @throws NullPointerException thrown when the registrar is null
     */
    void registerRegistrar(@NonNull ThreadPoolPluginRegistrar registrar);

    /**
     * Register a {@link ThreadPoolPlugin}, it will be adapted to a {@link ThreadPoolPluginRegistrar}
     *
     * @param plugin registrar
     * @return {@link SingletonPluginRegistrar}
     * @throws IllegalArgumentException thrown when the plugin id is the same as
     * the id of the registered registrar or the id of the registered plugin
     * @throws NullPointerException thrown when the plugin is null
     * @see SingletonPluginRegistrar
     */
    @NonNull
    default SingletonPluginRegistrar registerSingletonPluginRegistrar(@NonNull ThreadPoolPlugin plugin) {
        SingletonPluginRegistrar adapter = () -> plugin;
        registerRegistrar(adapter);
        return adapter;
    }

    /**
     * Unregister the specified registrar.
     *
     * @param registrarId registrar id, also be plugin id
     * @return the previous registrar associated with id, or null if there was no mapping for id
     */
    @Nullable
    ThreadPoolPluginRegistrar unregisterRegistrar(String registrarId);

    /**
     * Adapt {@link ThreadPoolPlugin} to {@link ThreadPoolPluginRegistrar}.
     */
    @FunctionalInterface
    interface SingletonPluginRegistrar extends ThreadPoolPluginRegistrar {

        /**
         * Get original {@link ThreadPoolPlugin}
         *
         * @return {@link ThreadPoolPlugin}
         */
        ThreadPoolPlugin getPlugin();

        /**
         * Get id
         *
         * @return id
         */
        @Override
        default String getId() {
            return getPlugin().getId();
        }

        /**
         * Create and register plugin for the specified thread-pool instance.
         *
         * @param support thread pool plugin manager delegate
         */
        @Override
        default void doRegister(ThreadPoolPluginSupport support) {
            support.tryRegister(getPlugin());
        }
    }

}
