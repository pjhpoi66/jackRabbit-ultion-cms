package com.ultion.cms.document.controller;

import com.ultion.cms.document.dto.FIleDto;
import com.ultion.cms.document.service.FileDownloadService;
import com.ultion.cms.document.service.SearchService;
import com.ultion.cms.test.VersioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final FileDownloadService downloadService;
    private final VersioningService versioningService;

    @GetMapping("/search")
    public ModelAndView getSearch() {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("fileList", searchService.getFileList("upload/admin")
                .map(file -> {
                    return FIleDto.builder().name(file.getName()).type(
                            file.isFile() ? "file" : file.isDirectory() ? "folder" : "unknown"
                    ).path(file.getAbsolutePath()).build();
                }).collect(Collectors.toList()));


        modelAndView.setViewName("search");


        return modelAndView;

    }

    @PostMapping("/download")
    public void download(HttpServletResponse response ,String path) throws Exception {

        System.out.println(path);

        downloadService.fileDown(response, path);
    }




}
