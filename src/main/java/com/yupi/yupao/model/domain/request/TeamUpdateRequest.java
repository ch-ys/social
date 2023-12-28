package com.yupi.yupao.model.domain.request;

import lombok.Data;

import java.util.Date;

@Data
public class TeamUpdateRequest {

    private Long id;

    private String name;

    private String description;

    private Integer maxNum;

    private Date expireTime;

    private Long userId;
    /**
     * 0公开 1私有 3加密
     */
    private Integer status;

    private String password;

}
