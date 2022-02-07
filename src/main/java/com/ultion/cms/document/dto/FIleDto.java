package com.ultion.cms.document.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FIleDto {
    private String type;
    private String name;
}
