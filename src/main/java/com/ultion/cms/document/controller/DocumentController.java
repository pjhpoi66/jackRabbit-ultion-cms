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

    @GetMapping("/three")
    public ModelAndView getDocument() {
        return new ModelAndView("index");
    }

    @PostMapping("/three/upload")
    public ModelAndView fileUpload(@RequestParam("file") File file) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("file", file);
        uploadService.upload(param, "admin", "admin");
        return new ModelAndView("index");
    }



}
