package com.seneca.hotelreservation_system.service;

import com.seneca.hotelreservation_system.model.Admin;
import com.seneca.hotelreservation_system.util.JPAUtil;
import org.mindrot.jbcrypt.BCrypt;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class AuthService {

    public void registerAdmin(String username, String password, String role) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            Admin admin = new Admin(username, hashedPassword, role);
            em.getTransaction().begin();
            em.persist(admin);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Admin authenticate(String username, String password) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Admin> query = em.createQuery(
                    "SELECT a FROM Admin a WHERE a.username = :username", Admin.class);
            query.setParameter("username", username);
            Admin admin = query.getSingleResult();

            if (admin != null && BCrypt.checkpw(password, admin.getPassword())) {
                return admin;
            }
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
        return null;
    }
}