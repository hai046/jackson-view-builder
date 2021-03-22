package com.hai046.builder;

import com.hai046.builder.annotations.ViewField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hai046
 * date 2021-3-22
 */
final class ViewBuilderBean implements FactoryBean<ViewBuilder>, InitializingBean, ApplicationContextAware {
    private static Logger logger = LoggerFactory.getLogger(ViewBuilderBean.class);
    private Map<Class, Class> doMapper = new HashMap<>();
    private Map<Class, ViewBuilderFetchEntryByIds> beanMap = new HashMap<>();
    private ApplicationContext applicationContext;

    public ViewBuilderBean setDoMapper(Map<Class, Class> doMapper) {
        this.doMapper = doMapper;
        return this;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        doMapper.forEach((k, v) -> {
            final Object beanClazz = applicationContext.getBean(v);
            beanMap.put(k, (ViewBuilderFetchEntryByIds) beanClazz);
        });
        beanMap.forEach((k, v) -> {
            logger.info("view support DO:{},doService={}", k, v);
        });

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public ViewBuilder getObject() throws Exception {
        return new ViewBuilder() {
            @Override
            public Object serializer(ViewField annotation, Object value) {
                if (value == null) {
                    return null;
                }
                final ViewBuilderFetchEntryByIds viewBuilderFetchEntryByIds = beanMap.get(annotation.value());
                if (viewBuilderFetchEntryByIds == null) {
                    throw new RuntimeException("没有实现 " + annotation.value() + " view的获取逻辑");
                }
                if (value instanceof Collection) {
                    final Collection ids = (Collection) value;
                    final Map entries = viewBuilderFetchEntryByIds.getEntries(ids);
                    return ids.stream().map(entries::get).collect(Collectors.toList());
                } else {
                    return viewBuilderFetchEntryByIds.getEntries(Collections.singleton(value)).get(value);
                }
            }

            @Override
            public ViewBuilderFetchEntryByIds getViewBuilder(Class model) {
                return beanMap.get(model);
            }


        };
    }

    @Override
    public Class<?> getObjectType() {
        return ViewBuilder.class;
    }
}

