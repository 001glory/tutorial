# Spring Boot Caffeine Caching Example Configuration

## Configure Caffeine Cache

We configure `caffeine` by using the `application.yml` file. We can create cache directories by appending a comma-separated list to the `spring.cache.cache-names` configuration property. We can define custom specifications using the `spring.cache.caffeine.spec` configuration property.

[CaffeineSpec](https://static.javadoc.io/com.github.ben-manes.caffeine/caffeine/2.2.2/com/github/benmanes/caffeine/cache/CaffeineSpec.html)supports the following configuration properties:

- `initialCapacity=[integer]`: Sets the minimum total size for the internal hash tables. Provide a large enough estimate at construction time avoids the need for expensive resizing operation later, but setting this value unnecessarily high wastes memory.

- `maximumSize=[long]`: Specifies the maximum number of entries the cache may contain. Note that the cache may evict an entry before this limit is exceeded or temporarily exceed the threshold while evicting. *This feature cannot be used in conjunction with maximumWeight*.

- `maximumWeight=[long]`: Specifies the maximum weight of entries the cache may contain. Note that the cache may evict an entry before this limit is exceeded or temporarily exceed the threshold while evicting. *This feature cannot be used in conjunction with maximumSize*.

- `expireAfterAccess=[duration]`: Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after the entry’s creation.

- `expireAfterWrite=[duration]`: Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after the entry’s creation, or the most recent replacement of its value.

- `refreshAfterWrite=[duration]`: Specifies that active entries are eligible for automatic refresh once a fixed duration has elapsed after the entry’s creation, or the most recent replacement of its value.

  Durations are represented by an integer, followed by one of “d”, “h”, “m”, or “s”, representing days, hours, minutes, or seconds respectively. There is currently no syntax to request expiration in milliseconds, microseconds, or nanoseconds.

  ```yml
  spring:
    cache:
      caffeine:
        spec: initialCapacity=50,maximumSize=500,expireAfterWrite=10s
  #must set this type
      type: caffeine
  ```

    If you use the` refreshAfterWrite `configuration, you must also specify a `CacheLoader`, such as:

  ```java
  @Bean
  public CacheLoader<Object, Object> cacheLoader() {
  
      CacheLoader<Object, Object> cacheLoader = new CacheLoader<Object, Object>() {
  
          @Override
          public Object load(Object key) throws Exception {
              return null;
          }
  
          // Rewrite this method to return the oldValue value back, and then refresh the cache
          @Override
          public Object reload(Object key, Object oldValue) throws Exception {
              return oldValue;
          }
      };
  
      return cacheLoader;
  }
  ```

  

  ## Testing Caffeine Cache

  To demonstrate if our methods are using the cache, we wrote a simple application. The `find` 、`save`、`remove`method is executed multiple times.

  ```java
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
  
  ```