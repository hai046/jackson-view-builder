package com.hai046.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hai046.builder.view.UserVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author hai046
 * date 3/22/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TestBuilder {

    @Autowired
    private ViewBuilderObjectMapper objectMapper;

    private static Logger logger = LoggerFactory.getLogger(TestBuilder.class);


    @Test
    public void test1() throws JsonProcessingException {
        final List<UserVO> list = LongStream.rangeClosed(0, 10).boxed().map(id -> {
            UserVO userVO = new UserVO();
            userVO.setUserId(id);
            userVO.setSchoolId(new Random().nextInt());
            userVO.setName("name" + id);
            userVO.setImage(id * 100);
            return userVO;
        }).collect(Collectors.toList());

        final String json = objectMapper.writeValueAsString(list);
        logger.info("result={}", json);



    }

    @Test
    public void test2() throws IOException {
        final List<UserVO> list = LongStream.rangeClosed(0, 10).boxed().map(id -> {
            UserVO userVO = new UserVO();
            userVO.setUserId(id);
            userVO.setSchoolId(new Random().nextInt());
            userVO.setName("name" + id);
            userVO.setImage(id * 100);
            return userVO;
        }).collect(Collectors.toList());

        final String json = objectMapper.writeValueAsStringByMerge(list);
        logger.info("result={}", json);


    }
}
