package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.model.domain.vo.TeamUserVo;
import com.yupi.yupao.model.domain.entiy.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.request.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author chenmoys
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-12-06 17:05:31
*/
public interface TeamService extends IService<Team> {

    Long addTeam(TeamAddRequest team, HttpServletRequest request);

    Page<TeamUserVo> searchTeamUserPage(TeamQueryRequest teamQuery, HttpServletRequest httpServletRequest);

    Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest httpServletRequest);

    Boolean joinTeam(TeamJoinRequest teamJoinRequest);

    Boolean quitTeam(TeamLeaveRequest teamLeaveRequest);

    Boolean deleteTeam(Integer id, HttpServletRequest httpServletRequest);

    List<TeamUserVo> listMyCreate(HttpServletRequest httpServletRequest);

    List<TeamUserVo> listMyJoin(HttpServletRequest httpServletRequest);

    List<Team> listTeam(TeamQueryRequest teamQuery, HttpServletRequest httpServletRequest);

    List<TeamUserVo> searchTeamUser(TeamQueryRequest teamQuery, HttpServletRequest httpServletRequest);

    List<Long> searchFromEs(String text);
}
