package com.hai046.builder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author h
 * date 2021-3-13
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonUUID {

//    /**
//     * AES key,如果比定义使用默认的，否则使用自定义的
//     *
//     * @return
//     */
//    byte[] key() default {};


}
