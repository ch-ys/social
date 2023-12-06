package com.yupi.yupao.service;

import com.yupi.yupao.model.domain.entiy.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.request.TeamAddRequest;

import javax.servlet.http.HttpServletRequest;

/**
* @author chenmoys
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-12-06 17:05:31
*/
public interface TeamService extends IService<Team> {

    Long addTeam(TeamAddRequest team, HttpServletRequest request);
}
