package com.yupi.yupao.model.domain.request;

import com.yupi.yupao.common.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class SearchQueryRequest extends Page implements Serializable {

    private Integer type;

    private String searchText;
}
