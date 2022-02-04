package com.ultion.cms.document.controller;

import com.ultion.cms.document.service.ThirdHop;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Controller
public class DocumentController {

    final
    ThirdHop thirdHop;

    public DocumentController(ThirdHop thirdHop) {
        this.thirdHop = thirdHop;
    }

    @GetMapping("/three")
    public ModelAndView getDocument() {
        return new ModelAndView("index");
    }

    @PostMapping("/three/upload")
    public ModelAndView fileUpload(@RequestParam("file") File file) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("file", file);
        thirdHop.three(param);
        return new ModelAndView("index");
    }

}
