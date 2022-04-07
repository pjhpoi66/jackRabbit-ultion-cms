package com.ultion.cms.user;

import com.ultion.cms.user.entity.User;
import com.ultion.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockUser {
    private final UserRepository repository;
//    @Bean
//    public void insertMockUsers() {
//        for (int i = 0; i < 100; i++) {
//            repository.save(User.builder().userId("mock" + i).pw("pass" + i).build());
//        }
//    }
}
