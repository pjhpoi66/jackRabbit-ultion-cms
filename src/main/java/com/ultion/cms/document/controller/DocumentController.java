package com.ultion.cms.document.controller;

import com.ultion.cms.document.service.DocumentService;
import com.ultion.cms.document.service.ThirdHop;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Controller
public class DocumentController {

    final ThirdHop thirdHop;
    final DocumentService documentService;

    public DocumentController(ThirdHop thirdHop, DocumentService documentService) {
        this.thirdHop = thirdHop;
        this.documentService = documentService;
    }

    @GetMapping("/index")
    public ModelAndView getDocument() {
        return new ModelAndView("index");
    }

    @PostMapping("/upload")
    public ModelAndView fileUpload(@RequestParam("file") File file) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("file", file);
        thirdHop.upload(param);
        return new ModelAndView("index");
    }

    @GetMapping("/tt")
    public ModelAndView tt() {
        return new ModelAndView("fileUpload");
    }

    @PostMapping("/uploadTest")
    public ModelAndView fileUpload(MultipartHttpServletRequest request) throws Exception {
        thirdHop.uploadTest(request);
        return new ModelAndView("index");
    }

    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> search() throws Exception{
        Map<String, Object> result = documentService.getNodeList();

        return result;
    }

}
