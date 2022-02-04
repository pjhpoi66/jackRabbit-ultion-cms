package com.ultion.cms.document.service;

import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.stereotype.Service;

import javax.jcr.GuestCredentials;
import javax.jcr.Repository;
import javax.jcr.Session;

@Service
public class FirstHop {

    public void first() throws Exception {
        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new GuestCredentials());
        try {
            String user = session.getUserID();
            String name = repository.getDescriptor(Repository.REP_NAME_DESC);
            System.out.println("Logged in as " + user + " to a " + name
                    + " repository.");
        } finally {
            session.logout();
        }
    }
}
