package com.ultion.cms.file;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class FileCharsetService {

    public String getFileName(HttpServletRequest request, String fileName) {
        return getFileNm(getBrowser(request), fileName);
    }

    private String getFileNm(String browser, String fileName) {
        String reFileNm = null;
        try {
            if (browser.equals("MSIE") || browser.equals("Trident") || browser.equals("Edge")) {
                reFileNm = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            } else {
                if (browser.equals("Chrome")) {
                    reFileNm = new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO-8859-1");
                } else {
                    reFileNm = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
                }
                if (browser.equals("Safari") || browser.equals("Firefox")) {
                    reFileNm = new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO-8859-1");
                }
            }
        } catch (Exception e) {
        }
        return reFileNm;
    }

    private String getBrowser(HttpServletRequest req) {
        String userAgent = req.getHeader("User-Agent");
        if (userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("Trident") > -1 // IE11
                || userAgent.indexOf("Edge") > -1) {
            return "MSIE";
        } else if (userAgent.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (userAgent.indexOf("Opera") > -1) {
            return "Opera";
        } else if (userAgent.indexOf("Safari") > -1) {
            return "Safari";
        } else if (userAgent.indexOf("Firefox") > -1) {
            return "Firefox";
        } else {
            return null;
        }
    }


}
