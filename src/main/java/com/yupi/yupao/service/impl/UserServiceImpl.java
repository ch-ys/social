package com.yupi.yupao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.dto.UserDto;
import com.yupi.yupao.model.domain.entiy.User;
import com.yupi.yupao.model.domain.request.UserQueryRequest;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.mapper.UserMapper;
import com.yupi.yupao.untils.AlgUntils;
import com.yupi.yupao.model.domain.entiy.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.yupao.contant.UserConstant.ADMIN_ROLE;
import static com.yupi.yupao.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签查询用户
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUserByTagsList(List<String> tagNameList) {
        //数据库方案
//        QueryWrapper<User> QueryWrapper = new QueryWrapper<>();
//        for (String tagName : tagNameList) {
//            QueryWrapper = QueryWrapper.like("tags",tagName);
//        }
//        return userMapper.selectList(QueryWrapper);
        //内存方案
        QueryWrapper<User> QueryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(QueryWrapper);
        List<User> users = userList.stream().filter(user -> {
            String tagsJson = user.getTags();
            List<String> userTagsNameList = JSONUtil.toList(tagsJson, String.class);
            if (CollectionUtils.isEmpty(userTagsNameList)){
                return false;
            }
            for (String tagName : tagNameList) {
                if (!userTagsNameList.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
        return users;
    }

    @Override
    public Page<User> searchUserByQuery(UserQueryRequest userQueryRequest) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //构造条件
        List<String> searchTags = userQueryRequest.getSearchTags();
        if (searchTags != null){
            for (String tagName : searchTags) {
                userQueryWrapper.like("tags",tagName);
            }
        }
        String searchText = userQueryRequest.getSearchText();
        if (searchText != null){
            userQueryWrapper.like("username",searchText);
        }
        // 脱敏
        Long pageNum = userQueryRequest.getPageNum();
        Long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userMapper
                .selectPage(new Page<User>(pageNum, pageSize), userQueryWrapper);
        List<User> collect = userPage.getRecords()
                .stream().map(this::getSafetyUser)
                .collect(Collectors.toList());
        userPage.setRecords(collect);
        return userPage;
    }


    /**
     * 更新用户信息
     * @param user
     * @param request
     * @return
     */
    @Override
    public Integer updateUser(User user, HttpServletRequest request) {
        //信息有效性
        long id = user.getId();
        if (id < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User updateUser = userMapper.selectById(id);
        if (updateUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"修改用户不存在");
        }
        UserDto userDto = BeanUtil.toBean(user, UserDto.class);
        if (ObjectUtil.isEmpty(userDto)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //判断是否有权限(管理员或者自身用户)
        User loginUser = getCurrentUser(request);
        if (loginUser.getId() != user.getId() && !isAdmin(request) ){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //更新
        updateById(user);
        return userMapper.updateById(user);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 通过session获取当前用户
     * @param request
     * @return
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        return getById(userId);
    }

    /**
     * 推荐页缓存 数据量大 更新少
     *
     * @param httpServletRequest
     * @param num
     * @return
     */
    @Override
    public List<User> recommend(HttpServletRequest httpServletRequest, Integer num) {
        //获取缓存
        User currentUser = getCurrentUser(httpServletRequest);
        String key = "recommend:userid:" + currentUser.getId();
        String Json = stringRedisTemplate.opsForValue().get(key);
        //缓存不为空
        if (Json != null){
            List<User> users = JSONUtil.toList(Json, User.class);
            return users;
        }
        //缓存为空查询数据库
        List<User> users = match(currentUser,num).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.toList());
        String jsonStr = JSONUtil.toJsonStr(users);
        //写入缓存
        try {
            stringRedisTemplate.opsForValue().set(key,jsonStr,5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.info("recommend set cache error", e);
        }
        return users;
    }



    public List<User> match(User currentUser,Integer num){
        //定义空间 可限制空间节省内存 需要双重循环判定 费时间
        ArrayList<Pair<Long,Integer>> pairs = new ArrayList<>();
        //获取用户标签列表
        String currentUserTags = currentUser.getTags();
        List<String> currentUserStringList = JSONUtil.toList(currentUserTags, String.class);
        //查询用户
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id","tags");
        userQueryWrapper.isNotNull("tags");
        List<User> userList = list(userQueryWrapper);
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
        Map<Long, List<User>> unSortedUserIdMap = listByIds(ids).stream()
                .collect(Collectors.groupingBy(User::getId));
        //重新排序
        ArrayList<User> users = new ArrayList<>(num);
        for (Long id:ids) {
            users.add(unSortedUserIdMap.get(id).get(0));
        }
        return users;
    }

}

