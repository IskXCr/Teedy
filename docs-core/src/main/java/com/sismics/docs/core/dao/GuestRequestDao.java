package com.sismics.docs.core.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.criteria.UserCriteria;
import com.sismics.docs.core.dao.dto.GuestRequestDto;
import com.sismics.docs.core.model.jpa.GuestRequest;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

public class GuestRequestDao {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GuestRequestDao.class);

    /**
     * Creates a new guest user request.
     * 
     * @param request GuestRequest to create
     * @param userId User ID
     * @return User ID
     * @throws Exception e
     */
    public String create(GuestRequest request, String userId) throws Exception {
        // Create the user UUID
        request.setId(UUID.randomUUID().toString());
        
        // Checks for user unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query qUserTable = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        qUserTable.setParameter("username", request.getUsername());
        List<?> lUserTable = qUserTable.getResultList();
        if (lUserTable.size() > 0) {
            throw new Exception("AlreadyExistingUsername");
        }
        
        Query qRequestTable = em.createQuery("select r from GuestRequest r where r.username = :username and r.deleteDate is null");
        qRequestTable.setParameter("username", request.getUsername());
        List<?> lRequestTable = qRequestTable.getResultList();
        if (lRequestTable.size() > 0) {
            throw new Exception("AlreadyExistingUsername");
        }
        
        // Create the request
        request.setCreateDate(new Date());
        request.setPassword(hashPassword(request.getPassword()));
        request.setApproved(false);
        em.persist(request);
        
        // Create audit log
        AuditLogUtil.create(request, AuditLogType.CREATE, "guest");
        
        return request.getId();
    }

    /**
     * Returns the list of all requests.
     * 
     * @return List of requests
     */
    public List<GuestRequestDto> findByCriteria(UserCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder("select r.REQ_ID_C as c0, r.REQ_USERNAME_C as c1, r.REQ_EMAIL_C as c2, r.REQ_CREATEDATE_D as c3, case when r.REQ_DELETEDATE_D is not null then true else false end as c4, r.REQ_APPROVED as c5");
        sb.append(" from T_GUEST_REQUEST r ");
        
        // Add search criterias
        if (criteria.getSearch() != null) {
            criteriaList.add("lower(r.REQ_USERNAME_C) like lower(:search)");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        if (criteria.getUserId() != null) {
            criteriaList.add("r.REQ_ID_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        if (criteria.getUserName() != null) {
            criteriaList.add("r.REQ_USERNAME_C = :userName");
            parameterMap.put("userName", criteria.getUserName());
        }
        
        // criteriaList.add("r.REQ_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();
        
        // Assemble results
        List<GuestRequestDto> guestRequestDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            GuestRequestDto guestRequestDto = new GuestRequestDto();
            guestRequestDto.setId((String) o[i++]);
            guestRequestDto.setUsername((String) o[i++]);
            guestRequestDto.setEmail((String) o[i++]);
            guestRequestDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            guestRequestDto.setDeletedDateNonnull((Boolean) o[i++]);
            guestRequestDto.setApproved((Boolean) o[i++]);
            guestRequestDtoList.add(guestRequestDto);
        }
        return guestRequestDtoList;
    }

    
    /**
     * Approve a request and delete it.
     * 
     * @param guestRequest GuestRequest to approve
     * @param userId User ID
     */
    public void approveRequest(GuestRequest request, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        Query q = em.createQuery("select r from GuestRequest r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", request.getId());

        // Approve & Delete the request
        Date dateNow = new Date();

        GuestRequest guestRequestDb = (GuestRequest) q.getSingleResult();
        guestRequestDb.setApproved(true);
        guestRequestDb.setDeleteDate(dateNow);
        
        // Create audit log for deletion
        AuditLogUtil.create(guestRequestDb, AuditLogType.UPDATE, userId);
        AuditLogUtil.create(guestRequestDb, AuditLogType.DELETE, userId);
    }

    /**
     * Deletes a guest request
     * @param request Guest request
     * @param userId User ID
     */
    public void delete(GuestRequest request, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        Query q = em.createQuery("select r from GuestRequest r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", request.getId());
        GuestRequest guestRequestDb = (GuestRequest) q.getSingleResult();

        // Delete the request
        Date dateNow = new Date();
        guestRequestDb.setDeleteDate(dateNow);

        // Create audit log
        AuditLogUtil.create(guestRequestDb, AuditLogType.DELETE, userId);
    }

    /**
     * Hash the user's password.
     * 
     * @param password Clear password
     * @return Hashed password
     */
    private String hashPassword(String password) {
        int bcryptWork = Constants.DEFAULT_BCRYPT_WORK;
        String envBcryptWork = System.getenv(Constants.BCRYPT_WORK_ENV);
        if (!Strings.isNullOrEmpty(envBcryptWork)) {
            try {
                int envBcryptWorkInt = Integer.parseInt(envBcryptWork);
                if (envBcryptWorkInt >= 4 && envBcryptWorkInt <= 31) {
                    bcryptWork = envBcryptWorkInt;
                } else {
                    log.warn(Constants.BCRYPT_WORK_ENV + " needs to be in range 4...31. Falling back to " + Constants.DEFAULT_BCRYPT_WORK + ".");
                }
            } catch (NumberFormatException e) {
                log.warn(Constants.BCRYPT_WORK_ENV + " needs to be a number in range 4...31. Falling back to " + Constants.DEFAULT_BCRYPT_WORK + ".");
            }
        }
        return BCrypt.withDefaults().hashToString(bcryptWork, password.toCharArray());
    }

    /**
     * Gets a guest request by its ID
     * 
     * @param id GuestRequest ID
     * @return GuestRequest
     */
    public GuestRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(GuestRequest.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Gets a guest request by its username
     * 
     * @param username GuestRequest's username
     * @return GuestRequest
     */
    public GuestRequest getActiveByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from GuestRequest r where r.username = :username and r.deleteDate is null");
            q.setParameter("username", username);
            return (GuestRequest) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns the number of active guest requests.
     *
     * @return Number of active requests
     */
    public long getActiveRequestCount() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery("select count(r.REQ_ID_C) from T_USER r where r.REQ_DELETEDATE_D is null");
        DateTime fromDate = DateTime.now().minusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        DateTime toDate = fromDate.plusMonths(1);
        query.setParameter("fromDate", fromDate.toDate());
        query.setParameter("toDate", toDate.toDate());
        return ((Number) query.getSingleResult()).longValue();
    }
}
