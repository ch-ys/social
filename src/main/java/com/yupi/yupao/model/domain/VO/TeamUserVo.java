package com.yupi.yupao.model.domain.VO;

import com.yupi.yupao.model.domain.entiy.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TeamUserVo implements Serializable {

    private Long id;

    private String name;

    private String description;

    private Integer maxNum;

    private Date expireTime;

    private Long userId;
    /**
     * 0公开 1私有 2加密
     */
    private Integer status;

    private List<User> userList;

}
