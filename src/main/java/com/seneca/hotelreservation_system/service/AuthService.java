package com.seneca.hotelreservation_system.service;

import com.seneca.hotelreservation_system.model.Admin;
import com.seneca.hotelreservation_system.util.JPAUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class AuthService {

    public void registerAdmin(String username, String plainTextPassword, String role) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());

        Admin admin = new Admin(username, hashedPassword, role);

        try {
            em.getTransaction().begin();
            em.persist(admin);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Admin authenticate(String username, String plainTextPassword) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            TypedQuery<Admin> query = em.createQuery(
                    "SELECT a FROM Admin a WHERE a.username = :username", Admin.class);
            query.setParameter("username", username);

            Admin admin = query.getSingleResult();

            if (BCrypt.checkpw(plainTextPassword, admin.getPassword())) {
                return admin;
            }
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
        return null;
    }
}