package com.tngtech.confluence.plugin.data;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "interested")
@XmlAccessorType(XmlAccessType.FIELD)
public class VoteResponse {
    @XmlElement(name = "id") //TODO
    private String id;
    @XmlAttribute
    private String users;
    @XmlAttribute
    private int userNo;

    public VoteResponse() {

    }
   
    public VoteResponse(String id, String users, int userNo) {
        this.id = id;
        this.users = users;
        this.userNo = userNo;
    }

    public String getId() {
        return id;
    }

    public String getUsers() {
        return users;
    }

    public int getUserNo() {
        return userNo;
    }
}
