package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;

import org.junit.Assert;
import org.junit.Test;

/**
 * Exhaustive test of the chat message resource.
 * 
 * @author Hachimi
 */
public class TestChatMessageResource extends BaseJerseyTest{
    
    /**
     * Test the chat message resource.
     */
    @Test
    public void testChatMessageResource() {
        // Login chat1
        clientUtil.createUser("chat1");
        String chat1Token = clientUtil.login("chat1");

        // Read chat messages with chat1
        JsonObject json = target().path("/chat_message/").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, chat1Token)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("chat_messages").size());

        // Create a chat message with chat1
        json = target().path("/chat_message").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, chat1Token)
                .put(Entity.form(new Form()
                        .param("content", "Hachimi o namerudo")), JsonObject.class);
        String chatMessage1Id = json.getString("id");
        Assert.assertNotNull(chatMessage1Id);
        Assert.assertEquals("Hachimi o namerudo", json.getString("content"));
        Assert.assertEquals("chat1", json.getString("creator"));
        Assert.assertNotNull(json.getJsonNumber("create_date"));

        // Read chat messages with chat1
        json = target().path("/chat_message/").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, chat1Token)
                .get(JsonObject.class);
        Assert.assertEquals(1, json.getJsonArray("chat_messages").size());
        Assert.assertEquals(chatMessage1Id, json.getJsonArray("chat_messages").getJsonObject(0).getString("id"));

        // Delete a chat message
        json = target().path("/chat_message/" + chatMessage1Id).request()
            .cookie(TokenBasedSecurityFilter.COOKIE_NAME, chat1Token)
            .delete(JsonObject.class);
        
        // Read chat messages with chat1
        json = target().path("/chat_message/").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, chat1Token)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("chat_messages").size());
    }
}
