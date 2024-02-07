package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.entiy.News;
import com.yupi.yupao.model.domain.vo.TeamUserVo;
import com.yupi.yupao.model.domain.entiy.Team;
import com.yupi.yupao.model.domain.request.*;
import com.yupi.yupao.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/news")
public class NewsController {

    @Resource
    private NewsService newsService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addNews(NewsAddRequest newsAddRequest, HttpServletRequest request){
        if (newsAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Boolean news = newsService.addNews(newsAddRequest,request);
        return ResultUtils.success(news);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Integer id,HttpServletRequest httpServletRequest){
        if (id < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean remove = newsService.deleteNews(id,httpServletRequest);
        return ResultUtils.success(remove);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateNews(NewsUpdateRequest newsUpdateRequest, HttpServletRequest httpServletRequest){
        if (newsUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Boolean b = newsService.updateNews(newsUpdateRequest, httpServletRequest);
        return ResultUtils.success(b);
    }

    @GetMapping("/list")
    public BaseResponse<List<News>> listNews(NewsQueryRequest newsQueryRequest,HttpServletRequest httpServletRequest){
        List<News> teamUserVos = newsService.listNews(newsQueryRequest,httpServletRequest);
        return ResultUtils.success(teamUserVos);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<News>> listPageNews(NewsQueryRequest newsQueryRequest){
        Page<News> page = newsService.listPageNews(newsQueryRequest);
        return ResultUtils.success(page);
    }


}
