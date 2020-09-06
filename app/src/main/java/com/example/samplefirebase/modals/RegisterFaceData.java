package com.example.samplefirebase.modals;

import java.io.Serializable;

public class RegisterFaceData implements Serializable {
    private String name, JNTU, section, department;
    private Object imgUrl;
    private Float bottom;
    private Boolean empty;
    private Float left,right,top;

    public RegisterFaceData() {
    }

    public RegisterFaceData(String name, String JNTU, String section, String department, Object imgUrl, Float bottom, Boolean empty, Float left, Float right, Float top) {
        this.name = name;
        this.JNTU = JNTU;
        this.section = section;
        this.department = department;
        this.imgUrl = imgUrl;
        this.bottom = bottom;
        this.empty = empty;
        this.left = left;
        this.right = right;
        this.top = top;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJNTU() {
        return JNTU;
    }

    public void setJNTU(String JNTU) {
        this.JNTU = JNTU;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Object getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(Object imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Float getBottom() {
        return bottom;
    }

    public void setBottom(Float bottom) {
        this.bottom = bottom;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Float getLeft() {
        return left;
    }

    public void setLeft(Float left) {
        this.left = left;
    }

    public Float getRight() {
        return right;
    }

    public void setRight(Float right) {
        this.right = right;
    }

    public Float getTop() {
        return top;
    }

    public void setTop(Float top) {
        this.top = top;
    }
}
