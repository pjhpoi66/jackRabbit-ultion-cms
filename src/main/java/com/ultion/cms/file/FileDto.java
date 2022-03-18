package com.ultion.cms.file;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Setter
@Builder
public class FileDto {
    //    private String uuid;
    private int id;
    private int pid;
    private String path;

    private String name;
    private String contentType;


    //    private int parentId;
//    private String status;
    private String owner;
    private String lastUpdate;
    private String type;


}




