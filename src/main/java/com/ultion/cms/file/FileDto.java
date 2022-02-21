package com.ultion.cms.file;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
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

}
