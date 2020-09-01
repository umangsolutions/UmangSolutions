package com.example.samplefirebase.modals;

public class IndividualFaceData {
    private String leftEar, rightEar, leftEye, rightEye, leftMouth, rightMouth, noseBase;
    private String imageUrl;

    public IndividualFaceData() {
    }

    public IndividualFaceData(String leftEar, String rightEar, String leftEye, String rightEye, String leftMouth, String rightMouth, String noseBase, String imageUrl) {
        this.leftEar = leftEar;
        this.rightEar = rightEar;
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        this.leftMouth = leftMouth;
        this.rightMouth = rightMouth;
        this.noseBase = noseBase;
        this.imageUrl = imageUrl;
    }

    public String getLeftEar() {
        return leftEar;
    }

    public void setLeftEar(String leftEar) {
        this.leftEar = leftEar;
    }

    public String getRightEar() {
        return rightEar;
    }

    public void setRightEar(String rightEar) {
        this.rightEar = rightEar;
    }

    public String getLeftEye() {
        return leftEye;
    }

    public void setLeftEye(String leftEye) {
        this.leftEye = leftEye;
    }

    public String getRightEye() {
        return rightEye;
    }

    public void setRightEye(String rightEye) {
        this.rightEye = rightEye;
    }

    public String getLeftMouth() {
        return leftMouth;
    }

    public void setLeftMouth(String leftMouth) {
        this.leftMouth = leftMouth;
    }

    public String getRightMouth() {
        return rightMouth;
    }

    public void setRightMouth(String rightMouth) {
        this.rightMouth = rightMouth;
    }

    public String getNoseBase() {
        return noseBase;
    }

    public void setNoseBase(String noseBase) {
        this.noseBase = noseBase;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
