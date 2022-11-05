package cn.hippo4j.springboot.starter.support;

import cn.hippo4j.common.toolkit.Assert;
import cn.hippo4j.core.plugin.ThreadPoolPlugin;
import cn.hippo4j.core.plugin.manager.DefaultGlobalThreadPoolPluginManager;
import cn.hippo4j.core.plugin.manager.GlobalThreadPoolPluginManager;
import cn.hippo4j.core.plugin.manager.ThreadPoolPluginRegistrar;
import cn.hippo4j.core.plugin.manager.ThreadPoolPluginSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Objects;

/**
 * <p>The extension implementation of {@link GlobalThreadPoolPluginManager} and {@link BeanPostProcessor},
 * used to register {@link ThreadPoolPlugin} for the bean initialization stage of the {@link ThreadPoolPluginSupport}.
 *
 * <p><b>NOTE:</b>
 * If the {@link ThreadPoolPlugin}, {@link ThreadPoolPluginRegistrar}, and {@link ThreadPoolPluginSupport} is set to lazy load,
 * The processor will not perceive the bean unless the user actively triggers the initialization of the bean.
 *
 * @see ThreadPoolPluginSupport
 * @see ThreadPoolPluginRegistrar
 * @see ThreadPoolPlugin
 * @see GlobalThreadPoolPluginManager
 * @see DefaultGlobalThreadPoolPluginManager
 */
@Slf4j
public class ThreadPoolPluginRegisterPostProcessor extends DefaultGlobalThreadPoolPluginManager
    implements BeanNameAware, BeanPostProcessor, BeanFactoryAware {

    /**
     * application context
     */
    private ConfigurableListableBeanFactory beanFactory;

    /**
     * <p>Post process bean, if bean is instance of {@link ThreadPoolPlugin},
     * {@link ThreadPoolPluginRegistrar} or {@link ThreadPoolPluginSupport},
     * then take beans as an available component and register to {@link GlobalThreadPoolPluginManager}.
     *
     * @param bean     the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one;
     * if {@code null}, no subsequent BeanPostProcessors will be invoked
     * @throws BeansException in case of errors
     * @see GlobalThreadPoolPluginManager#enableThreadPoolPlugin 
     * @see GlobalThreadPoolPluginManager#enableThreadPoolPluginRegistrar 
     * @see GlobalThreadPoolPluginManager#doRegisterAndManage
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanType = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
        if (Objects.isNull(beanType)) {
            log.warn("cannot resolve type for bean [{}]", beanName);
            return bean;
        }

        // register bean if necessary
        if (ThreadPoolPluginRegistrar.class.isAssignableFrom(beanType)) {
            ThreadPoolPluginRegistrar registrar = (ThreadPoolPluginRegistrar)bean;
            if (enableThreadPoolPluginRegistrar(registrar) && log.isDebugEnabled()) {
                log.info("register ThreadPoolPluginRegistrar [{}]", registrar.getId());
            }
        }
        if (ThreadPoolPlugin.class.isAssignableFrom(beanType)) {
            ThreadPoolPlugin plugin = (ThreadPoolPlugin)bean;
            if (enableThreadPoolPlugin(plugin) && log.isDebugEnabled()) {
                log.info("register ThreadPoolPlugin [{}]", plugin.getId());
            }
        }
        if (ThreadPoolPluginSupport.class.isAssignableFrom(beanType)) {
            ThreadPoolPluginSupport support = (ThreadPoolPluginSupport)bean;
            if (doRegisterAndManage(support) && log.isDebugEnabled()) {
                log.info("register ThreadPoolPluginSupport [{}]", support.getThreadPoolId());
            }
        }
        return bean;
    }

    /**
     * Set the ApplicationContext.
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
    }

    /**
     * Use id as alias of bean name.
     *
     * @param name the name of the bean in the factory.
     *             Note that this name is the actual bean name used in the factory, which may
     *             differ from the originally specified name: in particular for inner bean
     *             names, the actual bean name might have been made unique through appending
     *             "#..." suffixes. Use the {@link BeanFactoryUtils#originalBeanName(String)}
     *             method to extract the original bean name (without suffix), if desired.
     */
    @Override
    public void setBeanName(String name) {
        this.beanFactory.registerAlias(name, getId());
    }

}
