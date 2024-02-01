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

package cn.hippo4j.core.executor;

import cn.hippo4j.core.executor.plugin.manager.DefaultThreadPoolPluginManager;
import cn.hippo4j.core.executor.plugin.manager.DynamicThreadPoolExecutorStandardPlugins;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.task.TaskDecorator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced dynamic and monitored thread pool.
 *
 * @see DynamicThreadPoolExecutorStandardPlugins
 */
@Slf4j
public class DynamicThreadPoolExecutor extends ExtensibleThreadPoolExecutor implements DisposableBean {

    /**
     * A flag used to indicate whether destroy() method has been called,
     * after the flag is set to false, calling destroy() method again will not take effect
     */
    private final AtomicBoolean active;

    /**
     * Wait for tasks to complete on shutdown
     */
    @Getter
    @Setter
    private boolean waitForTasksToCompleteOnShutdown;

    /**
     * <p>The standard plugins of thread-pool.<br/>
     * we trust that the standard plugins will not be modified after executor initialization,
     * so we can configure the properties of the standard plugins through the apis of executor directly.
     */
    private final DynamicThreadPoolExecutorStandardPlugins standardPlugins;

    /**
     * Creates a new {@code DynamicThreadPoolExecutor} with the given initial parameters.
     *
     * @param threadPoolId                     thread-pool id
     * @param executeTimeOut                   execute time out
     * @param waitForTasksToCompleteOnShutdown wait for tasks to complete on shutdown
     * @param awaitTerminationMillis           await termination millis
     * @param corePoolSize                     the number of threads to keep in the pool, even
     *                                         if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize                  the maximum number of threads to allow in the
     *                                         pool
     * @param keepAliveTime                    when the number of threads is greater than
     *                                         the core, this is the maximum time that excess idle threads
     *                                         will wait for new tasks before terminating.
     * @param unit                             the time unit for the {@code keepAliveTime} argument
     * @param blockingQueue                    the queue to use for holding tasks before they are
     *                                         executed.  This queue will hold only the {@code Runnable}
     *                                         tasks submitted by the {@code execute} method.
     * @param threadFactory                    the factory to use when the executor creates a new thread
     * @param rejectedExecutionHandler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException     if {@code workQueue}
     *                                  or {@code threadFactory} or {@code handler} is null
     */
    public DynamicThreadPoolExecutor(
                                     int corePoolSize, int maximumPoolSize,
                                     long keepAliveTime, TimeUnit unit,
                                     long executeTimeOut, boolean waitForTasksToCompleteOnShutdown, long awaitTerminationMillis,
                                     @NonNull BlockingQueue<Runnable> blockingQueue,
                                     @NonNull String threadPoolId,
                                     @NonNull ThreadFactory threadFactory,
                                     @NonNull RejectedExecutionHandler rejectedExecutionHandler) {
        super(
                threadPoolId, new DefaultThreadPoolPluginManager().setPluginComparator(AnnotationAwareOrderComparator.INSTANCE),
                corePoolSize, maximumPoolSize, keepAliveTime, unit,
                blockingQueue, threadFactory, rejectedExecutionHandler);
        log.info("Initializing ExecutorService '{}'", threadPoolId);
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        this.active = new AtomicBoolean(false);
        // Init default plugins.
        this.standardPlugins = new DynamicThreadPoolExecutorStandardPlugins(executeTimeOut, awaitTerminationMillis);
        standardPlugins.doRegister(this);
        this.active.set(true);
    }

    /**
     * <p>Whether the current instance is in the active state. <br />
     * It returns false when the xx method is called at least once.
     *
     * @return true if current instance is in the active state, false otherwise
     */
    public boolean isActive() {
        return active.get();
    }

    /**
     * Invoked by the containing {@code BeanFactory} on destruction of a bean.
     */
    @Override
    public void destroy() {
        // instance has been destroyed, not need to call this method again
        if (!isActive()) {
            log.warn("Failed to destroy ExecutorService '{}' because it has already been destroyed", getThreadPoolId());
            return;
        }
        if (isWaitForTasksToCompleteOnShutdown()) {
            super.shutdown();
        } else {
            super.shutdownNow();
        }
        getThreadPoolPluginManager().clear();
        log.info("ExecutorService '{}' has been destroyed", getThreadPoolId());

        // modify the flag to false avoid the method being called repeatedly
        active.set(false);
    }

    /**
     * Get await termination millis.
     *
     * @return await termination millis.
     */
    public long getAwaitTerminationMillis() {
        return standardPlugins.getAwaitTerminationMillis();
    }

    /**
     * Set support param.
     *
     * @param awaitTerminationMillis           await termination millis
     * @param waitForTasksToCompleteOnShutdown wait for tasks to complete on shutdown
     */
    public void setSupportParam(long awaitTerminationMillis, boolean waitForTasksToCompleteOnShutdown) {
        setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        standardPlugins.setAwaitTerminationMillis(awaitTerminationMillis);
    }

    /**
     * Get reject count num.
     *
     * @return reject count num
     */
    public Long getRejectCountNum() {
        return standardPlugins.getRejectCountNum();
    }

    /**
     * Get reject count.
     *
     * @return reject count num
     */
    public AtomicLong getRejectCount() {
        return standardPlugins.getRejectCount();
    }

    /**
     * Get execute time out.
     *
     * @return execute time out
     */
    public Long getExecuteTimeOut() {
        return standardPlugins.getExecuteTimeOut();
    }

    /**
     * Set execute time out.
     *
     * @param executeTimeOut execute time out
     */
    public void setExecuteTimeOut(Long executeTimeOut) {
        standardPlugins.setExecuteTimeOut(executeTimeOut);
    }

    /**
     * Get {@link TaskDecorator}.
     *
     * @return task decorator
     */
    public TaskDecorator getTaskDecorator() {
        return standardPlugins.getTaskDecorator();
    }

    /**
     * Set {@link TaskDecorator}.
     *
     * @param taskDecorator task decorator
     */
    public void setTaskDecorator(TaskDecorator taskDecorator) {
        standardPlugins.setTaskDecorator(taskDecorator);
    }

    /**
     * Get rejected execution handler.
     *
     * @deprecated use {@link #getRejectedExecutionHandler}
     */
    @Deprecated
    public RejectedExecutionHandler getRedundancyHandler() {
        return getRejectedExecutionHandler();
    }

    /**
     * Set rejected execution handler.
     *
     * @param handler handler
     * @deprecated use {@link #setRejectedExecutionHandler}
     */
    @Deprecated
    public void setRedundancyHandler(RejectedExecutionHandler handler) {
        setRejectedExecutionHandler(handler);
    }
}
