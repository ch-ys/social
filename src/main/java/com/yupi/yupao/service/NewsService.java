package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.model.domain.entiy.News;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.request.NewsAddRequest;
import com.yupi.yupao.model.domain.request.NewsQueryRequest;
import com.yupi.yupao.model.domain.request.NewsUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author chenmoys
* @description 针对表【news(用户)】的数据库操作Service
* @createDate 2024-02-07 12:10:54
*/
public interface NewsService extends IService<News> {

    Boolean addNews(NewsAddRequest newsAddRequest, HttpServletRequest request);

    Boolean deleteNews(Integer id, HttpServletRequest httpServletRequest);

    Boolean updateNews(NewsUpdateRequest newsAddRequest, HttpServletRequest httpServletRequest);

    Page<News> listPageNews(NewsQueryRequest newsQueryRequest);

    List<News> listNews(NewsQueryRequest newsQueryRequest, HttpServletRequest httpServletRequest);

    List<Long> searchFromEs(String text);
}
