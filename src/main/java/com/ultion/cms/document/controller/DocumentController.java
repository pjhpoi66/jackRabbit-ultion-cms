package com.ultion.cms.document.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ultion.cms.document.service.DocumentService;
import com.ultion.cms.file.FileDto;
import com.ultion.cms.file.FileListDto;
import com.ultion.cms.test.TestDto;
import com.ultion.cms.test.TestListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
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
        resultMap.put("fileMap", documentService.getNodeList(param, session));
        return new ModelAndView("index-content", resultMap);
    }

    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> fileUpload(@RequestParam("nodePath") String nodePath, MultipartHttpServletRequest request) throws Exception {
        boolean resultAdd = documentService.upload(nodePath, request, session);
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
    public void down2(HttpServletRequest request, HttpServletResponse response,  FileDto fileDto) throws Exception {
        List<FileDto> fileDtos = new ArrayList<>();
        fileDtos.add(fileDto);
        documentService.downLoad(request, response, session, fileDtos);
    }

    /**
     * 멀티 다운로드 입니다
     */
    @PostMapping("/download2")
    @ResponseBody
    public void down(HttpServletRequest request, HttpServletResponse response,  @RequestBody List<FileDto>  fileDtos ) throws Exception {
        documentService.downLoad(request, response, session, fileDtos);
    }
    @PostMapping("/download3")
    @ResponseBody
    public void test(HttpServletRequest request, HttpServletResponse response, @RequestBody List<FileDto> fileDtos ) throws Exception {

        System.out.println(fileDtos);
        System.out.println("브레이크 포인트");
//        documentService.downLoad(request, response, session, dtos.getDtos());
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
