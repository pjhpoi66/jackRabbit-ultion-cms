package com.ultion.cms;

import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jcr.*;

@Configuration
public class MockSession {

    @Bean
    public Repository repository() throws RepositoryException {
        return JcrUtils.getRepository();
    }

    @Bean
    public Session session(Repository repository) throws RepositoryException {
        return repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
    }

    @Bean
    public Node node(Session session) throws RepositoryException {
        return session.getRootNode();
    }

}
