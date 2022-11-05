package cn.hippo4j.springboot.starter.support;

import cn.hippo4j.common.toolkit.Assert;
import cn.hippo4j.common.toolkit.CollectionUtil;
import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import cn.hippo4j.core.plugin.annotation.ThreadPoolPluginAdapt;
import cn.hippo4j.core.plugin.manager.GlobalThreadPoolPluginManager;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>An implementation of {@link BeanPostProcessor}.
 * Scan the methods annotated by {@link ThreadPoolPluginAdapt}
 * in the bean after bean initialization, and adapted to the corresponding {@link ThreadPoolPlugin},
 * finally the plugin will publish by {@link GlobalThreadPoolPluginManager}.
 *
 * <p>Before use, ensure that {@link AnnotationAwarePluginFactory} is available in the application context.
 * The processor will try to call {@link AnnotationAwarePluginFactory#support}
 * of all factories by specific order,
 * and finally use the highest priority factory to adapt annotated method to {@link ThreadPoolPlugin}.
 *
 * <p><b>NOTE:</b>
 * The processor will force initialization of all {@link AnnotationAwarePluginFactory}
 * in the {@link org.springframework.context.ApplicationContext},
 * Please ensure that {@link AnnotationAwarePluginFactory} is not lazy loading,
 * or does not rely on beans that should not be initialized in advance in {@link AnnotationAwarePluginFactory}.
 *
 * @see ThreadPoolPluginAdapt
 * @see AnnotationAwarePluginFactory
 * @see GlobalThreadPoolPluginManager
 */
@Slf4j
public class AnnotationAwarePluginPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    /**
     * global thread pool plugin publisher
     */
    private GlobalThreadPoolPluginManager globalThreadPoolPluginManager;

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
     * Check whether there is any annotation directly or indirectly marked by {@link ThreadPoolPluginAdapt} in the bean,
     * and adapt it to {@link cn.hippo4j.core.plugin.ThreadPoolPlugin}.
     *
     * @param bean     the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one;
     * if {@code null}, no subsequent BeanPostProcessors will be invoked
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
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
        annotatedMethods.forEach((method, annotation) -> getPluginFactories().stream()
            .filter(f -> f.support(beanName, beanType, method, annotation))
            .findFirst()
            .map(f -> f.createThreadPoolPlugin(beanName, beanType, method, annotation))
            .ifPresent(globalThreadPoolPluginManager::enableThreadPoolPlugin)
        );

        return bean;
    }

    /**
     * Set the ApplicationContext,.
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
        this.globalThreadPoolPluginManager = beanFactory.getBean(GlobalThreadPoolPluginManager.class);
    }

    private Map<Method, ThreadPoolPluginAdapt> resolvedAnnotatedMethods(Class<?> beanType) {
        return MethodIntrospector.selectMethods(
            beanType, (MethodIntrospector.MetadataLookup<ThreadPoolPluginAdapt>) method ->
                AnnotatedElementUtils.findMergedAnnotation(method, ThreadPoolPluginAdapt.class)
        );
    }

    private Collection<AnnotationAwarePluginFactory> getPluginFactories() {
        if (Objects.isNull(this.factories)) {
            List<AnnotationAwarePluginFactory> sortedFactories = new ArrayList<>(
                beanFactory.getBeansOfType(AnnotationAwarePluginFactory.class).values()
            );
            AnnotationAwareOrderComparator.sort(sortedFactories);
            this.factories = sortedFactories;
        }
        return this.factories;
    }
}
