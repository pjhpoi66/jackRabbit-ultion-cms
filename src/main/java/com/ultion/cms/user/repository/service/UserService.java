package com.ultion.cms.user.repository.service;

import com.ultion.cms.user.entity.UserEntity;
import com.ultion.cms.user.repository.UserRepository;
import com.ultion.cms.user.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;


    public Page<UserEntity> searchAllUser(int pageNo, int onePageView, Sort sort) {
        Pageable pageable = PageRequest.of(pageNo - 1, onePageView, sort);
        return userRepository.findAll(pageable);
    }

    public UserEntity login(UserEntity requestUserEntity) {
        UserEntity checkUserEntity = userRepository.findByUserId(requestUserEntity.getUserId());
        if (checkUserEntity != null) {
            if (checkUserEntity.getPw().equals(requestUserEntity.getPw())) {
                return checkUserEntity;
            }
        }
        return null;
    }


    public String register(String id, String pw) {
        if (userRepository.findByUserId(id) != null) {
            return "already";
        }
        userRepository.save(UserEntity.builder().userId(id).pw(encoder.encode(pw)).build());
        return "success";
    }

    public String deleteUser(String userId) {
        UserEntity findUserEntity = userRepository.findByUserId(userId);

        if (findUserEntity != null) {
            userRepository.delete(findUserEntity);

            return "success";
        }
        return "fail";
    }

    @Transactional
    public UserEntity changePw(String userId, String changePw) {
        UserEntity findUserEntity = userRepository.findByUserId(userId);
        findUserEntity.setPw(changePw);
        return userRepository.save(findUserEntity);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (userId.contains("ad")) {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
            authorities.add(new SimpleGrantedAuthority(Role.MEMBER.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(Role.MEMBER.getValue()));
        }
        UserEntity findUser = userRepository.findByUserId(userId);
        return new User(findUser.getUserId(), findUser.getPw(), authorities);
    }


}
