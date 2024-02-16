package com.yupi.yupao.datasource;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupao.model.domain.entiy.Team;
import com.yupi.yupao.model.domain.entiy.User;
import com.yupi.yupao.model.domain.entiy.UserTeam;
import com.yupi.yupao.model.domain.enums.TeamStatusEnum;
import com.yupi.yupao.model.domain.vo.TeamUserVo;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeamDataSource implements DataSource {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    public List<Object> doSearch(String searchText){
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        //查询名称或者描述
        if (StringUtils.isNotBlank(searchText)){
            List<Long> longs = teamService.searchFromEs(searchText);
            teamQueryWrapper.and(qw -> {
                qw.like("name",searchText);
                if (longs != null){
                    qw.or().in("id",longs);
                }
            });
        }
        //时间大于当前
        teamQueryWrapper.and(qw -> qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        //查询公共队伍(只有管理员可以查看非公共队伍）
        Integer status = TeamStatusEnum.PUBLIC.getValue();
        teamQueryWrapper.eq("status",status);
        //查询team
        List<Team> list = teamService.list(teamQueryWrapper);
        List<Object> collect = null;
        if (list != null) {
            collect = list.stream().map(team -> {
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
                return (Object) teamUserVo;
            }).collect(Collectors.toList());
        }
        return collect;
    };
}
