package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * User entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_GUEST_REQUEST")
public class GuestRequest implements Loggable {
    /**
     * User ID.
     */
    @Id
    @Column(name = "REQ_ID_C", length = 36)
    private String id;
    
    /**
     * User's username.
     */
    @Column(name = "REQ_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * User's password.
     */
    @Column(name = "REQ_PASSWORD_HASH_C", nullable = false, length = 100)
    private String password;
    
    /**
     * Email address.
     */
    @Column(name = "REQ_EMAIL_C", nullable = false, length = 100)
    private String email;

    /**
     * Creation date.
     */
    @Column(name = "REQ_CREATEDATE_D", nullable = false)
    private Date createDate;

    @Column(name = "REQ_APPROVED")
    private Boolean approved;

    /**
     * Deletion date.
     */
    @Column(name = "REQ_DELETEDATE_D")
    private Date deleteDate;
    
    public String getId() {
        return id;
    }

    public GuestRequest setId(String id) {
        this.id = id;
        return this;
    }
    
    public String getUsername() {
        return username;
    }

    public GuestRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public GuestRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public GuestRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public GuestRequest setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Boolean getApproved() {
        return approved;
    }

    public GuestRequest setApproved(Boolean approved) {
        this.approved = approved;
        return this;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public GuestRequest setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .toString();
    }

    @Override
    public String toMessage() {
        return username;
    }
}
