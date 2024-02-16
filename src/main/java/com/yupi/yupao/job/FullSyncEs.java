package com.yupi.yupao.job;

import com.yupi.yupao.model.domain.dto.NewsEsDTO;
import com.yupi.yupao.model.domain.dto.TeamEsDTO;
import com.yupi.yupao.model.domain.entiy.News;
import com.yupi.yupao.model.domain.entiy.Team;
import com.yupi.yupao.service.NewsService;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

//@Component
@Slf4j
public class FullSyncEs implements CommandLineRunner {
    @Resource
    private TeamService teamService;
    @Resource
    private NewsService newsService;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void run(String... args) throws Exception {
        List<TeamEsDTO> teamEsDTOList = teamService.list().stream()
                .map(TeamEsDTO::objToDto)
                .collect(Collectors.toList());
        elasticsearchRestTemplate.save(teamEsDTOList);
        List<NewsEsDTO> newsEsDTOList = newsService.list().stream()
                .map(NewsEsDTO::objToDto)
                .collect(Collectors.toList());
        elasticsearchRestTemplate.save(newsEsDTOList);
    }
}
