package com.yupi.yupao.model.domain.entiy;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Integer maxNum;

    private Date expireTime;

    private Long userId;

    private Integer status;

    private String password;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}