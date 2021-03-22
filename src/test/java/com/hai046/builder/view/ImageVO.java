package com.hai046.builder.view;

import com.hai046.builder.annotations.JsonUUID;
import lombok.Data;

/**
 * @author hai046
 * date 3/22/21
 */
@Data
public class ImageVO {

    @JsonUUID
    private Long id;

    private String url;

    private Integer width, height;

}
