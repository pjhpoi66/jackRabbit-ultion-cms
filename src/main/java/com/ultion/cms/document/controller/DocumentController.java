package com.ultion.cms.document.controller;

import com.ultion.cms.document.service.DocumentService;
import com.ultion.cms.document.service.ThirdHop;
import com.ultion.cms.document.service.UploadTestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DocumentController {

    final ThirdHop thirdHop;
    final DocumentService documentService;
    final UploadTestService uploadTestService;

    public DocumentController(ThirdHop thirdHop, DocumentService documentService, UploadTestService uploadTestService) {
        this.thirdHop = thirdHop;
        this.documentService = documentService;
        this.uploadTestService = uploadTestService;
    }

    @GetMapping("/index")
    public ModelAndView indexPage() throws Exception{
        Map<String, Object> result = documentService.indexPageLoad();
        return new ModelAndView("index", result);
    }

    @PostMapping("/nodeList")
    @ResponseBody
    public Map<String, Object> getNodeList(@RequestBody Map<String, Object> param) throws Exception{
        Map<String, Object> resultMap = documentService.indexPageLoad();
        resultMap.put("fileList", documentService.getFileList(param).get("fileList"));
        return resultMap;
    }

    @GetMapping("/tt")
    public ModelAndView tt() {
        return new ModelAndView("fileUpload");
    }

    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> fileUpload(@RequestParam("nodePath") String nodePath, MultipartHttpServletRequest request) throws Exception {

        boolean resultAdd = documentService.upload(nodePath, request);
        Map<String, Object> result = new HashMap<>();
        result.put("resultAdd", resultAdd);
        return result;
    }

    @PostMapping("/folderAdd")
    @ResponseBody
    public Map<String, Object> folderAdd(@RequestBody Map<String, Object> param) throws Exception {

        boolean resultAdd = documentService.folderNodeAdd(param);
        Map<String, Object> result = new HashMap<>();
        result.put("resultAdd", resultAdd);
        return result;
    }

    @PostMapping("/folderDelete")
    @ResponseBody
    public Map<String, Object> delete(@RequestBody Map<String, Object> param) throws Exception{
        Map<String, Object> result = new HashMap<>();
        boolean delResult = documentService.deleteNode(param);
        result.put("delResult", delResult);
        return result;
    }

    @PostMapping("/download")
    @ResponseBody
    public Map<String, Object> down() throws Exception{
        Map<String, Object> result = new HashMap<>();
        return result;
    }

}
