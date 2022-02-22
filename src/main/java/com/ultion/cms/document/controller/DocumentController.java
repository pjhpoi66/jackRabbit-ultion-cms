package com.ultion.cms.document.controller;

import com.ultion.cms.document.service.DocumentService;
import com.ultion.cms.document.service.ThirdHop;
import com.ultion.cms.document.service.UploadTestService;
import com.ultion.cms.file.FileDto;
import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.util.HashMap;
import java.util.List;
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
        resultMap.put("fileList", documentService.getNodeList(param).get("fileList"));
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
    public Map<String, String> down(@RequestBody List<FileDto>  fileDtos ) throws Exception{
        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        Map<String, String> resultMap = new HashMap<>();
     /*   List<FileDto> fileDtos = new ArrayList<>();
        mapList.forEach(map -> {
            fileDtos.add(FileDto.builder()
                    .name(map.get("nodeName"))
                    .path(map.get("nodePath"))
                    .build()
            );
        });*/
        resultMap.put("result", documentService.downLoad(session, fileDtos));
        session.logout();
        return resultMap;
    }



}
