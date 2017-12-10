package com.mps.esteban.forms;

import org.json.JSONObject;

/**
 * Created by cosmin on 10.12.2017.
 */

public class FacebookDetails {
    private String id;
    private String name;
    private String email;
    private int number_of_friends;

    public FacebookDetails(JSONObject jsonObject) {
        this.id = jsonObject.optString("id");
        this.name = jsonObject.optString("name");
        this.email = jsonObject.optString("email");
        this.number_of_friends = jsonObject
                .optJSONObject("friends")
                .optJSONObject("summary")
                .optInt("total_count");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNumber_of_friends() {
        return number_of_friends;
    }

    public void setNumber_of_friends(int number_of_friends) {
        this.number_of_friends = number_of_friends;
    }
}
