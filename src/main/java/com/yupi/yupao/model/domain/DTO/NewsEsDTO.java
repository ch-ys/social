package com.yupi.yupao.model.domain.dto;


import com.yupi.yupao.model.domain.entiy.News;
import com.yupi.yupao.model.domain.entiy.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;


/**
 * news ES 包装类
 **/
@Document(indexName = "news")
@Data
public class NewsEsDTO implements Serializable {

    /**
     * id
     */
    @Id
    private Long id;

    private String text;


    public static NewsEsDTO objToDto(News news) {
        if (news == null) {
            return null;
        }
        NewsEsDTO newsEsDTO= new NewsEsDTO();
        BeanUtils.copyProperties(news, newsEsDTO);
        return newsEsDTO;
    }


    public static News dtoToObj(NewsEsDTO newsEsDTO) {
        if (newsEsDTO == null) {
            return null;
        }
        News news = new News();
        BeanUtils.copyProperties(newsEsDTO,news);
        return news;
    }
}
