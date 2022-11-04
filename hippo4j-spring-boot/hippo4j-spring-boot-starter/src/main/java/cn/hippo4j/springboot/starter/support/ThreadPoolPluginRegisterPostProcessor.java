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
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>The extension implementation of {@link GlobalThreadPoolPluginManager} and {@link BeanPostProcessor},
 * used to register {@link ThreadPoolPlugin} for the bean initialization stage
 * of the {@link ThreadPoolPluginSupport}.
 *
 * <p>In the bean post-processing phase, {@link ThreadPoolPlugin}, {@link ThreadPoolPluginRegistrar},
 * and {@link ThreadPoolPluginSupport} will be parsed and cached. <br />
 * After the application context is refreshed and the {@link ContextRefreshedEvent} event occurs,
 * the plugin will be registered in all the registered {@link ThreadPoolPluginSupport},
 * and all plugin registrar are applied to the {@link ThreadPoolPluginSupport}.
 *
 * <p>In the non post-processing phase, the components actively registered
 * before the {@link ContextRefreshedEvent} event will be
 * delayed to take effect after the event occurs.<br />
 * Components that are actively registered after the {@link ContextRefreshedEvent} event will take effect immediately.
 *
 * <p><b>NOTE:</b>
 * If the {@link ThreadPoolPlugin}, {@link ThreadPoolPluginRegistrar}, and {@link ThreadPoolPluginSupport} is set to lazy load,
 * The processor will not perceive the bean unless the user actively triggers the initialization of the bean.
 *
 * @see ThreadPoolPluginSupport
 * @see ThreadPoolPluginRegistrar
 * @see ThreadPoolPlugin
 * @see DefaultGlobalThreadPoolPluginManager
 * @see ContextRefreshedEvent
 */
@Slf4j
public class ThreadPoolPluginRegisterPostProcessor extends DefaultGlobalThreadPoolPluginManager
    implements BeanNameAware, BeanPostProcessor, BeanFactoryAware, ApplicationListener<ContextRefreshedEvent> {

    /**
     * application context
     */
    private ConfigurableListableBeanFactory beanFactory;

    /**
     * application completed refresh
     */
    private boolean applicationCompletedRefresh = false;

    /**
     * <p>Cache the {@link ThreadPoolPlugin}, {@link ThreadPoolPluginRegistrar} and {@link ThreadPoolPluginSupport}
     * processed by this method, and the component will be registered after the {@link ContextRefreshedEvent} is published. <br />
     * If the {@link ContextRefreshedEvent} has been published,
     * the component will complete the incremental registration immediately.
     *
     * @param bean     the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one;
     * if {@code null}, no subsequent BeanPostProcessors will be invoked
     * @throws BeansException in case of errors
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanType = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
        if (Objects.isNull(beanType)) {
            log.warn("cannot resolve type for bean [{}]", beanName);
            return bean;
        }

        // register bean if necessary
        boolean isNewRegistrar = false;
        if (ThreadPoolPluginRegistrar.class.isAssignableFrom(beanType)) {
            ThreadPoolPluginRegistrar registrar = (ThreadPoolPluginRegistrar)bean;
            isNewRegistrar = getAppliedThreadPoolPluginRegistrar().add(registrar);
            if (isNewRegistrar && log.isDebugEnabled()) {
                log.info("register ThreadPoolPluginRegistrar [{}]", registrar.getId());
            }
        }
        boolean isNewPlugin = false;
        if (ThreadPoolPlugin.class.isAssignableFrom(beanType)) {
            ThreadPoolPlugin plugin = (ThreadPoolPlugin)bean;
            isNewPlugin = getRegisteredThreadPoolPlugins().add(plugin);
            if (isNewPlugin && log.isDebugEnabled()) {
                log.info("register ThreadPoolPlugin [{}]", plugin.getId());
            }
        }
        boolean isNewSupport = false;
        if (ThreadPoolPluginSupport.class.isAssignableFrom(beanType)) {
            ThreadPoolPluginSupport support = (ThreadPoolPluginSupport)bean;
            isNewSupport = registerThreadPoolPluginSupport(support);
            if (isNewSupport && log.isDebugEnabled()) {
                log.info("register ThreadPoolPluginSupport [{}]", support.getThreadPoolId());
            }
        }

        // eager completed register if application completed refresh
        boolean isNeedProcess = isNewSupport || isNewRegistrar || isNewPlugin;
        if (applicationCompletedRefresh && isNeedProcess) {
            Collection<ThreadPoolPluginSupport> targets = getRegisteredThreadPoolPluginSupports();
            if (isNewSupport) {
                targets = targets.stream().filter(t -> !Objects.equals(t, bean)).collect(Collectors.toList());
            }
            if (isNewRegistrar) {
                targets.forEach(((ThreadPoolPluginRegistrar)bean)::doRegister);
            }
            if (isNewPlugin) {
                ThreadPoolPlugin plugin = (ThreadPoolPlugin)bean;
                targets.forEach(s -> s.register(plugin));
            }
            if (isNewSupport) {
                ThreadPoolPluginSupport newSupport = (ThreadPoolPluginSupport)bean;
                doRegisterToSupport(newSupport);
            }
        }

        return bean;
    }

    /**
     * Whether enable register.
     *
     * @return true if enable register now, false otherwise
     */
    @Override
    protected boolean enableRegister() {
        return applicationCompletedRefresh;
    }

    /**
     * Set {@link #applicationCompletedRefresh} is {@link true},
     * and register all cache plugins to the manager and call all cached registrars.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.applicationCompletedRefresh = true;
        log.info("register plugins for all ThreadPoolPluginSupport");
        completedRegister();
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
