package com.yupi.yupao.model.domain.request;

import com.yupi.yupao.common.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class TeamQueryRequest extends Page implements Serializable {

    private Long id;

    private String name;

    private String searchText;

    private Integer maxNum;

    private Date expireTime;

    private Integer status;

}
