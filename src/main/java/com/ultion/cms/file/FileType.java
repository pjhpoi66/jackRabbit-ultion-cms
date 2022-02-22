package com.ultion.cms.file;

import lombok.Getter;

@Getter
public enum FileType {

    FILE("file") ,
    FOLDER("folder");

    final private String value;

    FileType(String value) {
        this.value = value;
    }

}
