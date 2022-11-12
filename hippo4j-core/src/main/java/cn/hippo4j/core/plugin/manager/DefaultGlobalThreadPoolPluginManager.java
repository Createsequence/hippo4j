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

import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link GlobalThreadPoolPluginManager}.
 */
public class DefaultGlobalThreadPoolPluginManager implements GlobalThreadPoolPluginManager {

    /**
     * Thread pool plugin supports
     */
    private final Map<String, ThreadPoolPluginSupport> threadPoolPluginSupports = new ConcurrentHashMap<>(32);

    /**
     * Thread pool plugins
     */
    private final Map<String, ThreadPoolPlugin> threadPoolPlugins = new ConcurrentHashMap<>(32);

    /**
     * Thread pool plugin registrars
     */
    private final Map<String, ThreadPoolPluginRegistrar> threadPoolPluginRegistrars = new ConcurrentHashMap<>(32);

    /**
     * Register a {@link ThreadPoolPluginSupport}.
     *
     * @param support thread pool plugin manager support
     * @return true if the support has not been managed before, false otherwise
     */
    @Override
    public boolean registerThreadPoolPluginSupport(@NonNull ThreadPoolPluginSupport support) {
        return Objects.isNull(threadPoolPluginSupports.putIfAbsent(support.getThreadPoolId(), support));
    }

    /**
     * Get all registered {@link ThreadPoolPluginSupport}.
     *
     * @return all registered {@link ThreadPoolPluginSupport}
     */
    @Override
    public Collection<ThreadPoolPluginSupport> getAllThreadPoolPluginSupports() {
        return threadPoolPluginSupports.values();
    }

    /**
     * Get {@link ThreadPoolPluginSupport}.
     *
     * @param threadPoolId thread pool id
     * @return {@link ThreadPoolPluginSupport} if present, null otherwise
     */
    @Override
    public @Nullable ThreadPoolPluginSupport getThreadPoolPluginSupport(String threadPoolId) {
        return threadPoolPluginSupports.get(threadPoolId);
    }

    /**
     * Register a {@link ThreadPoolPlugin}.
     *
     * @param plugin plugin
     * @return true if the plugin has not been enabled before, false otherwise
     */
    @Override
    public boolean registerThreadPoolPlugin(@NonNull ThreadPoolPlugin plugin) {
        return Objects.isNull(threadPoolPlugins.putIfAbsent(plugin.getId(), plugin));
    }

    /**
     * Get {@link ThreadPoolPlugin}.
     *
     * @param pluginId plugin id
     * @return {@link ThreadPoolPlugin} if present, null otherwise
     */
    @Override
    public @Nullable ThreadPoolPlugin getThreadPoolPlugin(String pluginId) {
        return threadPoolPlugins.get(pluginId);
    }

    /**
     * Get all registered {@link ThreadPoolPlugin}.
     *
     * @return all registered {@link ThreadPoolPlugin}
     */
    @Override
    public Collection<ThreadPoolPlugin> getAllThreadPoolPlugins() {
        return threadPoolPlugins.values();
    }

    /**
     * Register a {@link ThreadPoolPluginRegistrar}.
     *
     * @param registrar registrar
     * @return true if the registrar has not been register before, false otherwise
     */
    @Override
    public boolean registerThreadPoolPluginRegistrar(@NonNull ThreadPoolPluginRegistrar registrar) {
        return Objects.isNull(threadPoolPluginRegistrars.putIfAbsent(registrar.getId(), registrar));
    }

    /**
     * Get {@link ThreadPoolPluginRegistrar}.
     *
     * @param registrarId registrar id
     * @return {@link ThreadPoolPluginRegistrar} if present, null otherwise
     */
    @Override
    public @Nullable ThreadPoolPluginRegistrar getThreadPoolPluginRegistrar(String registrarId) {
        return threadPoolPluginRegistrars.get(registrarId);
    }

    /**
     * Get all registered {@link ThreadPoolPluginRegistrar}.
     *
     * @return all registered {@link ThreadPoolPluginRegistrar}.
     */
    @Override
    public Collection<ThreadPoolPluginRegistrar> getAllThreadPoolPluginRegistrars() {
        return threadPoolPluginRegistrars.values();
    }

}
