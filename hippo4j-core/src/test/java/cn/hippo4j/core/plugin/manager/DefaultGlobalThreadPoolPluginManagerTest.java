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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * test for {@link DefaultGlobalThreadPoolPluginManager}
 */
public class DefaultGlobalThreadPoolPluginManagerTest {

    private GlobalThreadPoolPluginManager globalThreadPoolPluginManager;

    @Before
    public void initManager() {
        globalThreadPoolPluginManager = new DefaultGlobalThreadPoolPluginManager();
    }

    @Test
    public void testRegisterThreadPoolPluginSupport() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));
        Assert.assertFalse(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));
    }

    @Test
    public void testGetThreadPoolPluginSupport() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));
        Assert.assertSame(support, globalThreadPoolPluginManager.getThreadPoolPluginSupport(support.getThreadPoolId()));
    }

    @Test
    public void testGetAllThreadPoolPluginSupports() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));
        Assert.assertEquals(1, globalThreadPoolPluginManager.getAllThreadPoolPluginSupports().size());
    }

    @Test
    public void testRegisterThreadPoolPlugin() {
        ThreadPoolPlugin plugin = new TestPlugin("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(plugin));
        Assert.assertFalse(globalThreadPoolPluginManager.registerThreadPoolPlugin(plugin));
    }

    @Test
    public void testGetThreadPoolPlugin() {
        ThreadPoolPlugin plugin = new TestPlugin("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(plugin));
        Assert.assertSame(plugin, globalThreadPoolPluginManager.getThreadPoolPlugin(plugin.getId()));
    }

    @Test
    public void testGetAllThreadPoolPlugins() {
        ThreadPoolPlugin plugin = new TestPlugin("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(plugin));
        Assert.assertEquals(1, globalThreadPoolPluginManager.getAllThreadPoolPlugins().size());
    }

    @Test
    public void testRegisterThreadPoolPluginRegistrar() {
        ThreadPoolPluginRegistrar registrar = new TestRegistrar("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(registrar));
        Assert.assertFalse(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(registrar));
    }

    @Test
    public void testGetThreadPoolPluginRegistrar() {
        ThreadPoolPluginRegistrar registrar = new TestRegistrar("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(registrar));
        Assert.assertSame(registrar, globalThreadPoolPluginManager.getThreadPoolPluginRegistrar(registrar.getId()));
    }

    @Test
    public void testGetAllThreadPoolPluginRegistrars() {
        ThreadPoolPluginRegistrar registrar = new TestRegistrar("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(registrar));
        Assert.assertEquals(1, globalThreadPoolPluginManager.getAllThreadPoolPluginRegistrars().size());
    }

    @Test
    public void testEnableThreadPoolPluginForAll() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));
        Assert.assertFalse(globalThreadPoolPluginManager.enableThreadPoolPluginForAll("test"));
        ThreadPoolPlugin plugin = new TestPlugin("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(plugin));
        Assert.assertTrue(globalThreadPoolPluginManager.enableThreadPoolPluginForAll("test"));
        Assert.assertTrue(support.getPlugin(plugin.getId()).isPresent());
    }

    @Test
    public void testEnableThreadPoolPlugin() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));
        Assert.assertFalse(globalThreadPoolPluginManager.enableThreadPoolPlugin("test", "test"));
        ThreadPoolPlugin plugin = new TestPlugin("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(plugin));
        Assert.assertTrue(globalThreadPoolPluginManager.enableThreadPoolPlugin("test", "test"));
        Assert.assertTrue(support.getPlugin(plugin.getId()).isPresent());
    }

    @Test
    public void testDisableThreadPoolPluginForAll() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));

        Assert.assertFalse(globalThreadPoolPluginManager.disableThreadPoolPluginForAll("test"));
        ThreadPoolPlugin plugin = new TestPlugin("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(plugin));
        Assert.assertTrue(globalThreadPoolPluginManager.enableThreadPoolPlugin("test", "test"));

        Assert.assertTrue(globalThreadPoolPluginManager.disableThreadPoolPluginForAll("test"));
        Assert.assertTrue(support.getAllPlugins().isEmpty());
    }

    @Test
    public void testDisableThreadPoolPlugin() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));

        Assert.assertFalse(globalThreadPoolPluginManager.disableThreadPoolPlugin("test", "test"));
        ThreadPoolPlugin plugin = new TestPlugin("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(plugin));
        Assert.assertTrue(globalThreadPoolPluginManager.enableThreadPoolPlugin("test", "test"));

        Assert.assertTrue(globalThreadPoolPluginManager.disableThreadPoolPlugin("test", "test"));
        Assert.assertTrue(support.getAllPlugins().isEmpty());
    }

    @Test
    public void testEnableThreadPoolPluginRegistrarForAll() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));

        Assert.assertFalse(globalThreadPoolPluginManager.enableThreadPoolPluginRegistrarForAll("test"));
        ThreadPoolPluginRegistrar registrar = new TestRegistrar("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(registrar));
        Assert.assertTrue(globalThreadPoolPluginManager.enableThreadPoolPluginRegistrarForAll("test"));

        Assert.assertTrue(support.getPlugin(TestRegistrar.PLUGIN_ID).isPresent());
    }

    @Test
    public void testEnableThreadPoolPluginRegistrar() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));

        Assert.assertFalse(globalThreadPoolPluginManager.enableThreadPoolPluginRegistrar("test", "test"));
        ThreadPoolPluginRegistrar registrar = new TestRegistrar("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(registrar));
        Assert.assertTrue(globalThreadPoolPluginManager.enableThreadPoolPluginRegistrar("test", "test"));

        Assert.assertTrue(support.getPlugin(TestRegistrar.PLUGIN_ID).isPresent());
    }

    @Test
    public void testDoRegister() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(new TestPlugin("test")));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(new TestRegistrar("test")));

        globalThreadPoolPluginManager.doRegister(support);
        Assert.assertEquals(2, support.getAllPlugins().size());
    }

    @Test
    public void testDoRegisterForAll() {
        ThreadPoolPluginSupport support = new TestSupport("test");
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(support));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(new TestPlugin("test")));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(new TestRegistrar("test")));

        globalThreadPoolPluginManager.doRegisterForAll();
        Assert.assertEquals(2, support.getAllPlugins().size());
    }

    @Test
    public void testGetAllPluginsFromAllManagers() {
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(new TestSupport("test1")));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(new TestSupport("test2")));

        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(new TestPlugin("test")));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(new TestRegistrar("test")));
        globalThreadPoolPluginManager.doRegisterForAll();

        Assert.assertEquals(4, globalThreadPoolPluginManager.getAllPluginsFromAllManagers().size());
    }

    @Test
    public void getPluginsFromAllManagersByType() {
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(new TestSupport("test1")));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(new TestSupport("test2")));

        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(new TestPlugin("test")));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(new TestRegistrar("test")));
        globalThreadPoolPluginManager.doRegisterForAll();

        Assert.assertEquals(4, globalThreadPoolPluginManager.getPluginsFromAllManagers(TestPlugin.class).size());
    }

    @Test
    public void getPluginsFromAllManagersById() {
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(new TestSupport("test1")));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginSupport(new TestSupport("test2")));

        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPlugin(new TestPlugin("test")));
        Assert.assertTrue(globalThreadPoolPluginManager.registerThreadPoolPluginRegistrar(new TestRegistrar("test")));
        globalThreadPoolPluginManager.doRegisterForAll();

        Assert.assertEquals(2, globalThreadPoolPluginManager.getPluginsFromAllManagers("test").size());
    }

    @RequiredArgsConstructor
    @Getter
    private static class TestSupport implements ThreadPoolPluginSupport {

        private final String threadPoolId;
        private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        private final ThreadPoolPluginManager threadPoolPluginManager = new DefaultThreadPoolPluginManager();
    }

    @Getter
    @RequiredArgsConstructor
    private static class TestRegistrar implements ThreadPoolPluginRegistrar {

        static final String PLUGIN_ID = "TestRegistrar";
        private final String id;
        @Override
        public void doRegister(ThreadPoolPluginSupport support) {
            support.register(new TestPlugin(PLUGIN_ID));
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class TestPlugin implements ThreadPoolPlugin {

        private final String id;
    }

}
