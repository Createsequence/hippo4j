package cn.hippo4j.core.plugin.annotation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;

/**
 * A bean holder.
 */
@RequiredArgsConstructor
public class BeanHolder {

    /**
     * bean factory
     */
    @NonNull
    private final BeanFactory beanFactory;

    /**
     * bean type
     */
    @Getter
    @NonNull
    private final Class<?> beanType;

    /**
     * bean name
     */
    @Getter
    @NonNull
    private final String beanName;

    /**
     * Get bean from {@link BeanFactory} by bean name.
     *
     * @return bean
     * @param <T> bean type
     * @throws ClassCastException Thrown when it cannot be converted to the specified type
     * @see BeanFactory#getBean(String)
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean() {
        return (T)beanFactory.getBean(beanName);
    }

}
