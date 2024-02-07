package com.yupi.yupao.model.domain.enums;

/**
 * 通用查询类型枚举
 */
public enum SearchTypeStatusEnum {

    NEWS(0, "新闻"),
    TEAM(1, "队伍"),
    USER(2, "用户");

    private int value;

    private String text;

    public static SearchTypeStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        SearchTypeStatusEnum[] values = SearchTypeStatusEnum.values();
        for (SearchTypeStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    SearchTypeStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
