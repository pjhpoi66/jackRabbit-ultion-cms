package com.ultion.cms.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileDto {

    private String uuid;
    private String fileName;
    private String path;
    private String contentType;


}
