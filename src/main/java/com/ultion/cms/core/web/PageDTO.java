package com.ultion.cms.core.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class PageDTO<T> implements Serializable {
    private static final long serialVersionUID = -6618206850210257861L;

    private List<T> list;

    @JsonIgnore
    private Pageable page;

    @JsonIgnore
    private int totalCount;
}
