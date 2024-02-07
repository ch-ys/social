package com.yupi.yupao.model.domain.entiy;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName news
 */
@TableName(value ="news")
@Data
public class News implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String text;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    private String tags;

    private static final long serialVersionUID = 1L;
}