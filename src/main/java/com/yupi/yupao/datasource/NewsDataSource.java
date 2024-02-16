package com.yupi.yupao.datasource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.yupi.yupao.model.domain.entiy.News;
import com.yupi.yupao.service.NewsService;
import org.elasticsearch.client.ElasticsearchClient;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsDataSource implements DataSource {

    @Resource
    private NewsService newsService;

    public List<Object> doSearch(String searchText){
        //查询标题或者文本
        QueryWrapper<News> newsQueryWrapper = new QueryWrapper<>();
        newsQueryWrapper.and(qw -> {
            List<Long> longs = newsService.searchFromEs(searchText);
            qw.like("title", searchText);
            if (longs != null) {
                qw.or().in("id", longs);
            }
        });
        List<Object> collect = newsService.list(newsQueryWrapper).stream()
                .map(news -> (Object) news)
                .collect(Collectors.toList());
        return collect;
    };
}
