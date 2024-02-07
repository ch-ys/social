package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.model.domain.entiy.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.request.UserQueryRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签查询用户
     * @param tagNameList
     * @return
     */
    List<User> searchUserByTagsList(List<String> tagNameList);

    Integer updateUser(User user, HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    User getCurrentUser(HttpServletRequest request);

    List<User> recommend(HttpServletRequest request, Integer num);

    Page<User> searchUserByQuery(UserQueryRequest userQueryRequest);
}
