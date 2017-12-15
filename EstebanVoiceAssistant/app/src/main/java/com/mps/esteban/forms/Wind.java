package com.mps.esteban.forms;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cosmin on 15.12.2017.
 */

public class Wind {

    @SerializedName("speed")
    @Expose
    private float speed;

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
