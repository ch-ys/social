package com.yupi.yupao.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupao.mapper.UserTeamMapper;
import com.yupi.yupao.model.domain.entiy.User;
import com.yupi.yupao.untils.AlgUntils;
import javafx.util.Pair;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Resource
    private UserTeamMapper userTeamMapper;

    /**
     * 测试添加用户
     */
    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("dogYupi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }


    /**
     * 测试更新用户
     */
    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("dogYupi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.updateById(user);
        Assertions.assertTrue(result);
    }

    /**
     * 测试删除用户
     */
    @Test
    public void testDeleteUser() {
        boolean result = userService.removeById(1L);
        Assertions.assertTrue(result);
    }


    /**
     * 测试获取用户
     */
    @Test
    public void testGetUser() {
        User user = userService.getById(1L);
        Assertions.assertNotNull(user);
    }

    /**
     * 测试用户注册
     */
    @Test
    void userRegister() {
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "yu";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "yupi";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "yu pi";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "dogYupi";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "yupi";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
    }

    @Test
    void searchUserByTagsList() {
        List<String> tagsList = Arrays.asList("java");
        List<User> users = userService.searchUserByTagsList(tagsList);

    }

    @Test
    public void testRecommend(){
        //相关参数
        Integer num = 3;
        User currentUser= userService.getById(6);
        //定义空间 可限制空间节省内存 需要双重循环判定 费时间
        ArrayList<Pair<Long,Integer>> pairs = new ArrayList<>();
        //获取用户标签列表
        String currentUserTags = currentUser.getTags();
        List<String> currentUserStringList = JSONUtil.toList(currentUserTags , String.class);
        //查询用户
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id","tags");
        userQueryWrapper.isNotNull("tags");
        List<User> userList = userService.list(userQueryWrapper);
        //放入pairs中
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //排除自己
            if (user.getId() == currentUser.getId()){
                continue;
            }
            List<String> userStringList = JSONUtil.toList(userTags, String.class);
            //计算相似度
            int distance = AlgUntils.minDistance(currentUserStringList, userStringList);
            pairs.add(new Pair<>(user.getId(),distance));
        }
        //排序挑选num
        List<Pair<Long,Integer>> collect = pairs.stream()
                .sorted((a,b) -> a.getValue()- b.getValue())
                .limit(num)
                .collect(Collectors.toList());
        //补充信息
        List<Long> ids = collect.stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());
        //map 有根据id的key 方便取出对应对象
        Map<Long, List<User>> unSortedUserIdMap = userService.listByIds(ids).stream()
                .collect(Collectors.groupingBy(User::getId));
        //重新排序
        ArrayList<User> users = new ArrayList<>(num);
        for (Long id:ids) {
            users.add(unSortedUserIdMap.get(id).get(0));
        }


    }

    @Test
    void findTeamUsers(){
        List<User> teamUsers = userTeamMapper.findTeamUsers(1L);
    }
}