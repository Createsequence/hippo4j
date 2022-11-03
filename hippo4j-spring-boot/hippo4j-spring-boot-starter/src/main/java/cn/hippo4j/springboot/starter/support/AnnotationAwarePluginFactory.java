package cn.hippo4j.springboot.starter.support;

import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import cn.hippo4j.core.plugin.annotation.ThreadPoolPluginAdapt;

import java.lang.reflect.Method;

/**
 * Factory of annotation aware plugin.
 *
 * @author huangchengxing
 */
public interface AnnotationAwarePluginFactory {

    /**
     * Does the factory support converting this method into a plugin.
     *
     * @param beanName bean name
     * @param beanType bean type
     * @param method method
     * @param annotation annotation
     * @return return ture if factory support, false otherwise
     */
    boolean support(String beanName, Class<?> beanType, Method method, ThreadPoolPluginAdapt annotation);

    /**
     * Create a {@link ThreadPoolPlugin} instance by {@link ThreadPoolPluginAdapt}
     *
     * @param beanName bean name
     * @param beanType bean type
     * @param method method
     * @param annotation annotation
     * @return {@link ThreadPoolPlugin}
     */
    ThreadPoolPlugin createThreadPoolPlugin(String beanName, Class<?> beanType, Method method, ThreadPoolPluginAdapt annotation);

}
