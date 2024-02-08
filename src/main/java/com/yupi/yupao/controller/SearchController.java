package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;

import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.entiy.News;
import com.yupi.yupao.model.domain.entiy.Team;
import com.yupi.yupao.model.domain.entiy.User;
import com.yupi.yupao.model.domain.entiy.UserTeam;
import com.yupi.yupao.model.domain.enums.SearchTypeStatusEnum;
import com.yupi.yupao.model.domain.enums.TeamStatusEnum;
import com.yupi.yupao.model.domain.request.SearchQueryRequest;
import com.yupi.yupao.model.domain.vo.TeamUserVo;
import com.yupi.yupao.service.NewsService;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private NewsService newsService;

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @GetMapping()
    public BaseResponse<Page<Object>> allPage(SearchQueryRequest searchQueryRequest){
        Integer type = searchQueryRequest.getType();
        String searchText = searchQueryRequest.getSearchText();
        //参数为空
        if (type == null || searchText == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入类型和查询关键词");
        }
        //分页参数
        Long pageNum = searchQueryRequest.getPageNum();
        Long pageSize = searchQueryRequest.getPageSize();
        Page<Object> objectPage = new Page<>(pageNum, pageSize);
        //匹配情况
        switch (type) {
            case 0:
                //查询标题或者文本
                QueryWrapper<News> newsQueryWrapper = new QueryWrapper<>();
                newsQueryWrapper.and(qw -> {
                    qw.like("title", searchText)
                            .or()
                            .like("text", searchText);
                });
                List<Object> collect = newsService.list(newsQueryWrapper).stream().map(news -> {
                       return (Object) news;
                }).collect(Collectors.toList());
                objectPage.setRecords(collect);
                break;
            case 1:
                QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
                //查询名称或者描述
                if (StringUtils.isNotBlank(searchText)){
                    teamQueryWrapper.and(qw -> qw.like("name",searchText)
                            .or().like("description",searchText));
                }
                //时间大于当前
                teamQueryWrapper.and(qw ->qw.gt("expireTime",new Date()).or().isNull("expireTime"));
                //查询公共队伍(只有管理员可以查看非公共队伍）
                Integer status = TeamStatusEnum.PUBLIC.getValue();
                teamQueryWrapper.eq("status",status);
                //查询team
                List<Team> list = teamService.list(teamQueryWrapper);
                List<TeamUserVo> teamUserVoList = null;
                if (list != null) {
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
                        //List<User> userList = userTeamService.findTeamUsers(teamId);
                        //组装
                        TeamUserVo teamUserVo = new TeamUserVo();
                        BeanUtils.copyProperties(team, teamUserVo);
                        teamUserVo.setUserList(userList);
                        return teamUserVo;
                    }).collect(Collectors.toList());
                }
                List<Object> collect1 = teamUserVoList.stream().map(teamUserVo -> {
                    return (Object) teamUserVo;
                }).collect(Collectors.toList());
                objectPage.setRecords(collect1);
                break;
            case 2:
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                //构造条件
                userQueryWrapper.like("username",searchText);
                // 脱敏
                List<User> userList = userService.list(userQueryWrapper);
                List<Object> collect2 = userList.stream().map(user -> {
                    User safetyUser = userService.getSafetyUser(user);
                    return (Object) safetyUser;
                }).collect(Collectors.toList());
                objectPage.setRecords(collect2);
        }
        return ResultUtils.success(objectPage);
    }


}
