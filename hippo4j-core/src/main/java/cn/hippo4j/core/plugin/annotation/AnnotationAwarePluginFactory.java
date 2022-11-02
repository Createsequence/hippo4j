package cn.hippo4j.core.plugin.annotation;

import cn.hippo4j.core.plugin.ThreadPoolPlugin;

import java.lang.reflect.Method;

/**
 * Factory of annotation aware plugin.
 *
 * @author huangchengxing
 * @see AnnotationAwarePluginProcessor
 */
public interface AnnotationAwarePluginFactory {

    /**
     * Does the factory support converting this method into a plugin.
     *
     * @param beanHolder bean holder
     * @param method method
     * @param annotation annotation
     * @return return ture if factory support, false otherwise
     */
    boolean support(BeanHolder beanHolder, Method method, ThreadPoolPluginAdapt annotation);

    /**
     * Create a {@link ThreadPoolPlugin} instance by {@link ThreadPoolPluginAdapt}
     *
     * @param beanHolder bean holder
     * @param method method
     * @param annotation annotation
     * @return {@link ThreadPoolPlugin}
     */
    ThreadPoolPlugin createThreadPoolPlugin(BeanHolder beanHolder, Method method, ThreadPoolPluginAdapt annotation);

}
