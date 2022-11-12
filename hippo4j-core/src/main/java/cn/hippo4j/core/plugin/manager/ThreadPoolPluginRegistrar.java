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

/**
 * <p>Registrar of {@link ThreadPoolPlugin}, usually used as a singleton. <br />
 * It is used to uniformly register a group of {@link ThreadPoolPlugin}
 * with associated relationships, or used to register non singleton {@link ThreadPoolPlugin}
 * with one-to-one relationship with {@link ThreadPoolPluginSupport} instances.
 *
 * <p><b>NOTE</b>:
 * One registrar may be applied to multiple {@link ThreadPoolPluginSupport} at the same time,
 * and one {@link ThreadPoolPluginSupport} may sometimes be processed by the same registrar <b>multiple times</b>,
 * Therefore, users should avoid repeatedly registering the same plugin with {@link ThreadPoolPluginSupport}.
 *
 * @see ThreadPoolPlugin
 * @see ThreadPoolPluginSupport
 */
public interface ThreadPoolPluginRegistrar {

    /**
     * Get id
     *
     * @return id
     */
    default String getId() {
        return this.getClass().getSimpleName();
    }

    /**
     * Register plugin for the specified {@link ThreadPoolPluginSupport} instance.
     *
     * @param support {@link ThreadPoolPluginSupport}
     */
    void doRegister(ThreadPoolPluginSupport support);
}
