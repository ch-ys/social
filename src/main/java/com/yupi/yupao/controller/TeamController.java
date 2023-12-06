package com.yupi.yupao.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.request.TeamAddRequest;
import com.yupi.yupao.model.domain.request.TeamQueryRequest;
import com.yupi.yupao.model.domain.entiy.Team;
import com.yupi.yupao.service.TeamService;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (ObjectUtil.isEmpty(teamAddRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Long teamId = teamService.addTeam(teamAddRequest, request);
        return ResultUtils.success(teamId);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Integer id){
        if (id < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean remove = teamService.removeById(id);
        return ResultUtils.success(remove);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team){
        if (ObjectUtil.isEmpty(team)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean b = teamService.updateById(team);
        return ResultUtils.success(b);
    }

    @GetMapping("/search")
    public BaseResponse<Team> getTeamById(TeamQueryRequest teamQuery){
        if (ObjectUtil.isEmpty(teamQuery)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Team query = BeanUtil.toBean(teamQuery, Team.class);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(query);
        Team team = teamService.getOne(teamQueryWrapper);
        return ResultUtils.success(team);
    }

    @GetMapping("/search/page")
    public BaseResponse<Page<Team>> searchPageTeam(TeamQueryRequest teamQuery){
        if (ObjectUtil.isEmpty(teamQuery)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Team query = BeanUtil.toBean(teamQuery, Team.class);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(query);
        Page<Team> page = teamService.page(new Page<Team>(teamQuery.getPageNum(), teamQuery.getPageSize()), teamQueryWrapper);
        return ResultUtils.success(page);
    }
}
