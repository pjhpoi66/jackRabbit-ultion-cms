package com.ultion.cms.document.controller;

import com.ultion.cms.document.service.DocumentService;
import com.ultion.cms.document.service.ThirdHop;
import com.ultion.cms.document.service.UploadTestService;
import com.ultion.cms.file.FileDto;
import lombok.RequiredArgsConstructor;
import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final ThirdHop thirdHop;
    private final DocumentService documentService;
    private final Repository repository;
    private final Session session;
    private final Node root;

    @GetMapping("/index")
    public ModelAndView indexPage() throws Exception {
        Map<String, Object> result = documentService.indexPageLoad(session);
        return new ModelAndView("index", result);
    }

    @PostMapping("/nodeList")
    @ResponseBody
    public ModelAndView getNodeList(@RequestBody Map<String, Object> param) throws Exception {
        Map<String, Object> resultMap = documentService.indexPageLoad(session);
        resultMap.put("fileMap", documentService.getNodeList(param,session));
        return new ModelAndView("index-content", resultMap);
    }

    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> fileUpload(@RequestParam("nodePath") String nodePath, MultipartHttpServletRequest request) throws Exception {

        System.out.println("브레이크 포인트");

        boolean resultAdd = documentService.upload(nodePath, request,session);
        Map<String, Object> result = new HashMap<>();
        result.put("resultAdd", resultAdd);
        return result;
    }

    @PostMapping("/folderAdd")
    @ResponseBody
    public Map<String, Object> folderAdd(@RequestBody Map<String, Object> param) throws Exception {
        boolean resultAdd = documentService.folderNodeAdd(param, session);
        Map<String, Object> result = new HashMap<>();
        result.put("resultAdd", resultAdd);
        return result;
    }

    @PostMapping("/folderDelete")
    @ResponseBody
    public Map<String, Object> deleteNode(@RequestBody Map<String, Object> param) throws Exception {
        Map<String, Object> result = new HashMap<>();
        boolean delResult = documentService.deleteNode(param, session);
        result.put("delResult", delResult);
        return result;
    }

    /**
     * 싱글 다운로드
     */
    @PostMapping("/download")
    @ResponseBody
    public Map<String, String> down2(@RequestBody FileDto fileDto) throws Exception {
        List<FileDto> fileDtos = new ArrayList<>();
        fileDtos.add(fileDto);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("result", documentService.downLoad(session, fileDtos));
        return resultMap;
    }

    /**
     * 멀티 다운로드 입니다
     */
    @PostMapping("/download2")
    @ResponseBody
    public Map<String, String> down(@RequestBody List<FileDto> fileDtos) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("result", documentService.downLoad(session, fileDtos));
        return resultMap;
    }

    @PostMapping("/reName")
    @ResponseBody
    public void reNaming(@RequestBody Map<String, String> map) throws RepositoryException {
        String path = map.get("path").substring(6);
        Node reNamingNode = documentService.findFileNode(root, FileDto.builder().path(path).build());
        documentService.reNamingFile(reNamingNode, map.get("reName"));
//        return "success";
    }


}
