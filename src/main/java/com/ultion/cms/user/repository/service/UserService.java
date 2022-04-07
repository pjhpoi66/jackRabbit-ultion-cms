package com.ultion.cms.user.repository.service;

import com.ultion.cms.user.entity.User;
import com.ultion.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

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
        } else {
            userRepository.save(User.builder().userId(id).pw(pw).build());
            return "success";
        }
    }

    public void changePw(User user, String pw) {
        User findUser = userRepository.findById(user.getIdx()).get();
        findUser.setPw(pw);
    }

}
