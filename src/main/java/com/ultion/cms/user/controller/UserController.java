package com.ultion.cms.user.controller;

import com.ultion.cms.user.entity.User;
import com.ultion.cms.user.repository.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @GetMapping("/user")
    public ModelAndView userIndex(@RequestParam(required = false) Integer pageNo,
                                  @RequestParam(required = false) Integer onePageView,
                                  @RequestParam(required = false) Sort sort) {
        if (pageNo == null) {
            pageNo = 1;
        }
        if (onePageView == null) {
            onePageView = 10;
        }
        if (sort == null) {
            sort = Sort.by(Sort.Direction.DESC, "idx");
        }
        Map<String, Object> map = new HashMap<>();
        userService.searchAllUser(pageNo, onePageView, sort).getTotalPages();
        userService.searchAllUser(pageNo, onePageView, sort).hasPrevious();
        userService.searchAllUser(pageNo, onePageView, sort).nextPageable();
        userService.searchAllUser(pageNo, onePageView, sort).nextPageable();
        map.put("userList", userService.searchAllUser(pageNo, onePageView, sort));
        return new ModelAndView("/user", map);
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public User LoginAction(@RequestBody User user) {
        return userService.login(user);
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public String registerAction(@RequestBody User user) {
        String success = userService.register(user.getUserId(), user.getPw());
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return success;
    }

    @PostMapping("/userDelete")
    @ResponseBody
    public Map<String, Object> deleteUser(@RequestBody String userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("result", userService.deleteUser(userId));
        return result;
    }

    @PostMapping("/changeUserInfo")
    @ResponseBody
    public Map<String, Object> changeUserInfo(@RequestBody Map<String,Object> param) {
        Map<String, Object> result = new HashMap<>();
        User resultUser = userService.changePw((String) param.get("userId"), (String) param.get("changePw"));
        if(resultUser == null) {
            result.put("result", "fail");
        } else {
            result.put("result", "success");
        }
        return result;
    }

}
