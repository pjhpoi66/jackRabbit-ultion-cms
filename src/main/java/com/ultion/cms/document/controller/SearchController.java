package com.ultion.cms.document.controller;

import com.ultion.cms.document.dto.FIleDto;
import com.ultion.cms.document.service.FileDownloadService;
import com.ultion.cms.document.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final FileDownloadService downloadService;


    @GetMapping("/search")
    public ModelAndView getSearch() {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("fileList", searchService.getFileList("upload/admin")
                .map(file -> {
                    return FIleDto.builder().name(file.getName()).type(
                            file.isFile() ? "file" : file.isDirectory() ? "folder" : "unknown"
                    ).build();
                }).collect(Collectors.toList()));


        modelAndView.setViewName("search");


        return modelAndView;

    }

    @GetMapping("/search/{path}")
    public String downLoad(@PathVariable String path) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("search");

        System.out.println("파일다운");
        downloadService.fileDown("upload/admin"+path);

        return "redirect:/search";
    }

}
