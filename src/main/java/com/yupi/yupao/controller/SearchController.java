package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;

import com.yupi.yupao.datasource.*;
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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private DateSourceRegistry dateSourceRegistry;

    @GetMapping()
    public BaseResponse<Page<Object>> allPage(SearchQueryRequest searchQueryRequest) {
        Integer type = searchQueryRequest.getType();
        String searchText = searchQueryRequest.getSearchText();
        //参数为空
        if (type == null || searchText == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入类型和查询关键词");
        }
        //分页参数
        Long pageNum = searchQueryRequest.getPageNum();
        Long pageSize = searchQueryRequest.getPageSize();
        Page<Object> page = new Page<>(pageNum, pageSize);
        //注册器
        DataSource dataSource = dateSourceRegistry.getDataSource(type);
        List<Object> objects = dataSource.doSearch(searchText);
        page.setRecords(objects);
        return ResultUtils.success(page);
    }
}
