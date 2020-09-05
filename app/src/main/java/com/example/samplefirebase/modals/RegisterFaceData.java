package com.example.samplefirebase.modals;

public class RegisterFaceData {
    private String name, JNTU, section, department,imgUrl;

    public RegisterFaceData() {
    }

    public RegisterFaceData(String name, String JNTU, String section, String department, String imgUrl) {
        this.name = name;
        this.JNTU = JNTU;
        this.section = section;
        this.department = department;
        this.imgUrl = imgUrl;
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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
