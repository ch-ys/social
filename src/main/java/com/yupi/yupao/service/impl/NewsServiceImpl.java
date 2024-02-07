package com.yupi.yupao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.entiy.News;
import com.yupi.yupao.model.domain.request.NewsAddRequest;
import com.yupi.yupao.model.domain.request.NewsQueryRequest;
import com.yupi.yupao.model.domain.request.NewsUpdateRequest;
import com.yupi.yupao.service.NewsService;
import com.yupi.yupao.mapper.NewsMapper;
import com.yupi.yupao.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
* @author chenmoys
* @description 针对表【news(用户)】的数据库操作Service实现
* @createDate 2024-02-07 12:10:54
*/
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
    implements NewsService{

    @Resource
    private UserService userService;

    @Override
    public Boolean addNews(NewsAddRequest newsAddRequest, HttpServletRequest request) {
        //权限验证
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"需要管理员才能发布新闻");
        }
        //参数验证
        String title = newsAddRequest.getTitle();
        if (title == null ||title.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题不能过长");
        }
        String text = newsAddRequest.getText();
        if (text == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"新闻内容不能为空");
        }
        String tags = newsAddRequest.getTags();
        if (tags == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入新闻标签");
        }
        News news = new News();
        news.setTitle(title);
        news.setText(text);
        news.setTags(tags);
        news.setCreateTime(new Date());
        news.setUpdateTime(new Date());
        return save(news);
    }

    @Override
    public boolean deleteNews(Integer id, HttpServletRequest httpServletRequest) {
        //权限验证
        if (!userService.isAdmin(httpServletRequest)){
            throw new BusinessException(ErrorCode.NO_AUTH,"需要管理员才有权限删除");
        }
        //参数验证
        News byId = getById(id);
        if (byId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该记录为空，请勿重复删除空记录");
        }
        return removeById(id);
    }

    @Override
    public Boolean updateNews(NewsUpdateRequest newsUpdateRequest, HttpServletRequest httpServletRequest) {
        //权限验证
        if (!userService.isAdmin(httpServletRequest)){
            throw new BusinessException(ErrorCode.NO_AUTH,"需要管理员才有权限删除");
        }
        //参数验证
        Long id = newsUpdateRequest.getId();
        if (id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入要更新的新闻id");
        }
        //更新内容不能为空
        NewsAddRequest news = BeanUtil.toBean(newsUpdateRequest, NewsAddRequest.class);
        if (news == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新的内容不能为空");
        }
        //设置更新值
        News updateNews = new News();
        updateNews.setId(id);
        String title = news.getTitle();
        if (title != null){
            updateNews.setTitle(title);
        }
        String text = news.getText();
        if (text != null){
            updateNews.setText(text);
        }
        String tags = news.getTags();
        if (tags != null){
            updateNews.setTags(tags);
        }
        return updateById(updateNews);
    }

    @Override
    public Page<News> listPageNews(NewsQueryRequest newsQueryRequest) {
        //构造器
        QueryWrapper<News> newsQueryWrapper = new QueryWrapper<>();
        String searchText = newsQueryRequest.getSearchText();
        Long pageNum = newsQueryRequest.getPageNum();
        Long pageSize = newsQueryRequest.getPageSize();
        if (searchText != null){
            newsQueryWrapper.and(qw -> {
                qw.like("title", searchText)
                        .or()
                        .like("text", searchText);
            });
        }
        //查询标题或者文本
        return page(new Page<News>(pageNum,pageSize),newsQueryWrapper);
    }

    @Override
    public List<News> listNews(NewsQueryRequest newsQueryRequest, HttpServletRequest httpServletRequest) {
        //权限验证
        if (!userService.isAdmin(httpServletRequest)){
            throw new BusinessException(ErrorCode.NO_AUTH,"管理员才有不分页查询权限");
        }
        //查询
        //构造器
        QueryWrapper<News> newsQueryWrapper = new QueryWrapper<>();
        String searchText = newsQueryRequest.getSearchText();
        if (searchText != null){
            newsQueryWrapper.and(qw -> {
                qw.like("title", searchText)
                        .or()
                        .like("text", searchText);
            });
        }
        List<String> searchTags = newsQueryRequest.getSearchTags();
        if (searchTags != null){
            for (String tag: searchTags) {
                newsQueryWrapper.like("tags",tag);
            }
        }
        return list(newsQueryWrapper);
    }
}




