package com.yupi.yupao.datasource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupao.model.domain.entiy.User;
import com.yupi.yupao.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDataSource implements DataSource {

    @Resource
    private UserService userService;

    public List<Object> doSearch(String searchText) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //构造条件
        userQueryWrapper.like("username", searchText);
        // 脱敏
        List<Object> collect = userService.list(userQueryWrapper).stream().map(user -> {
            return (Object) userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return collect;
    }

    ;
}
