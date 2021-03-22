package com.hai046.builder;

import cn.hutool.core.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author hai046
 * date   2020-3-22
 */
final class ViewBuilderRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private Environment environment;
    private ResourceLoader resourceLoader;
    private static Logger logger = LoggerFactory.getLogger(ViewBuilderRegistrar.class);

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;

    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return true;
            }

        };
    }


    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = this.getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableBuilder.class.getName());
        if (attrs == null) {
            return;
        }
        final String[] settingsBasePackages = (String[]) attrs.get("basePackages");
        Set<String> basePackages = new HashSet<>();

        if (settingsBasePackages != null) {
            basePackages.addAll(Arrays.asList(settingsBasePackages));
        }
        Set<String> typeBeanMap = new HashSet<>();
        scanner.addIncludeFilter(new AssignableTypeFilter(ViewBuilderFetchEntryByIds.class) {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                    throws IOException {
                String clazzName = metadataReader.getClassMetadata().getClassName();
                boolean first = !clazzName.startsWith(ViewBuilder.class.getName())
                        && !Objects.equals(clazzName, ViewBuilderFetchEntryByIds.class.getName())
                        && super.match(metadataReader, metadataReaderFactory);
                if (first) {
                    for (String superClazz : metadataReader.getClassMetadata().getInterfaceNames()) {
                        typeBeanMap.remove(superClazz);
                    }
                    typeBeanMap.remove(metadataReader.getClassMetadata().getSuperClassName());
                    if (!metadataReader.getClassMetadata().isInterface()) {
                        typeBeanMap.add(metadataReader.getClassMetadata().getClassName());
                    }
                    return true;
                }
                return false;
            }
        });

        Map<Class, Class> doMapper = new HashMap<>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (typeBeanMap.contains(candidateComponent.getBeanClassName())) {
                    logger.info("scan view builder={} ", candidateComponent.getBeanClassName());
                    try {
                        final Class<?> aClass = Class.forName(candidateComponent.getBeanClassName());
                        Method getEntries = getMethod(aClass, "getEntries", Collection.class);
                        if (getEntries == null) {
                            getEntries = aClass.getMethod("getEntries", Collection.class);
                        }
                        Type type = getEntries.getGenericReturnType();
                        final Type typeArgument = getTypeArgument(type, 1);
                        doMapper.put(Class.forName(typeArgument.getTypeName()), aClass);
                    } catch (ClassNotFoundException e) {
                        logger.warn("ClassNotFoundException candidateComponent :{} cost err", candidateComponent.getBeanClassName(), e);
                    } catch (NoSuchMethodException e) {
                        logger.warn("NoSuchMethodException candidateComponent :{} cost err", candidateComponent.getBeanClassName(), e);
                    }
                }
                candidateComponent.getBeanClassName();
            }
        }

        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(ViewBuilderBean.class);
        definition.setScope(BeanDefinition.SCOPE_SINGLETON);
        definition.addPropertyValue("doMapper", doMapper);
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, ViewBuilder.class.getName(), new String[]{"viewBuilder"});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
        logger.info("======doMapper:{}", doMapper);

    }

    static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
        if (null == clazz || StringUtils.hasLength(methodName)) {
            return null;
        }

        final Method[] methods = getMethodsDirectly(clazz, true);
        if (methods != null && methods.length > 0) {
            for (Method method : methods) {
                if (Objects.equals(methodName, method.getName())) {
                    return method;
                }
            }
        }
        return null;
    }

    static Method[] getMethodsDirectly(Class<?> beanClass, boolean withSuperClassMethods) throws SecurityException {
        Method[] allMethods = null;
        Class<?> searchType = beanClass;
        Method[] declaredMethods;
        while (searchType != null) {
            declaredMethods = searchType.getDeclaredMethods();
            if (null == allMethods) {
                allMethods = declaredMethods;
            } else {
                allMethods = ArrayUtil.append(allMethods, declaredMethods);
            }
            searchType = withSuperClassMethods ? searchType.getSuperclass() : null;
        }

        return allMethods;
    }

    static Type getTypeArgument(Type type, int index) {
        final Type[] typeArguments = getTypeArguments(type);
        if (null != typeArguments && typeArguments.length > index) {
            return typeArguments[index];
        }
        return null;
    }

    static Type[] getTypeArguments(Type type) {
        if (null == type) {
            return null;
        }
        final ParameterizedType parameterizedType = toParameterizedType(type);
        return (null == parameterizedType) ? null : parameterizedType.getActualTypeArguments();
    }

    static ParameterizedType toParameterizedType(Type type) {
        ParameterizedType result = null;
        if (type instanceof ParameterizedType) {
            result = (ParameterizedType) type;
        } else if (type instanceof Class) {
            final Class<?> clazz = (Class<?>) type;
            Type genericSuper = clazz.getGenericSuperclass();
            if (null == genericSuper || Object.class.equals(genericSuper)) {
                // 如果类没有父类，而是实现一些定义好的泛型接口，则取接口的Type
                final Type[] genericInterfaces = clazz.getGenericInterfaces();
                if (genericInterfaces != null && genericInterfaces.length != 0) {
                    // 默认取第一个实现接口的泛型Type
                    genericSuper = genericInterfaces[0];
                }
            }
            result = toParameterizedType(genericSuper);
        }
        return result;
    }
}


