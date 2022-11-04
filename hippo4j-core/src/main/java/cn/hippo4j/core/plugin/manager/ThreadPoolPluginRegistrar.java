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
 * <p>Registrar of {@link ThreadPoolPlugin}. <br />
 * Generally, it is used to register non singleton plugins,
 * or a group of plugins with specific associations can be configured in a centralized way.
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
     * Create and register plugin for the specified thread-pool instance.
     *
     * @param support thread pool plugin manager delegate
     */
    void doRegister(ThreadPoolPluginSupport support);

}
