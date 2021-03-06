package com.techingen.twentyat.packetinterceptor.data;
// Generated 9 Mar, 2011 4:37:06 PM by Hibernate Tools 3.2.1.GA


import java.util.HashSet;
import java.util.Set;

/**
 * TwentyatGroup generated by hbm2java
 */
public class TwentyatGroup  implements java.io.Serializable {


     private Integer twentyatGroupId;
     private TwentyatUser twentyatUser;
     private String groupName;
     private Set<FriendsMapping> friendsMappings = new HashSet<FriendsMapping>(0);

    public TwentyatGroup() {
    }

    public TwentyatGroup(TwentyatUser twentyatUser, String groupName, Set<FriendsMapping> friendsMappings) {
       this.twentyatUser = twentyatUser;
       this.groupName = groupName;
       this.friendsMappings = friendsMappings;
    }
   
    public Integer getTwentyatGroupId() {
        return this.twentyatGroupId;
    }
    
    public void setTwentyatGroupId(Integer twentyatGroupId) {
        this.twentyatGroupId = twentyatGroupId;
    }
    public TwentyatUser getTwentyatUser() {
        return this.twentyatUser;
    }
    
    public void setTwentyatUser(TwentyatUser twentyatUser) {
        this.twentyatUser = twentyatUser;
    }
    public String getGroupName() {
        return this.groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public Set<FriendsMapping> getFriendsMappings() {
        return this.friendsMappings;
    }
    
    public void setFriendsMappings(Set<FriendsMapping> friendsMappings) {
        this.friendsMappings = friendsMappings;
    }




}


