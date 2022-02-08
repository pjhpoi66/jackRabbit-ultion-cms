package com.ultion.cms.document.controller;

import com.ultion.cms.document.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final UploadService uploadService;

    @GetMapping("/")
    public ModelAndView getDocument() {
        return new ModelAndView("index");
    }

    @GetMapping("/upload")
    public String urlLocated() {
        return "redirect:/";
    }


    @PostMapping("/upload")
    public ModelAndView fileUpload(@RequestParam("file") File file) throws Exception {
        ModelAndView modelAndView = new ModelAndView("upload");

        Map<String, Object> param = new HashMap<>();
        param.put("file", file);

        if (file == null) {
            return new ModelAndView("index");
        }
        String success = (uploadService.upload(param, "admin", "admin"));
        modelAndView.addObject("success", success);
        return modelAndView;
    }


}
