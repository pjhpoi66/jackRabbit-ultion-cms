package com.ultion.cms.user.repository.service;

import com.ultion.cms.user.entity.User;
import com.ultion.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;


    public Page<User> searchAllUser(int pageNo, int onePageView, Sort sort) {
        Pageable pageable = PageRequest.of(pageNo - 1, onePageView, sort);
        return userRepository.findAll(pageable);
    }

    public User login(User requestUser) {
        User checkUser = userRepository.findByUserId(requestUser.getUserId());
        if (checkUser != null) {
            if (checkUser.getPw().equals(requestUser.getPw())) {
                return checkUser;
            }
        }
        return null;
    }


    public String register(String id, String pw) {
        if (userRepository.findByUserId(id) != null) {
            return "already";
        }
        userRepository.save(User.builder().userId(id).pw(encoder.encode(pw)).build());
        return "success";
    }

    public String deleteUser(String userId) {
        User findUser = userRepository.findByUserId(userId);

        if (findUser != null) {
            userRepository.delete(findUser);

            return "success";
        }
        return "fail";
    }

    @Transactional
    public User changePw(String userId, String changePw) {
        User findUser = userRepository.findByUserId(userId);
        findUser.setPw(changePw);
        return userRepository.save(findUser);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findByUserId(userId);
    }


}
