package com.yupi.yupao.common;

import lombok.Data;

@Data
public class Page {
    private Long pageNum = 1L;
    private Long pageSize = 5L;
}
