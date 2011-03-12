package com.techingen.twentyat.packetinterceptor.data;
// Generated 9 Mar, 2011 4:37:06 PM by Hibernate Tools 3.2.1.GA



/**
 * ContactPerson generated by hbm2java
 */
public class ContactPerson  implements java.io.Serializable {


     private Integer contactId;
     private TwentyatUser twentyatUser;
     private String firstName;
     private String lastName;
     private String email;
     private String mobilePhone;
     private String photo;
     private String friendlyName;
     private String middleName;
     private String facebookId;
     private String streetAddress;
     private String locality;
     private String region;
     private Integer postalCode;
     private String country;

    public ContactPerson() {
    }

    public ContactPerson(TwentyatUser twentyatUser, String firstName, String lastName, String email, String mobilePhone, String photo, String friendlyName, String middleName, String facebookId, String streetAddress, String locality, String region, Integer postalCode, String country) {
       this.twentyatUser = twentyatUser;
       this.firstName = firstName;
       this.lastName = lastName;
       this.email = email;
       this.mobilePhone = mobilePhone;
       this.photo = photo;
       this.friendlyName = friendlyName;
       this.middleName = middleName;
       this.facebookId = facebookId;
       this.streetAddress = streetAddress;
       this.locality = locality;
       this.region = region;
       this.postalCode = postalCode;
       this.country = country;
    }
   
    public Integer getContactId() {
        return this.contactId;
    }
    
    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }
    public TwentyatUser getTwentyatUser() {
        return this.twentyatUser;
    }
    
    public void setTwentyatUser(TwentyatUser twentyatUser) {
        this.twentyatUser = twentyatUser;
    }
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    public String getMobilePhone() {
        return this.mobilePhone;
    }
    
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }
    public String getPhoto() {
        return this.photo;
    }
    
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    public String getFriendlyName() {
        return this.friendlyName;
    }
    
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
    public String getMiddleName() {
        return this.middleName;
    }
    
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    public String getFacebookId() {
        return this.facebookId;
    }
    
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
    public String getStreetAddress() {
        return this.streetAddress;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
    public String getLocality() {
        return this.locality;
    }
    
    public void setLocality(String locality) {
        this.locality = locality;
    }
    public String getRegion() {
        return this.region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    public Integer getPostalCode() {
        return this.postalCode;
    }
    
    public void setPostalCode(Integer postalCode) {
        this.postalCode = postalCode;
    }
    public String getCountry() {
        return this.country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }




}


