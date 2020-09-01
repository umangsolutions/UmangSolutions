package com.example.samplefirebase.modals;

import android.graphics.Bitmap;
import android.graphics.PointF;

public class FaceLandmarkData {

    private Bitmap imageBitmap;
    private PointF leftEar, rightEar, leftEye, rightEye, leftMouth, rightMouth, noseBase;
    private String imageUrl;

    public FaceLandmarkData() {

    }

    public FaceLandmarkData(Bitmap imageBitmap, PointF leftEar, PointF rightEar, PointF leftEye, PointF rightEye, PointF leftMouth, PointF rightMouth, PointF noseBase, String imageUrl) {
        this.imageBitmap = imageBitmap;
        this.leftEar = leftEar;
        this.rightEar = rightEar;
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        this.leftMouth = leftMouth;
        this.rightMouth = rightMouth;
        this.noseBase = noseBase;
        this.imageUrl = imageUrl;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public PointF getLeftEar() {
        return leftEar;
    }

    public void setLeftEar(PointF leftEar) {
        this.leftEar = leftEar;
    }

    public PointF getRightEar() {
        return rightEar;
    }

    public void setRightEar(PointF rightEar) {
        this.rightEar = rightEar;
    }

    public PointF getLeftEye() {
        return leftEye;
    }

    public void setLeftEye(PointF leftEye) {
        this.leftEye = leftEye;
    }

    public PointF getRightEye() {
        return rightEye;
    }

    public void setRightEye(PointF rightEye) {
        this.rightEye = rightEye;
    }

    public PointF getLeftMouth() {
        return leftMouth;
    }

    public void setLeftMouth(PointF leftMouth) {
        this.leftMouth = leftMouth;
    }

    public PointF getRightMouth() {
        return rightMouth;
    }

    public void setRightMouth(PointF rightMouth) {
        this.rightMouth = rightMouth;
    }

    public PointF getNoseBase() {
        return noseBase;
    }

    public void setNoseBase(PointF noseBase) {
        this.noseBase = noseBase;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
