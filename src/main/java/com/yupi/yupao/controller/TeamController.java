package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.vo.TeamUserVo;
import com.yupi.yupao.model.domain.request.*;
import com.yupi.yupao.model.domain.entiy.Team;
import com.yupi.yupao.service.TeamService;


import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Long teamId = teamService.addTeam(teamAddRequest, request);
        return ResultUtils.success(teamId);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Integer id,HttpServletRequest httpServletRequest){
        if (id < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean remove = teamService.deleteTeam(id,httpServletRequest);
        return ResultUtils.success(remove);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest httpServletRequest){
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Boolean b = teamService.updateTeam(teamUpdateRequest, httpServletRequest);
        return ResultUtils.success(b);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(TeamJoinRequest teamJoinRequest){
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Boolean b = teamService.joinTeam(teamJoinRequest);
        return ResultUtils.success(b);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(TeamLeaveRequest teamLeaveRequest){
        if (teamLeaveRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Boolean b = teamService.quitTeam(teamLeaveRequest);
        return ResultUtils.success(b);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<TeamUserVo>> TeamUserVoPage(TeamQueryRequest teamQuery,HttpServletRequest httpServletRequest){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Page<TeamUserVo> teamUserVoPage = teamService.searchTeamUserPage(teamQuery,httpServletRequest);
        return ResultUtils.success(teamUserVoPage);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeamUserVo(TeamQueryRequest teamQuery,HttpServletRequest httpServletRequest){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        List<TeamUserVo> teamUserVoPage = teamService.searchTeamUser(teamQuery,httpServletRequest);
        return ResultUtils.success(teamUserVoPage);
    }

    @GetMapping("/list/team")
    public BaseResponse<List<Team>> listTeam(TeamQueryRequest teamQuery,HttpServletRequest httpServletRequest){
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        List<Team> teams = teamService.listTeam(teamQuery, httpServletRequest);
        return ResultUtils.success(teams);
    }

    @GetMapping("/list/myCreate")
    public BaseResponse<List<TeamUserVo>> listMyCreate(HttpServletRequest httpServletRequest){

        List<TeamUserVo> teamUserVos = teamService.listMyCreate(httpServletRequest);
        return ResultUtils.success(teamUserVos);
    }

    @GetMapping("/list/myJoin")
    public BaseResponse<List<TeamUserVo>> listMyJoin(HttpServletRequest httpServletRequest){
        List<TeamUserVo> teamUserVos = teamService.listMyJoin(httpServletRequest);
        return ResultUtils.success(teamUserVos);
    }
}
