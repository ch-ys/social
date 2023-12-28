package com.yupi.yupao.job;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.model.domain.entiy.User;
import com.yupi.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RecommendScheduler {
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 50 * * * ? ")
    private void setRecommend(){
        String key = "recommend:userid:" + 6;
        String lockName = "RecommendScheduler:lock";
        RLock lock = redissonClient.getLock(lockName);
        try {
            if (lock.tryLock(0,10L,TimeUnit.SECONDS)){
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User> userPage = userService.page(new Page<User>(1,5), queryWrapper);
                String jsonStr = JSONUtil.toJsonStr(userPage);
                try {
                    stringRedisTemplate.opsForValue().set(key,jsonStr,5L, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.info("recommend set cache error", e);
                }
            }
        } catch (InterruptedException e) {
            log.info("scheduler error",e);
        }finally {
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
