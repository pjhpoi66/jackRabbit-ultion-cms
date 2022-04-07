package com.ultion.cms.user.repository;

import com.ultion.cms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserId(String id);
}
