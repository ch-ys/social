package com.yupi.yupao.model.domain.request;



import com.yupi.yupao.common.Page;

import java.util.Date;

public class TeamQueryRequest extends Page {

    private Long id;

    private String name;

    private String description;

    private Integer maxNum;

    private Date expireTime;

    private Long userId;

    private Integer status;

}
