package com.sismics.docs.core.dao.dto;

import com.google.common.base.MoreObjects;

/**
 * GuestRequest DTO.
 *
 * @author Hachimi 
 */
public class GuestRequestDto {
    /**
     * User ID.
     */
    private String id;
    
    /**
     * Username.
     */
    private String username;
    
    /**
     * Email address.
     */
    private String email;
    
    /**
     * Creation date of this request.
     */
    private Long createTimestamp;

    /**
     * Whether this request is deleted.
     */
    private Boolean deletedDateNonnull;

    /**
     * Whether this request is approved or not.
     */
    private Boolean approved;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }
    
    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Boolean getDeletedDateNonnull() {
        return deletedDateNonnull;
    }

    public void setDeletedDateNonnull(Boolean deletedDateNonnull) {
        this.deletedDateNonnull = deletedDateNonnull;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .toString();
    }
}
