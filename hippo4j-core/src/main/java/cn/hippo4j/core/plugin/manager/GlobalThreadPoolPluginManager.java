/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.hippo4j.core.plugin.manager;

import cn.hippo4j.common.toolkit.ArrayUtil;
import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>A global thread-pool plugin manager. <br />
 * It is used to uniformly manage {@link ThreadPoolPlugin}, {@link ThreadPoolPluginRegistrar}
 * and {@link ThreadPoolPluginSupport} in the spring's context,
 * and provide support for batch registration, batch logout
 * and plugin batch acquisition from {@link ThreadPoolPluginSupport}.
 */
public interface GlobalThreadPoolPluginManager extends ThreadPoolPluginRegistrar {

    /**
     * Register plugins with {@link ThreadPoolPluginSupport}
     * and apply all registrars for {@link ThreadPoolPluginSupport}.
     *
     * @param support thread pool plugin manager delegate
     */
    @Override
    default void doRegister(ThreadPoolPluginSupport support) {
        getAllThreadPoolPlugins().forEach(support::register);
        getAllThreadPoolPluginRegistrars().forEach(registrar -> registrar.doRegister(support));
    }

    /**
     * Register plugins with {@link ThreadPoolPluginSupport}
     * and apply all registrars for all registered {@link ThreadPoolPluginSupport}.
     */
    default void doRegisterForAll() {
        getAllThreadPoolPluginSupports().forEach(this::doRegister);
    }

    /**
     * Register a {@link ThreadPoolPluginSupport}.
     *
     * @param support thread pool plugin manager support
     * @return true if the support has not been managed before, false otherwise
     */
    boolean registerThreadPoolPluginSupport(@NonNull ThreadPoolPluginSupport support);

    /**
     * Get all registered {@link ThreadPoolPluginSupport}.
     *
     * @return all registered {@link ThreadPoolPluginSupport}
     */
    Collection<ThreadPoolPluginSupport> getAllThreadPoolPluginSupports();

    /**
     * Get {@link ThreadPoolPluginSupport}.
     *
     * @param threadPoolId thread pool id
     * @return {@link ThreadPoolPluginSupport} if present, null otherwise
     */
    @Nullable
    ThreadPoolPluginSupport getThreadPoolPluginSupport(String threadPoolId);

    /**
     * Register a {@link ThreadPoolPlugin}.
     *
     * @param plugin plugin
     * @return true if the plugin has not been enabled before, false otherwise
     */
    boolean registerThreadPoolPlugin(@NonNull ThreadPoolPlugin plugin);

    /**
     * Get {@link ThreadPoolPlugin}.
     *
     * @param pluginId plugin id
     * @return {@link ThreadPoolPlugin} if present, null otherwise
     */
    @Nullable
    ThreadPoolPlugin getThreadPoolPlugin(String pluginId);

    /**
     * Get all registered {@link ThreadPoolPlugin}.
     *
     * @return all registered {@link ThreadPoolPlugin}
     */
    Collection<ThreadPoolPlugin> getAllThreadPoolPlugins();

    /**
     * Register a {@link ThreadPoolPluginRegistrar}.
     *
     * @param registrar registrar
     * @return true if the registrar has not been register before, false otherwise
     */
    boolean registerThreadPoolPluginRegistrar(@NonNull ThreadPoolPluginRegistrar registrar);

    /**
     * Get {@link ThreadPoolPluginRegistrar}.
     *
     * @param registrarId registrar id
     * @return {@link ThreadPoolPluginRegistrar} if present, null otherwise
     */
    @Nullable
    ThreadPoolPluginRegistrar getThreadPoolPluginRegistrar(String registrarId);

    /**
     * Get all registered {@link ThreadPoolPluginRegistrar}.
     *
     * @return all registered {@link ThreadPoolPluginRegistrar}.
     */
    Collection<ThreadPoolPluginRegistrar> getAllThreadPoolPluginRegistrars();

    // ===================== plugin =====================



    /**
     * Register plugin for all {@link ThreadPoolPluginSupport}.
     *
     * @param pluginId plugin id
     * @return true if the registrar has been registered, false otherwise
     */
    default boolean useThreadPoolPluginForAll(String pluginId) {
        ThreadPoolPlugin plugin = getThreadPoolPlugin(pluginId);
        if (Objects.isNull(plugin)) {
            return false;
        }
        getAllThreadPoolPluginSupports().forEach(support -> support.register(plugin));
        return true;
    }

    /**
     * Register plugin for specific {@link ThreadPoolPluginSupport}.
     *
     * @param pluginId plugin id
     * @param threadPoolIds thread pool ids
     * @return true if the registrar has been registered, false otherwise
     */
    default boolean useThreadPoolPlugin(String pluginId, String... threadPoolIds) {
        ThreadPoolPlugin plugin = getThreadPoolPlugin(pluginId);
        if (Objects.isNull(plugin)) {
            return false;
        }
        if (ArrayUtil.isNotEmpty(threadPoolIds)) {
            Arrays.stream(threadPoolIds)
                .map(this::getThreadPoolPluginSupport)
                .filter(Objects::nonNull)
                .forEach(support -> support.register(plugin));
        }
        return true;
    }

    /**
     * Enable plugin for all {@link ThreadPoolPluginSupport}.
     *
     * @param pluginId plugin id
     */
    default void enableThreadPoolPluginForAll(String pluginId) {
        getAllThreadPoolPluginSupports().forEach(support -> support.enable(pluginId));
    }

    /**
     * Enable plugin for specific {@link ThreadPoolPluginSupport}.
     *
     * @param pluginId plugin id
     */
    default void enableThreadPoolPlugin(String pluginId, String... threadPoolIds) {
        if (ArrayUtil.isNotEmpty(threadPoolIds)) {
            Arrays.stream(threadPoolIds)
                .map(this::getThreadPoolPluginSupport)
                .filter(Objects::nonNull)
                .forEach(support -> support.enable(pluginId));
        }
    }

    /**
     * Disable plugin for all {@link ThreadPoolPluginSupport}.
     *
     * @param pluginId plugin id
     */
    default void disableThreadPoolPluginForAll(String pluginId) {
        getAllThreadPoolPluginSupports().forEach(support -> support.disable(pluginId));
    }

    /**
     * Disable plugin for specific {@link ThreadPoolPlugin}.
     *
     * @param pluginId plugin id
     * @param threadPoolIds thread pool ids
     */
    default void disableThreadPoolPlugin(String pluginId, String... threadPoolIds) {
        if (ArrayUtil.isNotEmpty(threadPoolIds)) {
            Arrays.stream(threadPoolIds)
                .map(this::getThreadPoolPluginSupport)
                .filter(Objects::nonNull)
                .forEach(support -> support.unregister(pluginId));
        }
    }

    // ===================== registrar =====================

    /**
     * Use registrar for all {@link ThreadPoolPluginSupport}.
     *
     * @param registrarId registrar id
     * @return true if the registrar has been registered, false otherwise
     */
    default boolean useThreadPoolPluginRegistrarForAll(String registrarId) {
        ThreadPoolPluginRegistrar registrar = getThreadPoolPluginRegistrar(registrarId);
        if (Objects.isNull(registrar)) {
            return false;
        }
        getAllThreadPoolPluginSupports().forEach(registrar::doRegister);
        return true;
    }

    /**
     * Use registrar for specific {@link ThreadPoolPluginSupport}.
     *
     * @param registrarId registrar id
     * @param threadPoolIds thread pool ids
     * @return true if the registrar has been registered, false otherwise
     */
    default boolean useThreadPoolPluginRegistrar(String registrarId, String... threadPoolIds) {
        ThreadPoolPluginRegistrar registrar = getThreadPoolPluginRegistrar(registrarId);
        if (Objects.isNull(registrar)) {
            return false;
        }
        if (ArrayUtil.isNotEmpty(threadPoolIds)) {
            Arrays.stream(threadPoolIds)
                .map(this::getThreadPoolPluginSupport)
                .filter(Objects::nonNull)
                .forEach(registrar::doRegister);
        }
        return true;
    }

    // ===================== get from support =====================

    /**
     * Get all plugins from registered {@link ThreadPoolPluginSupport}.
     *
     * @return plugins
     */
    default Collection<ThreadPoolPlugin> getAllPluginsFromAllManagers() {
        return getAllThreadPoolPluginSupports().stream()
            .map(ThreadPoolPluginSupport::getAllPlugins)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Get plugins of type from registered {@link ThreadPoolPluginSupport}.
     *
     * @param pluginType plugin type
     * @return plugins
     */
    default <T extends ThreadPoolPlugin> Collection<T> getPluginsFromAllManagers(Class<T> pluginType) {
        return getAllThreadPoolPluginSupports().stream()
            .map(support -> support.getAllPluginsOfType(pluginType))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Get plugins by id from registered {@link ThreadPoolPluginSupport}.
     *
     * @param pluginId plugin id
     * @return plugins
     */
    default Collection<ThreadPoolPlugin> getPluginsFromAllManagers(String pluginId) {
        return getAllThreadPoolPluginSupports().stream()
            .map(support -> support.getPlugin(pluginId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

}
