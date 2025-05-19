package com.sismics.docs.core.model.jpa;

import java.util.Date;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ChatMessage entity.
 * 
 * @author Hachimi
 */
@Entity
@Table(name = "T_CHAT_MESSAGE")
public class ChatMessage implements Loggable {
    /**
     * ChatMessage ID.
     */
    @Id
    @Column(name = "MSG_ID_C", length = 36)
    private String id;

    /**
     * User ID.
     */
    @Column(name = "MSG_IDUSER_C", length = 36, nullable = false)
    private String userId;

    /**
     * Content.
     */
    @Column(name = "MSG_CONTENT_C", nullable = false)
    private String content;

    /**
     * Creation date.
     */
    @Column(name = "MSG_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "MSG_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .toString();
    }

    @Override
    public String toMessage() {
        return id;
    }
}
