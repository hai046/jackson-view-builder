package com.hai046.builder;

import com.hai046.builder.annotations.ViewField;

/**
 * @author hai046
 * date 3/22/21
 */
public interface ViewBuilder {
    /**
     * 通过注解序列化数据
     *
     * @param annotation
     * @param value
     * @return
     */
    Object serializer(ViewField annotation, Object value);

    ViewBuilderFetchEntryByIds getViewBuilder(Class model);
}
