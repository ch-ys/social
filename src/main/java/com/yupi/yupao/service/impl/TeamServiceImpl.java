package com.yupi.yupao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.entiy.Team;
import com.yupi.yupao.model.domain.entiy.User;
import com.yupi.yupao.model.domain.entiy.UserTeam;
import com.yupi.yupao.model.domain.enums.TeamStatusEnum;
import com.yupi.yupao.model.domain.request.TeamAddRequest;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.mapper.TeamMapper;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

/**
* @author chenmoys
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-12-06 17:05:31
*/
@Service
@Transactional(rollbackFor=Exception.class)
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    @Override
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
}




