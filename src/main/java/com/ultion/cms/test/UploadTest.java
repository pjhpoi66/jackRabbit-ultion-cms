package com.ultion.cms.test;

import lombok.NoArgsConstructor;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.fs.local.LocalFileSystem;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
@NoArgsConstructor
public class UploadTest {

    private final LocalFileSystem fileSystem = new LocalFileSystem();

    public String upload(String uploadPath ,  File file) throws FileSystemException {

//        fileSystem.setRoot(file);

        System.out.println(file.getName());

        String resultPath = System.getProperty("user.dir") + uploadPath;

        File userFolder = new File(resultPath);

        if (!userFolder.exists()) {
            userFolder.mkdir();
        }

        try (InputStream is = fileSystem.getInputStream(file.getAbsolutePath());
             OutputStream os = fileSystem.getOutputStream(resultPath + file.getName());) {
            int data;
            byte[] buffer = new byte[1024];
            while ((data = is.read(buffer)) != -1) {
                os.write(buffer, 0, data);
                os.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        }finally {
            fileSystem.close();
        }

        return "success";

    }


}
