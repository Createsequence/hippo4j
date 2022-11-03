package cn.hippo4j.core.plugin.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Mark a method as a stateless singleton {@link ThreadPoolPluginAdapt}.
 *
 * @author huang
 * @see cn.hippo4j.core.plugin.ThreadPoolPlugin
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface ThreadPoolPluginAdapt {

    /**
     * Get plugin id.
     * If it is empty, it defaults to the annotated method name.
     *
     * @return plugin id
     */
    @AliasFor("pluginId")
    String value() default "";

    /**
     * Get plugin id.
     * If it is empty, it defaults to the annotated method name.
     *
     * @return plugin id
     */
    @AliasFor("value")
    String pluginId() default "";

    /**
     * Get plugin name
     *
     * @return plugin name
     */
    String pluginName() default "";

    /**
     * Indicates which actions the method allows to sense the thread-pool.
     *
     * @return aware type
     */
    Aware awareType();

    /**
     * This method applies to which thread pools.
     * If it is empty, it applies to all thread pools by default.
     *
     * @return thread pool ids
     */
    String[] enableThreadPoolIds() default {};

    /**
     * Indicates which actions the plugin allows to sense the thread-pool.
     */
    enum Aware {

        /**
         * Equivalent to {@link cn.hippo4j.core.plugin.TaskAwarePlugin#beforeTaskExecute}.
         */
        BEFORE_TASK_EXECUTE,

        /**
         * Equivalent to {@link cn.hippo4j.core.plugin.ExecuteAwarePlugin#beforeExecute}.
         */
        BEFORE_EXECUTE,

        /**
         * Equivalent to {@link cn.hippo4j.core.plugin.ExecuteAwarePlugin#afterExecute}.
         */
        AFTER_EXECUTE,

        /**
         * Equivalent to {@link cn.hippo4j.core.plugin.ShutdownAwarePlugin#beforeShutdown}
         */
        BEFORE_SHUTDOWN,

        /**
         * Equivalent to {@link cn.hippo4j.core.plugin.ShutdownAwarePlugin#afterShutdown}
         */
        AFTER_SHUTDOWN,

        /**
         * Equivalent to {@link cn.hippo4j.core.plugin.ShutdownAwarePlugin#afterTerminated}
         */
        AFTER_TERMINATED,

        /**
         * Equivalent to {@link cn.hippo4j.core.plugin.RejectedAwarePlugin#beforeRejectedExecution}.
         */
        BEFORE_REJECTED_EXECUTION;

    }

}
