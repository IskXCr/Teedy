package com.sismics.docs.rest.resource;

import java.util.List;

import com.sismics.docs.core.dao.dto.GuestRequestDto;
import com.sismics.docs.core.model.jpa.GuestRequest;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.GuestRequestDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.UserCriteria;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * GuestRequest REST resources.
 * 
 * @author Hachimi
 */
@Path("guest_request")
public class GuestRequestResource extends BaseResource {
    /**
     * Creates a new guest request.
     *
     * @api {put} /guest_request Create a new guest request
     * @apiName PutGuestRequest
     * @apiGroup GuestRequest
     * @apiParam {String{3..50}} username Username
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) AlreadyExistingUsername Login already used
     * @apiError (server) UnknownError Unknown server error
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param username User's username
     * @param password Password
     * @param email E-Mail
     * @return Response
     */
    @PUT
    public Response register(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("email") String email) {
        
        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        ValidationUtil.validateEmail(email, "email");

        // Create the guest request
        GuestRequest guestRequest = new GuestRequest();
        guestRequest.setUsername(username);
        guestRequest.setPassword(password);
        guestRequest.setEmail(email);

        // Create the request
        GuestRequestDao guestRequestDao = new GuestRequestDao();
        try {
            guestRequestDao.create(guestRequest, null);
        } catch (Exception e) {
            if (e.getMessage().equals("AlreadyExistingUsername")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Json.createObjectBuilder()
                                .add("error", "AlreadyExistingUsername")
                                .build())
                        .build();
            }
            throw new RuntimeException(e);
        }

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Invalidates a guest request.
     *
     * @api {delete} /guest_request/:username Delete a guest request
     * @apiDescription All associated entities will be deleted as well.
     * @apiName DeleteGuestRequestUsername
     * @apiGroup GuestRequest
     * @apiParam {String} id ID
     * @apiSuccess {String} username Username
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied or the user cannot be deleted
     * @apiError (client) RequestNotFound The user does not exist
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param id ID
     * @return Response
     */
    
    @DELETE
    @Path("{id: [a-zA-Z0-9_@.-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Check that the user exists
        GuestRequestDao guestRequestDao = new GuestRequestDao();
        GuestRequest guestRequest = guestRequestDao.getById(id);
        if (guestRequest == null) {
            throw new ClientException("RequestNotFound", "The request does not exist");
        }
        
        // Delete the request
        guestRequestDao.delete(guestRequest, principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns the information about a guest request.
     *
     * @api {get} /guest_request/:username Get a guest request
     * @apiName GetGuestRequestUsername
     * @apiGroup User
     * @apiParam {String} id ID
     * @apiSuccess {String} id ID
     * @apiSuccess {String} username Username
     * @apiSuccess {String} email E-mail
     * @apiSuccess {String} deleted True if the request has been marked as deleted
     * @apiSuccess {String} approved True if the request has been marked as approved
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) RequestNotFound The request does not exist
     * @apiError (client) RequestAlreadyProcessed The request has already been processed
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param id id
     * @return Response
     */
    @GET
    @Path("{id: [a-zA-Z0-9_@.-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        GuestRequestDao guestRequestDao = new GuestRequestDao();
        GuestRequest guestRequest = guestRequestDao.getById(id);
        if (guestRequest == null) {
            throw new ClientException("RequestNotFound", "The request does not exist");
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
            .add("id", guestRequest.getId())
            .add("username", guestRequest.getUsername())
            .add("email", guestRequest.getEmail())
            .add("deleted", guestRequest.getDeleteDate() != null)
            .add("approved", guestRequest.getApproved());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Judge a request by making it approved or rejected (decisive)
     * 
     * @param id id Guest request's ID
     * @param approved approved True if the request is to be approved, false otherwise
     * @return Response
     */
    @POST
    @Path("{id: [a-zA-Z0-9_@.-]+}")
    public Response judge(
        @PathParam("id") String id,
        @FormParam("approved") Boolean approved) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Check if the request exists
        GuestRequestDao guestRequestDao = new GuestRequestDao();
        GuestRequest guestRequest = guestRequestDao.getById(id);
        if (guestRequest == null) {
            throw new ClientException("RequestNotFound", "The request does not exist");
        }
        // If it has been processed, then exit
        if (guestRequest.getDeleteDate() != null) {
            throw new ClientException("RequestAlreadyProcessed", "The request has already been processed");
        }
        
        // Update the status of the request
        guestRequest.setApproved(approved);
        if (!guestRequest.getApproved()) {
            guestRequestDao.delete(guestRequest, principal.getId());
            // Always return OK
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("status", "ok");
            return Response.ok().entity(response.build()).build();
        }

        // We will now try to register the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(guestRequest.getUsername());
        user.setPassword(guestRequest.getPassword()); // Use hashed password temporarily
        user.setEmail(guestRequest.getEmail());
        user.setStorageQuota(0L);
        user.setOnboarding(false);

        // Create the user
        UserDao userDao = new UserDao();
        try {
            userDao.create(user, principal.getId());
            user.setPassword(guestRequest.getPassword()); // Set the true hashed password to be updated
            userDao.updateHashedPassword(user);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ClientException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown server error", e);
            }
        }
        
        // Approve the request
        guestRequestDao.approveRequest(guestRequest, principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns all active guest requests.
     *
     * @api {get} /guest_request/list Get guest requests
     * @apiName GetGuestRequestList
     * @apiGroup GuestRequest
     * @apiParam {Number} sort_column Column index to sort on
     * @apiParam {Boolean} asc If true, sort in ascending order
     * @apiParam {String} group Filter on this group
     * @apiSuccess {Object[]} guest_requests List of guest requests
     * @apiSuccess {String} guest_requests.id ID
     * @apiSuccess {String} guest_requests.username Username
     * @apiSuccess {String} guest_requests.email E-mail
     * @apiSuccess {Number} guest_requests.create_date Create date (timestamp)
     * @apiSuccess {String} guest_requests.deleted True if the request has been marked as deleted
     * @apiSuccess {String} guest_requests.approved True if the request has been marked as approved
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission guest_request
     * @apiVersion 1.5.0
     *
     * @param sortColumn Sort index
     * @param asc If true, ascending sorting, else descending
     * @return Response
     */
    @GET
    @Path("list")
    public Response list(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        JsonArrayBuilder guest_requests = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        GuestRequestDao guestRequestDao = new GuestRequestDao();
        List<GuestRequestDto> guestRequestDtoList = guestRequestDao.findByCriteria(new UserCriteria(), sortCriteria);
        for (GuestRequestDto guestRequestDto : guestRequestDtoList) {
            JsonObjectBuilder guest_request = Json.createObjectBuilder()
                    .add("id", guestRequestDto.getId())
                    .add("username", guestRequestDto.getUsername())
                    .add("email", guestRequestDto.getEmail())
                    .add("create_date", guestRequestDto.getCreateTimestamp())
                    .add("deleted", guestRequestDto.getDeletedDateNonnull())
                    .add("approved", guestRequestDto.getApproved());
            guest_requests.add(guest_request);
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("guest_requests", guest_requests);
        return Response.ok().entity(response.build()).build();
    }
}
