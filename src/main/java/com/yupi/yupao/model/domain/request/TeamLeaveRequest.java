package com.yupi.yupao.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @TableName team
 */

@Data
public class TeamLeaveRequest implements Serializable {

    private Long teamId;

    private Long userId;
}