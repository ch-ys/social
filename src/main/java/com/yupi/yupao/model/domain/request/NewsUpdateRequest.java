package com.yupi.yupao.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @TableName team
 */

@Data
public class NewsUpdateRequest implements Serializable {

    private Long id;

    private String title;

    private String text;

    private String tags;

}