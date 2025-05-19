package com.sismics.docs.rest.resource;

import java.util.List;

import com.sismics.docs.core.dao.ChatMessageDao;
import com.sismics.docs.core.dao.dto.ChatMessageDto;
import com.sismics.docs.core.model.jpa.ChatMessage;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.ImageUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

/**
 * ChatMessage REST resource.
 * 
 * @author Hachimi
 */
@Path("/chat_message")
public class ChatMessageResource extends BaseResource {
    /**
     * Add a chat message.
     *
     * @api {put} /chat_message Add a chat message
     * @apiName PutChatMessage
     * @apiGroup ChatMessage
     * @apiParam {String} content Chat message content
     * @apiSuccess {String} id Chat message ID
     * @apiSuccess {String} content Content
     * @apiSuccess {String} creator Username
     * @apiSuccess {String} creator_gravatar Creator Gravatar hash
     * @apiSuccess {Number} create_date Create date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param documentId Document ID
     * @param content Chat message content
     * @return Response
     */
    @PUT
    public Response add(@FormParam("content") String content) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        content = ValidationUtil.validateLength(content, "content", 1, 4000, false);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setUserId(principal.getId());
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        chatMessageDao.create(chatMessage, principal.getId());

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
            .add("id", chatMessage.getId())
            .add("content", chatMessage.getContent())
            .add("creator", principal.getName())
            .add("creator_gravatar", ImageUtil.computeGravatar(principal.getEmail()))
            .add("create_date", chatMessage.getCreateDate().getTime());
        return Response.ok(jsonObjectBuilder.build()).build();
    }

    /**
     * Delete a chat message.
     *
     * @api {delete} /chat_message/:id Delete a chat_message
     * @apiName DeleteChatMessage
     * @apiGroup ChatMessage
     * @apiParam {String} id Chat Message ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Chat message not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param id Chat Message ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the chat message
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        ChatMessage chatMessage = chatMessageDao.getActiveById(id);
        if (chatMessage == null) {
            throw new NotFoundException();
        }

        // Delete the chat message
        chatMessageDao.delete(id, principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Get all chat messages in the public chat room.
     *
     * @api {get} /chat_message/ Get chat messages
     * @apiName GetChatMessages
     * @apiGroup ChatMessage
     * @apiSuccess {Object[]} chat_messages List of chat messages
     * @apiSuccess {String} chat_messages.id Chat Message ID
     * @apiSuccess {String} chat_messages.content Content
     * @apiSuccess {String} chat_messages.creator Username
     * @apiSuccess {String} chat_messages.creator_gravatar Creator Gravatar hash
     * @apiSuccess {Number} chat_messages.create_date Create date (timestamp)
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    public Response get() {
        // Get all chat messages
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        List<ChatMessageDto> chatMessageDtoList = chatMessageDao.getAllActive();
        JsonArrayBuilder chatMessages = Json.createArrayBuilder();
        for (ChatMessageDto chatMessageDto : chatMessageDtoList) {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                    .add("id", chatMessageDto.getId())
                    .add("content", chatMessageDto.getContent())
                    .add("creator", chatMessageDto.getCreatorName())
                    .add("creator_gravatar", ImageUtil.computeGravatar(chatMessageDto.getCreatorEmail()))
                    .add("create_date", chatMessageDto.getCreateTimestamp());
            chatMessages.add(jsonObjectBuilder.build());
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("chat_messages", chatMessages);
        return Response.ok().entity(response.build()).build();
    }
}