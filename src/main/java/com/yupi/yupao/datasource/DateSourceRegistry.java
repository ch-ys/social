package com.yupi.yupao.datasource;

import com.yupi.yupao.model.domain.enums.SearchTypeStatusEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class DateSourceRegistry {
    @Resource
    private NewsDataSource newsDataSource;

    @Resource
    private TeamDataSource teamDataSource;

    @Resource
    private UserDataSource userDataSource;

    private Map<Integer, DataSource> dataSourceHashMap;

    //初始化
    @PostConstruct
    public void doInit(){
        //注册器
        dataSourceHashMap = new HashMap(){{
            put(SearchTypeStatusEnum.NEWS.getValue(),newsDataSource);
            put(SearchTypeStatusEnum.TEAM.getValue(),teamDataSource);
            put(SearchTypeStatusEnum.USER.getValue(),userDataSource);
        }};
    }

    public DataSource getDataSource(Integer type){
        return dataSourceHashMap.get(type);
    }
}
