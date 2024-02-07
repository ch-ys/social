package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.entiy.News;
import com.yupi.yupao.model.domain.request.NewsAddRequest;
import com.yupi.yupao.model.domain.request.NewsQueryRequest;
import com.yupi.yupao.model.domain.request.NewsUpdateRequest;
import com.yupi.yupao.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private NewsService newsService;










    @GetMapping("/list/page")
    public BaseResponse<Page<News>> listPageNews(NewsQueryRequest newsQueryRequest){
        Page<News> page = newsService.listPageNews(newsQueryRequest);
        return ResultUtils.success(page);
    }


}
