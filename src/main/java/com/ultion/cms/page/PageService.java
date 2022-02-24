package com.ultion.cms.page;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class PageService {
    private final int MAX_PAGE_SIZE = 10;
    private final int MAX_VIEW_SIZE = 20;
    private int currentPage;
    private int nextPage;
    private int prevPage;
    private int lastPage;


}
