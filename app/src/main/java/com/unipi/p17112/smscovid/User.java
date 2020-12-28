package com.unipi.p17112.smscovid;

public class User {
    String firstName, lastName, email, address;

    public User(String firstName, String lastName, String email, String address){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
    }
    public User(){

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
