package cn.hippo4j.core.plugin.annotation;

import cn.hippo4j.common.toolkit.Assert;
import cn.hippo4j.common.toolkit.CollectionUtil;
import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapt the method annotated by {@link ThreadPoolPluginAdapt} to the {@link ThreadPoolPlugin} instance.
 *
 * @see ThreadPoolPluginAdapt
 * @see AnnotationAwarePluginFactory
 */
@Slf4j
public class AnnotationAwarePluginProcessor implements BeanPostProcessor, BeanFactoryAware {

    /**
     * application context
     */
    private ConfigurableListableBeanFactory beanFactory;

    /**
     * factories
     */
    private List<AnnotationAwarePluginFactory> factories;

    /**
     * ignore types
     */
    private final Set<Class<?>> ignoreTypes = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    /**
     * plugins
     */
    @Getter
    private final Set<ThreadPoolPlugin> plugins = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    /**
     * Check whether there is any annotation directly or indirectly marked by {@link ThreadPoolPluginAdapt} in the bean,
     * and adapt it to {@link cn.hippo4j.core.plugin.ThreadPoolPlugin}.
     *
     * @param bean     the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one;
     * if {@code null}, no subsequent BeanPostProcessors will be invoked
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (CollectionUtil.isEmpty(factories)) {
            return bean;
        }

        // resolve bean type
        Class<?> beanType = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
        if (ignoreTypes.contains(beanType)) {
            return bean;
        }
        if (Objects.isNull(beanType)) {
            log.warn("cannot resolve type for bean [{}]", beanName);
            return bean;
        }

        // resolve annotated methods
        Map<Method, ThreadPoolPluginAdapt> annotatedMethods = resolvedAnnotatedMethods(beanType);
        if (CollectionUtil.isEmpty(annotatedMethods)) {
            if (log.isDebugEnabled()) {
                log.debug("Class [{}] does not have any methods annotated by @ThreadPoolPluginAdapt", beanType);
            }
            ignoreTypes.add(beanType);
            return bean;
        }

        // create and cache plugin instance for methods
        BeanHolder beanHolder = new BeanHolder(beanFactory, beanType, beanName);
        annotatedMethods.forEach((method, annotation) -> factories.stream()
            .filter(f -> f.support(beanHolder, method, annotation))
            .findFirst()
            .map(f -> f.createThreadPoolPlugin(beanHolder, method, annotation))
            .ifPresent(plugins::add)
        );

        return bean;
    }

    /**
     * Set the ApplicationContext, then find and caching all {@link AnnotationAwarePluginFactory}.
     *
     * @param beanFactory owning BeanFactory (never {@code null}).
     *                    The bean can immediately call methods on the factory.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isTrue(
            beanFactory instanceof ConfigurableListableBeanFactory,
            "factory cannot cast to ConfigurableListableBeanFactory"
        );
        this.beanFactory = (ConfigurableListableBeanFactory)beanFactory;
        initPluginFactory();
    }

    private Map<Method, ThreadPoolPluginAdapt> resolvedAnnotatedMethods(Class<?> beanType) {
        return MethodIntrospector.selectMethods(
            beanType, (MethodIntrospector.MetadataLookup<ThreadPoolPluginAdapt>) method ->
                AnnotatedElementUtils.findMergedAnnotation(method, ThreadPoolPluginAdapt.class)
        );
    }

    private void initPluginFactory() {
        List<AnnotationAwarePluginFactory> sortedFactories = new ArrayList<>(
            beanFactory.getBeansOfType(AnnotationAwarePluginFactory.class).values()
        );
        AnnotationAwareOrderComparator.sort(sortedFactories);
        this.factories = sortedFactories;
    }
}
