package com.ultion.cms.document.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class SearchService {

    public Stream<File> getFileList(String folderPath) {

        String replacePath = replaceStr(folderPath);

        final File path = new File(folderPath);
        final List<File> list = Arrays.asList(path.listFiles());

        return list.stream();
    }

    private String replaceStr(String str) {
        if (str.equals("/")) str = "";

        return str.replace(".", "")
                .replace("..", "")
                .replace("C:/", "");
    }

}
