package com.github.dqqzj.caffeine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qinzhongjian
 * @date created in 2019-03-31 11:59
 * @description: TODD
 * @since 1.0.0
 */
@Slf4j
@RestController
public class CacheTestController {
    @Autowired
    RedisTemplate redisTemplate;

    @PostMapping("save")
    @CachePut(value = "people",key = "#people.id")
    public String save(People people) {
        Map<String,String> map = new HashMap<>(3);
        map.put("id",people.getId());
        map.put("username",people.getUsername());
        map.put("password",people.getPassword());
        log.info("start save id:{} username:{} password:{}",people.getId(),people.getUsername(),people.getPassword());
        this.redisTemplate.opsForHash().putAll(people.getId(),map);
        return map.toString();
    }
    @GetMapping("find")
    @Cacheable(value = "people",key = "#id")
    public Map find(String id) {
        log.info("start find id:{}",id);
        Map map = this.redisTemplate.opsForHash().entries(id);
        map.forEach((k,v) -> System.out.println("key:value = " + k + ":" + v));
        return map;
    }
    @DeleteMapping("remove")
    @CacheEvict(value = "people",key = "#id")
    public void remove(String id) {
        log.info("remove find id:{}",id);
    }
}
