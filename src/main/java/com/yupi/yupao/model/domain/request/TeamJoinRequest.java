package com.yupi.yupao.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName team
 */

@Data
public class TeamJoinRequest implements Serializable {

    private Long teamId;

    private Long userId;

    private String password;
}