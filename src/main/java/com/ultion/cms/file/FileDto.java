package com.ultion.cms.file;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Setter
@Builder
public class FileDto {
    private String uuid;
    private String name;
    private String contentType;
    private String path;
    private String index;
    private String status;
    private String owner;
    private String lastUpdate;
    private String type;
    private List<FileDto> dtoList;


}




