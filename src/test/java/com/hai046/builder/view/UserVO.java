package com.hai046.builder.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hai046.builder.annotations.JsonUUID;
import com.hai046.builder.annotations.ViewField;
import lombok.Data;

/**
 * @author hai046
 * date 3/22/21
 */
@Data
public class UserVO {

    /**
     * 把数字id加密转成对称的string Id，避免客户端传递穷举
     * <p>
     * 返回给客户端的是string类型，
     * 客户端传递string类型给服务端会反解成数字类型，对客户端和服务端都是透明
     */
    @JsonUUID
    private Long userId;

    private String name;

    /**
     * 通过image来换取实体
     */
    @ViewField(ImageVO.class)
    private Long image;

    @JsonProperty("school")
    @ViewField(SchoolVO.class)
    private Integer schoolId;
}
