package com.yupi.yupao.model.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName team
 */
@TableName(value ="team")
@Data
public class TeamAddRequest implements Serializable {

    private String name;

    private String description;

    private Integer maxNum;

    private Date expireTime;

    private Integer status;

    private String password;

}