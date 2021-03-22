package com.hai046.builder.services;

import com.hai046.builder.ViewBuilderFetchEntryByIds;
import com.hai046.builder.view.SchoolVO;
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
public class SchoolService  implements ViewBuilderFetchEntryByIds<Integer, SchoolVO> {
    private static Logger logger = LoggerFactory.getLogger(SchoolService.class);
    @Override
    public Map<Integer, SchoolVO> getEntries(Collection<Integer> ids) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("ids:{}",ids);
        return ids.stream().map(id -> {
            final SchoolVO schoolVO = new SchoolVO();
            schoolVO.setId(id);
            schoolVO.setName("清华大学" + id);
            return schoolVO;
        }).collect(Collectors.toMap(SchoolVO::getId, e -> e));
    }
}
