package com.ultion.cms.test;

import lombok.RequiredArgsConstructor;
import org.apache.jackrabbit.spi.commons.query.sql.Node;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@RequiredArgsConstructor
@Controller
public class TestController {

    private final VersioningService versioningService;

    public void test() {
    }

}
