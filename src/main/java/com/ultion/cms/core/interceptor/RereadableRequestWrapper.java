package com.ultion.cms.core.interceptor;

import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class RereadableRequestWrapper extends ContentCachingRequestWrapper {

    private final String body;

    private final String parameter;

    public RereadableRequestWrapper(HttpServletRequest request) throws IOException {

        super(request);

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        // Body 읽어오기
        if (!request.getMethod().equals("GET")) {
            try {
                InputStream inputStream = request.getInputStream();
                if (inputStream != null) {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    char[] charBuffer = new char[128];
                    int bytesRead = -1;
                    while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                        stringBuilder.append(charBuffer, 0, bytesRead);
                    }
                } else {
                    stringBuilder.append("");
                }
            } catch (IOException ex) {
                throw ex;
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ex) {
                        throw ex;
                    }
                }
            }
        }

        // Parameter 읽어오기

        Map<String, String[]> reqMap = request.getParameterMap();
        StringBuffer reqSb = new StringBuffer();
        String key = "";
        String[] values = null;
        Iterator<String> reqIt = reqMap.keySet().iterator();

        reqSb.append("{");

        while (reqIt.hasNext()) {
            key = reqIt.next();
            values = reqMap.get(key);

            StringBuffer valueSb = new StringBuffer();

            if (values != null && values.length > 0) {
                if (values.length == 1) {
                    valueSb.append(values[0]);
                }
            }
            reqSb.append("\"");
            reqSb.append(key);
            reqSb.append("\"");
            reqSb.append(":");
            reqSb.append(valueSb.toString());

            if (reqIt.hasNext()) {
                reqSb.append(",");
            }
        }

        reqSb.append("}");
        String stringBody = stringBuilder.toString();
        stringBody = stringBody.replaceAll("NULL", "null");
        body = stringBody;
        parameter = reqSb.toString();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
        ServletInputStream servletInputStream = new ServletInputStream() {
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }
        };
        return servletInputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public String getBody() {
        return this.body;
    }

    public String getParameter() {
        return this.parameter;
    }
}
