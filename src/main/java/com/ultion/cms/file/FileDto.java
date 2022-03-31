package com.ultion.cms.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Setter
@Builder
public class FileDto {
    //    private String uuid;
    private int id;
    @JsonProperty("pId")
    private int pId;
    private String path;
    private String name;
    private String owner;
    private String lastUpdate;
    private String type;
    private boolean iconOpen;


}




