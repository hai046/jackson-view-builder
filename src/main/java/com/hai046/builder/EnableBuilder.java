package com.hai046.builder;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author hai046
 * date 2020-3-22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({ViewBuilderRegistrar.class})
public @interface EnableBuilder {
    String[] basePackages() default "com";
}
