package com.yupi.yupao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.dto.TeamEsDTO;
import com.yupi.yupao.model.domain.dto.TeamUpdateDTO;
import com.yupi.yupao.model.domain.vo.TeamUserVo;
import com.yupi.yupao.model.domain.entiy.Team;
import com.yupi.yupao.model.domain.entiy.User;
import com.yupi.yupao.model.domain.entiy.UserTeam;
import com.yupi.yupao.model.domain.enums.TeamStatusEnum;
import com.yupi.yupao.model.domain.request.*;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.mapper.TeamMapper;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.service.UserTeamService;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Transactional(rollbackFor=Exception.class)
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    private static final Long OneTeam = 1L;

    @Override
    @Transactional
    public Long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request) {
        //检验登录
        User currentUser = userService.getCurrentUser(request);
        Long userId = currentUser.getId();
        //检验队伍人数
        Integer maxNum = teamAddRequest.getMaxNum();
        if (maxNum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数错误");
        }
        if (maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数错误");
        }
        //队伍名称
        String name = teamAddRequest.getName();
        if (name == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名称不能为空");
        }
        if (name.length() >= 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名称过长");
        }
        //队伍描述
        String description = teamAddRequest.getDescription();
        if (description.length() >= 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"描述过长");
        }
        //队伍状态
        Integer status = Optional.ofNullable(teamAddRequest.getStatus()).orElse(TeamStatusEnum.PUBLIC.getValue());
        if (status == TeamStatusEnum.SECRET.getValue()){
            //密码
            String password = teamAddRequest.getPassword();
            if (StringUtils.isEmpty(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍加密状态下参数不能为空");
            }
        }
        //超时时间
        Date expireTime = teamAddRequest.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间错误");
        }
        //队伍数量
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UserId",userId);
        long count = count(queryWrapper);
        if (count >= 5L){
            throw new BusinessException(ErrorCode.NO_AUTH,"队伍数量过多");
        }
        //创建队伍
        Team team = BeanUtil.toBean(teamAddRequest, Team.class);
        team.setId(null);
        team.setUserId(userId);
        boolean save = save(team);
        Long teamId = team.getId();
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        //创建关系数据
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        boolean save1 = userTeamService.save(userTeam);
        if (!save1){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        return teamId;
    }

    @Override
    public Page<TeamUserVo> searchTeamUserPage(TeamQueryRequest teamQuery,HttpServletRequest httpServletRequest) {
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        //id查询条件
        Long id = teamQuery.getId();
        if (id != null && id > 0){
            teamQueryWrapper.eq("id",id);
        }
        //名称查询条件
        String name = teamQuery.getName();
        if (StringUtils.isNotBlank(name)){
            teamQueryWrapper.like("name",name);
        }
        //查询名称或者描述
        String searchText = teamQuery.getSearchText();
        if (StringUtils.isNotBlank(searchText)){
            List<Long> longs = searchFromEs(searchText);
            teamQueryWrapper.and(qw -> {
                qw.like("name",searchText);
                if (longs != null){
                qw.or().in("id",longs);
                }
            });
        }
        //查询人数多余
        Integer maxNum = teamQuery.getMaxNum();
        if (maxNum != null && maxNum > 0){
            teamQueryWrapper.eq("maxNum", maxNum);
        }
        //时间大于当前
        teamQueryWrapper.and(qw ->qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        //根据状态查询队伍(只有管理员可以查看非公共队伍）
        Integer status = teamQuery.getStatus();
        if (status == null){
            status = TeamStatusEnum.PUBLIC.getValue();
        }
        if (!userService.isAdmin(httpServletRequest) && !status.equals(TeamStatusEnum.PUBLIC.getValue())){
            throw new BusinessException(ErrorCode.NO_AUTH,"无法查询非公共队伍");
        }
        teamQueryWrapper.eq("status",status);
        //查询team
        List<Team> list = list(teamQueryWrapper);
        List<TeamUserVo> teamUserVoList = null;
        if (list != null){
            teamUserVoList = list.stream().map(team -> {
                //获取队伍id
                Long teamId = team.getId();
                //获取在该队伍中队员的id列表 mybatis方案
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                List<Long> userTeamListIds = userTeamList.stream()
                        .map(UserTeam::getUserId)
                        .collect(Collectors.toList());
                List<User> userList = userService.listByIds(userTeamListIds);
                //获取在该队伍中队员的id列表 自定义sql
//                List<User> userList = userTeamService.findTeamUsers(teamId);
                //组装
                TeamUserVo teamUserVo = new TeamUserVo();
                BeanUtils.copyProperties(team, teamUserVo);
                teamUserVo.setUserList(userList);
                return teamUserVo;
            }).collect(Collectors.toList());
        }
        Long pageNum = teamQuery.getPageNum();
        Long pageSize = teamQuery.getPageSize();
        Page<TeamUserVo> teamUserVoPage = new Page<>(pageNum, pageSize);
        teamUserVoPage.setRecords(teamUserVoList);
        return teamUserVoPage;
    }

    @Override
    public List<Team> listTeam(TeamQueryRequest teamQuery, HttpServletRequest httpServletRequest) {
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        //id查询条件
        Long id = teamQuery.getId();
        if (id != null && id > 0){
            teamQueryWrapper.eq("id",id);
        }
        //名称查询条件
        String name = teamQuery.getName();
        if (StringUtils.isNotBlank(name)){
            teamQueryWrapper.like("name",name);
        }
        //查询名称或者描述
        String searchText = teamQuery.getSearchText();
        if (StringUtils.isNotBlank(searchText)){
            List<Long> longs = searchFromEs(searchText);
            teamQueryWrapper.and(qw -> {
                qw.like("name",searchText);
                if (longs != null){
                    qw.or().in("id",longs);
                }
            });
        }
        //查询人数多余
        Integer maxNum = teamQuery.getMaxNum();
        if (maxNum != null && maxNum > 0){
            teamQueryWrapper.eq("maxNum", maxNum);
        }
        //时间大于当前
        Date expireTime = teamQuery.getExpireTime();
        teamQueryWrapper.and(qw ->qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        //根据状态查询队伍(只有管理员可以查看非公共队伍）
        Integer status = teamQuery.getStatus();
        if (status == null){
            status = TeamStatusEnum.PUBLIC.getValue();
        }
        if (!userService.isAdmin(httpServletRequest) && !status.equals(TeamStatusEnum.PUBLIC.getValue())){
            throw new BusinessException(ErrorCode.NO_AUTH,"无法查询非公共队伍");
        }
        teamQueryWrapper.eq("status",status);
        //查询team
        List<Team> list = list(teamQueryWrapper);
        return list;
    }

    @Override
    public List<TeamUserVo> searchTeamUser(TeamQueryRequest teamQuery, HttpServletRequest httpServletRequest) {
        if (!userService.isAdmin(httpServletRequest)){
            throw new BusinessException(ErrorCode.NO_AUTH,"该接口需要管理员权限");
        }
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        //id查询条件
        Long id = teamQuery.getId();
        if (id != null && id > 0){
            teamQueryWrapper.eq("id",id);
        }
        //名称查询条件
        String name = teamQuery.getName();
        if (StringUtils.isNotBlank(name)){
            teamQueryWrapper.like("name",name);
        }
        //查询名称或者描述
        String searchText = teamQuery.getSearchText();
        if (StringUtils.isNotBlank(searchText)){
            List<Long> longs = searchFromEs(searchText);
            teamQueryWrapper.and(qw -> {
                qw.like("name",searchText);
                if (longs != null){
                    qw.or().in("id",longs);
                }
            });
        }
        //查询人数多余
        Integer maxNum = teamQuery.getMaxNum();
        if (maxNum != null && maxNum > 0){
            teamQueryWrapper.eq("maxNum", maxNum);
        }
        //时间大于当前
        Date expireTime = teamQuery.getExpireTime();
        teamQueryWrapper.and(qw ->qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        //根据状态查询队伍(只有管理员可以查看非公共队伍）
        Integer status = teamQuery.getStatus();
        if (status == null){
            status = TeamStatusEnum.PUBLIC.getValue();
        }
        if (!userService.isAdmin(httpServletRequest) && !status.equals(TeamStatusEnum.PUBLIC.getValue())){
            throw new BusinessException(ErrorCode.NO_AUTH,"无法查询非公共队伍");
        }
        teamQueryWrapper.eq("status",status);
        //查询team
        List<Team> list = list(teamQueryWrapper);
        List<TeamUserVo> teamUserVoList = null;
        if (list != null){
            teamUserVoList = list.stream().map(team -> {
                //获取队伍id
                Long teamId = team.getId();
                //获取在该队伍中队员的id列表 mybatis方案
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                List<Long> userTeamListIds = userTeamList.stream()
                        .map(UserTeam::getUserId)
                        .collect(Collectors.toList());
                List<User> userList = userService.listByIds(userTeamListIds);
                //获取在该队伍中队员的id列表 自定义sql
//                List<User> userList = userTeamService.findTeamUsers(teamId);
                //组装
                TeamUserVo teamUserVo = new TeamUserVo();
                BeanUtils.copyProperties(team, teamUserVo);
                teamUserVo.setUserList(userList);
                return teamUserVo;
            }).collect(Collectors.toList());
        }
        return teamUserVoList;
    }

    @Override
    public List<Long> searchFromEs(String text) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 按关键词检索
        boolQueryBuilder.must(QueryBuilders.matchQuery("description", text));
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .build();
        SearchHits<TeamEsDTO> search = elasticsearchRestTemplate.search(searchQuery, TeamEsDTO.class);
        if (!search.hasSearchHits()){
            return null;
        }
        List<Long> collect = search.getSearchHits().stream()
                .map(searchHit -> searchHit.getContent().getId())
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest httpServletRequest) {
        Long id = teamUpdateRequest.getId();
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新队伍id错误");
        }
        Team team = getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不存在该队伍");
        }
        //判断是否为管理员或者队长
        boolean admin = userService.isAdmin(httpServletRequest);
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if (!admin && currentUser.getId() != team.getUserId()){
            throw new BusinessException(ErrorCode.NO_AUTH,"队伍信息只允许管理员或队长");
        }
        //内容相同则之制空
        String name = teamUpdateRequest.getName();
        if (name != null && name.equals(team.getName())) {
            teamUpdateRequest.setName(null);
        }
        String description = teamUpdateRequest.getDescription();
        if (description != null && description.equals(team.getDescription())) {
            teamUpdateRequest.setName(null);
        }
        Integer maxNum = teamUpdateRequest.getMaxNum();
        if (maxNum != null && maxNum.equals(team.getMaxNum())) {
            teamUpdateRequest.setName(null);
        }
        Date expireTime = teamUpdateRequest.getExpireTime();
        if (expireTime != null && expireTime.equals(team.getExpireTime())) {
            teamUpdateRequest.setExpireTime(null);
        }
        Long userId = team.getId();
        if (userId != null && userId == team.getUserId()){
            teamUpdateRequest.setUserId(null);
        }
        Integer status = teamUpdateRequest.getStatus();
        if (status != null && status == team.getStatus()){
            teamUpdateRequest.setStatus(null);
        }
        //判断队伍状态是否需要密码
        if (status != null && status == TeamStatusEnum.SECRET.getValue()){
            String password = teamUpdateRequest.getPassword();
            if (StringUtils.isBlank(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"修改私密队伍必须输入密码");
            }
        }
        //验证数据是否都为空
        TeamUpdateDTO teamUpdateDto = BeanUtil.toBean(teamUpdateRequest, TeamUpdateDTO.class);
        if (teamUpdateDto == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"必须存在更新的数据");
        }
        Team updateTeam = BeanUtil.toBean(teamUpdateRequest, Team.class);
        return updateById(updateTeam);
    }

    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest) {
        //参数验证
        Long userId = teamJoinRequest.getUserId();
        if (userId == null || userId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户id非法");
        }
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍id非法");
        }
        //查询是否加入超过5个队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能同时加入超过5个队伍");
        }
        //队伍状况
        Team team = getById(teamId);
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        //时间不能过期
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍过期");
        }
        //不能满队
        Integer maxNum = team.getMaxNum();
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        long count1 = userTeamService.count(userTeamQueryWrapper);
        if (maxNum == count1){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍人员已满");
        }
        //队伍状态验证
        Integer status = team.getStatus();
        if (TeamStatusEnum.PRIVATE.getValue() == status){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"私有队伍无法加入");
        }
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.getValue() == status){
            if (!password.equals(teamJoinRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密队伍密码错误");
            }
        }
        //多并发加锁 防止重复加入队伍
        RLock lock = redissonClient.getLock("joinTeam:" + userId );
        boolean save = false;
        try {
            if (lock.tryLock(0, 10, TimeUnit.SECONDS)) {
                //不能重复加入队伍
                userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.eq("userId", userId);
                long count2 = userTeamService.count(userTeamQueryWrapper);
                if (count2 > 0) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不能重复加入队伍");
                }
                //关联属性
                UserTeam userTeam = new UserTeam();
                userTeam.setTeamId(teamId);
                userTeam.setUserId(userId);
                userTeam.setJoinTime(new Date());
                save = userTeamService.save(userTeam);
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入队伍失败，请稍后重试");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入队伍失败，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean quitTeam(TeamLeaveRequest teamLeaveRequest) {
        //参数验证
        Long userId = teamLeaveRequest.getUserId();
        if (userId == null || userId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户id非法");
        }
        Long teamId = teamLeaveRequest.getTeamId();
        if (teamId == null || teamId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍id非法");
        }
        //查询是否在队伍中
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        userTeamQueryWrapper.eq("userId",userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count == 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不在该队伍中");
        }
        //查询中间表队伍人数情况
        QueryWrapper<UserTeam> TeamQueryWrapper = new QueryWrapper<>();
        TeamQueryWrapper.eq("teamId",teamId);
        TeamQueryWrapper.orderByAsc("id");
        TeamQueryWrapper.last("limit 2");
        List<UserTeam> list = userTeamService.list(TeamQueryWrapper);
        //在队伍中 只剩一个
        if (OneTeam == list.size()){
            boolean remove = userTeamService.remove(userTeamQueryWrapper);
            boolean b = removeById(teamId);
            return b && remove;
        }
        //队伍中多人
        //判断是否卫队长
        Team team = getById(teamId);
        Long teamCap = team.getUserId();
        Boolean isCap = teamCap == userId;
        //非队长
        if (!isCap){
            return userTeamService.remove(userTeamQueryWrapper);
        }
        //队长
        //获取最先加入的用户
        UserTeam userTeam = list.get(1);
        Long nextUserId = userTeam.getUserId();
        Long teamId1 = userTeam.getTeamId();
        //队伍信息修改
        Team team1 = new Team();
        team1.setUserId(nextUserId);
        team1.setId(teamId1);
        boolean b = updateById(team1);
        //队伍用户关联表修改
        boolean remove = userTeamService.remove(userTeamQueryWrapper);
        return b && remove;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTeam(Integer id, HttpServletRequest httpServletRequest) {
        //检验队伍是否存在
        Team team = getById(id);
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        //是否为队长
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if (team.getUserId() != currentUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH,"非该队长无权限解散队伍");
        }
        // 关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",id);
        boolean updateUserTeam = userTeamService.update(userTeamQueryWrapper);
        boolean b = removeById(id);
        return b && updateUserTeam;
    }

    @Override
    public List<TeamUserVo> listMyCreate(HttpServletRequest httpServletRequest) {
        User currentUser = userService.getCurrentUser(httpServletRequest);
        Long userId = currentUser.getId();
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId",userId);
        List<TeamUserVo> collect = list(teamQueryWrapper).stream().map(team -> {
            TeamUserVo teamUserVo = BeanUtil.toBean(team, TeamUserVo.class);
            return teamUserVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<TeamUserVo> listMyJoin(HttpServletRequest httpServletRequest) {
        User currentUser = userService.getCurrentUser(httpServletRequest);
        Long userId = currentUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        List<Long> teamIds = userTeamService.list(userTeamQueryWrapper).stream()
                .map(userTeam -> userTeam.getTeamId())
                .collect(Collectors.toList());
        List<TeamUserVo> collect = listByIds(teamIds).stream()
                .map(team -> {return BeanUtil.toBean(team, TeamUserVo.class);})
                .collect(Collectors.toList());
        return collect;
    }
}




