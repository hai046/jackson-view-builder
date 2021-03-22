package com.hai046.builder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hai046
 * date 3/22/21
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewField {
    /**
     * 这个值对应的引用模块
     *
     * @return
     */
     Class<?> value();
}
