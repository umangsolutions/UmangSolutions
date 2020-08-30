package com.example.samplefirebase.modals;

public class UserData {
    private String name;
    private String rollNumber;
    private String phone;
    private String emailID;
    private String password;

    public UserData() {
    }

    public UserData(String name, String rollNumber, String phone, String emailID, String password) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.phone = phone;
        this.emailID = emailID;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
