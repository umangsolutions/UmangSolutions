package com.example.samplefirebase.modals;

public class RegisterFaceData {
    private String name, JNTU, section, department;
    private float d1_val, d2_val, d3_val, d4_val, d5_val;

    public RegisterFaceData() {
    }

    public RegisterFaceData(String name, String JNTU, String section, String department, float d1_val, float d2_val, float d3_val, float d4_val, float d5_val) {
        this.name = name;
        this.JNTU = JNTU;
        this.section = section;
        this.department = department;
        this.d1_val = d1_val;
        this.d2_val = d2_val;
        this.d3_val = d3_val;
        this.d4_val = d4_val;
        this.d5_val = d5_val;
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

    public float getD1_val() {
        return d1_val;
    }

    public void setD1_val(float d1_val) {
        this.d1_val = d1_val;
    }

    public float getD2_val() {
        return d2_val;
    }

    public void setD2_val(float d2_val) {
        this.d2_val = d2_val;
    }

    public float getD3_val() {
        return d3_val;
    }

    public void setD3_val(float d3_val) {
        this.d3_val = d3_val;
    }

    public float getD4_val() {
        return d4_val;
    }

    public void setD4_val(float d4_val) {
        this.d4_val = d4_val;
    }

    public float getD5_val() {
        return d5_val;
    }

    public void setD5_val(float d5_val) {
        this.d5_val = d5_val;
    }
}
