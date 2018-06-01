package com.example.jagdishduwal.bhaktapurquickroute.db;


public class Node {

    //private variables
    int _id;
    String _latitude;
    String _longitude;



    // Empty constructor
    public Node(){

    }
    // constructor



    public Node(int id, String latitude, String longitude){
        this._id = id;
        this._latitude = latitude;
        this._longitude = longitude;

    }




    // getting ID
    public int getID(){
        return this._id;
    }

    public void setID(int id){
        this._id = id;
    }

    public String getLatitude(){
        return this._latitude;
    }

    public void setLatitude(String latitude){
        this._latitude = latitude;
    }

    public String getLongitude(){
        return this._longitude;
    }

    public void setLongitude(String longitude){
        this._longitude = longitude;
    }

}

