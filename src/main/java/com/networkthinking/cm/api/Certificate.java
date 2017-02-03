/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.networkthinking.cm.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import org.mongojack.ObjectId;

/**
 *
 * @author Shawn
 */
public class Certificate {

    @ObjectId
    @JsonProperty("_id")
    private String id;
    private String commonName;
    private String server;
    private Date expirationDate;
    private String privateKey;
    private String publicKey;
    private String caChain;

    public Certificate() {
        // Jackson deserialization
    }

    @ObjectId
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty
    public String getCommonName() {
        return commonName;
    }

    @JsonProperty
    public String getServer() {
        return server;
    }

    @JsonProperty
    public Date getExpirationDate() {
        return expirationDate;
    }

    @JsonProperty
    public String getPrivateKey() {
        return privateKey;
    }

    @JsonProperty
    public String getPublicKey() {
        return publicKey;
    }

    @JsonProperty
    public String getCaChain() {
        return caChain;
    }

    @ObjectId
    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setCaChain(String caChain) {
        this.caChain = caChain;
    }
    
}
