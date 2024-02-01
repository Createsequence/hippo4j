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

package cn.hippo4j.core.executor.plugin.manager;

import cn.hippo4j.common.toolkit.CollectionUtil;
import cn.hippo4j.core.executor.plugin.impl.TaskDecoratorPlugin;
import cn.hippo4j.core.executor.plugin.impl.TaskRejectCountRecordPlugin;
import cn.hippo4j.core.executor.plugin.impl.TaskRejectNotifyAlarmPlugin;
import cn.hippo4j.core.executor.plugin.impl.TaskTimeoutNotifyAlarmPlugin;
import cn.hippo4j.core.executor.plugin.impl.ThreadPoolExecutorShutdownPlugin;
import org.springframework.core.task.TaskDecorator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Standard plugins for {@link cn.hippo4j.core.executor.DynamicThreadPoolExecutor}.
 *
 * @see TaskDecoratorPlugin
 * @see TaskTimeoutNotifyAlarmPlugin
 * @see TaskRejectCountRecordPlugin
 * @see TaskRejectNotifyAlarmPlugin
 * @see ThreadPoolExecutorShutdownPlugin
 */
public class DynamicThreadPoolExecutorStandardPlugins implements ThreadPoolPluginRegistrar {

    /**
     * Execute time out
     */
    private final long executeTimeOut;

    /**
     * Await termination millis
     */
    private final long awaitTerminationMillis;

    /**
     * Task decorator plugin
     */
    private TaskDecoratorPlugin taskDecoratorPlugin;

    /**
     * Task timeout notify alarm plugin
     */
    private TaskTimeoutNotifyAlarmPlugin taskTimeoutNotifyAlarmPlugin;

    /**
     * Task reject count record plugin
     */
    private TaskRejectCountRecordPlugin taskRejectCountRecordPlugin;

    /**
     * Thread pool executor shutdown plugin
     */
    private ThreadPoolExecutorShutdownPlugin threadPoolExecutorShutdownPlugin;

    /**
     * Create {@link DynamicThreadPoolExecutorStandardPlugins} instance.
     *
     * @param executeTimeOut execute time out
     * @param awaitTerminationMillis await termination millis
     */
    public DynamicThreadPoolExecutorStandardPlugins(long executeTimeOut, long awaitTerminationMillis) {
        this.executeTimeOut = executeTimeOut;
        this.awaitTerminationMillis = awaitTerminationMillis;
    }

    /**
     * Create and register plugin for the specified thread-pool instance.
     *
     * @param support thread pool plugin manager delegate
     */
    @Override
    public void doRegister(ThreadPoolPluginSupport support) {
        TaskRejectNotifyAlarmPlugin taskRejectNotifyAlarmPlugin;
        // create and register plugin
        this.taskDecoratorPlugin = new TaskDecoratorPlugin();
        support.register(taskDecoratorPlugin);
        this.taskTimeoutNotifyAlarmPlugin = new TaskTimeoutNotifyAlarmPlugin(support.getThreadPoolId(), executeTimeOut, support.getThreadPoolExecutor());
        support.register(taskTimeoutNotifyAlarmPlugin);
        this.taskRejectCountRecordPlugin = new TaskRejectCountRecordPlugin();
        support.register(taskRejectCountRecordPlugin);
        taskRejectNotifyAlarmPlugin = new TaskRejectNotifyAlarmPlugin();
        support.register(taskRejectNotifyAlarmPlugin);
        this.threadPoolExecutorShutdownPlugin = new ThreadPoolExecutorShutdownPlugin(awaitTerminationMillis);
        support.register(threadPoolExecutorShutdownPlugin);
    }

    /**
     * Get await termination millis.
     *
     * @return await termination millis.
     */
    public long getAwaitTerminationMillis() {
        return threadPoolExecutorShutdownPlugin.getAwaitTerminationMillis();
    }

    /**
     * Set await termination millis.
     *
     * @param awaitTerminationMillis           await termination millis
     */
    public void setAwaitTerminationMillis(long awaitTerminationMillis) {
        threadPoolExecutorShutdownPlugin.setAwaitTerminationMillis(awaitTerminationMillis);
    }

    /**
     * Get reject count num.
     *
     * @return reject count num
     */
    public Long getRejectCountNum() {
        return taskRejectCountRecordPlugin.getRejectCountNum();
    }

    /**
     * Get reject count.
     *
     * @return reject count num
     */
    public AtomicLong getRejectCount() {
        return taskRejectCountRecordPlugin.getRejectCount();
    }

    /**
     * Get execute time out.
     *
     * @return execute time out
     */
    public Long getExecuteTimeOut() {
        return taskTimeoutNotifyAlarmPlugin.getExecuteTimeOut();
    }

    /**
     * Set execute time out.
     *
     * @param executeTimeOut execute time out
     */
    public void setExecuteTimeOut(Long executeTimeOut) {
        taskTimeoutNotifyAlarmPlugin.setExecuteTimeOut(executeTimeOut);
    }

    /**
     * Get {@link TaskDecorator}.
     *
     * @return task decorator
     */
    public TaskDecorator getTaskDecorator() {
        return CollectionUtil.getFirst(taskDecoratorPlugin.getDecorators());
    }

    /**
     * Set {@link TaskDecorator}.
     *
     * @param taskDecorator task decorator
     */
    public void setTaskDecorator(TaskDecorator taskDecorator) {
        taskDecoratorPlugin.clearDecorators();
        taskDecoratorPlugin.addDecorator(taskDecorator);
    }
}
