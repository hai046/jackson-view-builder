package com.hai046.builder.services;

import com.hai046.builder.ViewBuilderFetchEntryByIds;
import com.hai046.builder.view.ImageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hai046
 * date 3/22/21
 */
@Service
public class ImageService implements ViewBuilderFetchEntryByIds<Long, ImageVO> {

    private static Logger logger = LoggerFactory.getLogger(ImageService.class);
    @Override
    public Map<Long, ImageVO> getEntries(Collection<Long> ids) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("ids:{}",ids);
        return ids.stream().map(id -> {
            final ImageVO imageVO = new ImageVO();
            imageVO.setId(id);
            imageVO.setUrl("http://image.com/" + id);
            return imageVO;
        }).collect(Collectors.toMap(ImageVO::getId, e -> e));
    }
}
