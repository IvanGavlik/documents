package org.jpwh.web.dao;

import org.jpwh.web.model.User;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class UserDAOImpl
    extends GenericDAOImpl<User, Long>
    implements UserDAO {

    @Inject
    public UserDAOImpl(EntityManager em) {
        super(em, User.class);
    }
}