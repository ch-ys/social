package com.yupi.yupao.model.domain.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class TeamUpdateDto {
    private String name;

    private String description;

    private Integer maxNum;

    private Date expireTime;

    /**
     * 0公开 1私有 3加密
     */
    private Integer status;

    private String password;
}
