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
    private String csr;
    private boolean deleted = false;
    private boolean renewed = false;

    @ObjectId
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getServer() {
        return server;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

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

	public String getCsr() {
		return csr;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isRenewed() {
		return renewed;
	}

	public void setCsr(String csr) {
		this.csr = csr;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public void setRenewed(boolean renewed) {
		this.renewed = renewed;
	}
    
    
}
