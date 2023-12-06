package com.yupi.yupao.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String port;

    private String host;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        String addresses = "redis://" + host + ":" + port;
        config.useSingleServer()
                .setAddress(addresses);

        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
