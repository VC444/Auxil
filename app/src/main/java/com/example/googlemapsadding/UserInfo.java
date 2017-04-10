package com.example.googlemapsadding;

/**
 * Created by Vignesh on 4/8/2017.
 */

public class UserInfo
{
    String latitude;
    String longitude;
    String id;

    public UserInfo()
    {

    }

    public UserInfo(String latitude, String longitude, String id)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
    }

    public String getLatitude()
    {
        return latitude;
    }

    public void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }

    public String getLongitude()
    {
        return longitude;
    }

    public void setLongitude(String longitude)
    {
        this.longitude = longitude;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
