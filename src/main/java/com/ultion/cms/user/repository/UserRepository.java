package com.ultion.cms.user.repository;

import com.ultion.cms.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByUserId(String id);


}
