package org.ifaster.redis.configuration;

import org.ifaster.redis.client.support.RedisClientSupport;
import org.ifaster.redis.exception.RedisConfigException;
import org.ifaster.redis.listener.RedisEventListener;
import org.ifaster.redis.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author yangnan
 */
@Configuration
@ConditionalOnClass(RedisProperties.class)
@ConditionalOnExpression(value = "${redis.enable:false}")
public class RedisConfiguration implements BeanFactoryPostProcessor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String PREFIX = "redis";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        RedisConfig config = ConfigFactory.getConfig(listableBeanFactory, RedisConfig.class, PREFIX);
        if (config == null) {
            throw new RedisConfigException("not found redis config");
        }
        List<RedisProperties> redisProperties = config.getClusters();
        if (redisProperties == null || redisProperties.isEmpty()) {
            throw new RedisConfigException("not found RedisProperties config");
        }
        redisProperties.forEach(r -> {
            if (!r.isEnable()) {
                return;
            }
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisClientSupport.class);
            builder.addConstructorArgValue(r);
            Serializer serializer = null;
            RedisEventListener eventListener = null;
            if (!StringUtils.isEmpty(r.getSerializer())) {
                try {
                    Class cz = ClassUtils.forName(r.getSerializer(), beanFactory.getBeanClassLoader());
                    serializer = (Serializer) BeanUtils.instantiateClass(cz);
                } catch (ClassNotFoundException e) {
                    throw new RedisConfigException(e.getMessage(), e);
                }
            }

            if (!StringUtils.isEmpty(r.getEventListener())) {
                try {
                    Class cz = ClassUtils.forName(r.getEventListener(), beanFactory.getBeanClassLoader());
                    eventListener = (RedisEventListener) BeanUtils.instantiateClass(cz);
                } catch (ClassNotFoundException e) {
                    throw new RedisConfigException(e.getMessage(), e);
                }
            }
            builder.addConstructorArgValue(serializer);
            builder.addConstructorArgValue(eventListener);
            listableBeanFactory.registerBeanDefinition(r.getName(), builder.getBeanDefinition());
            logger.info("init success config:[{}]", r);
        });
    }
}
