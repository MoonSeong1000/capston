package com.example.test3;

public class Dictionary {

    private String name;
    private String phoneNum;

    boolean selected;

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Dictionary(String name, String phoneNum) {
        this.name = name;
        this.phoneNum = phoneNum;
    }
}


