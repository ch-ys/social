package com.yupi.yupao.model.domain.dto;


import com.yupi.yupao.model.domain.entiy.News;
import com.yupi.yupao.model.domain.entiy.Team;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;


/**
 * news ES 包装类
 **/
@Document(indexName = "team")
@Data
public class TeamEsDTO implements Serializable {

    /**
     * id
     */
    @Id
    private Long id;

    private String description;

    public static TeamEsDTO objToDto(Team team) {
        if (team == null) {
            return null;
        }
        TeamEsDTO teamEsDTO= new TeamEsDTO();
        BeanUtils.copyProperties(team, teamEsDTO);
        return teamEsDTO;
    }


    public static Team dtoToObj(TeamEsDTO teamEsDTO) {
        if (teamEsDTO == null) {
            return null;
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamEsDTO,team);
        return team;
    }
}
