package com.sismics.docs.core.dao;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.dto.ChatMessageDto;
import com.sismics.docs.core.model.jpa.ChatMessage;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

/**
 * ChatMessage DAO.
 * 
 * @author Hachimi
 */
public class ChatMessageDao {
    /**
     * Creates a new chat message.
     * 
     * @param message Chat Message
     * @param userId  User ID
     * @return New ID
     */
    public String create(ChatMessage message, String userId) {
        // Create the UUID
        message.setId(UUID.randomUUID().toString());

        // Create the message
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        message.setCreateDate(new Date());
        em.persist(message);

        // Create audit log
        AuditLogUtil.create(message, AuditLogType.CREATE, userId);

        return message.getId();
    }

    /**
     * Deletes a ChatMessage.
     * 
     * @param id     ChatMessage ID
     * @param userId User ID
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the message
        Query q = em.createQuery("select m from ChatMessage m where m.id = :id and m.deleteDate is null");
        q.setParameter("id", id);
        ChatMessage messageDb = (ChatMessage) q.getSingleResult();

        // Delete the message
        Date dateNow = new Date();
        messageDb.setDeleteDate(dateNow);

        // Create audit log
        AuditLogUtil.create(messageDb, AuditLogType.DELETE, userId);
    }

    /**
     * Gets an active message by its ID.
     * 
     * @param id ChatMessage ID
     * @return ChatMessage
     */
    public ChatMessage getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select m from ChatMessage m where m.id = :id and m.deleteDate is null");
            q.setParameter("id", id);
            return (ChatMessage) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get all active messages.
     * 
     * @return List of messages
     */
    public List<ChatMessageDto> getAllActive() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select m.MSG_ID_C, m.MSG_CONTENT_C, m.MSG_CREATEDATE_D, u.USE_USERNAME_C, u.USE_EMAIL_C from T_CHAT_MESSAGE m, T_USER u");
        sb.append(" where m.MSG_IDUSER_C = u.USE_ID_C and m.MSG_DELETEDATE_D is null ");
        sb.append(" order by m.MSG_CREATEDATE_D asc ");
        Query q = em.createNativeQuery(sb.toString());

        @SuppressWarnings("unchecked")
        List<Object[]> l = q.getResultList();
        
        return l.stream().map(
                (o) -> {
                    int i = 0;
                    ChatMessageDto messageDto = new ChatMessageDto();
                    messageDto.setId((String) o[i++]);
                    messageDto.setContent((String) o[i++]);
                    messageDto.setCreateTimestamp(((Date) o[i++]).getTime());
                    messageDto.setCreatorName((String) o[i++]);
                    messageDto.setCreatorEmail((String) o[i++]);
                    return messageDto;
                }).toList();
    }
}
