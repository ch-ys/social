package com.yupi.yupao.model.domain.entiy;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long teamId;

    private Date joinTime;

    private Date createTime;

    private Date updateTime;
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}