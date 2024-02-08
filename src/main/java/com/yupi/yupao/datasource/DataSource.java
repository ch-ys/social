package com.yupi.yupao.datasource;

import java.util.List;

public interface DataSource {

    List<Object> doSearch(String searchText);
}
