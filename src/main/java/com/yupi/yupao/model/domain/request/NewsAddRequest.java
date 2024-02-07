package com.yupi.yupao.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName team
 */

@Data
public class NewsAddRequest implements Serializable {

    private String title;

    private String text;

    private String tags;

}