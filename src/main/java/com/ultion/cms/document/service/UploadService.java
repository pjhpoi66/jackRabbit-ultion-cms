package com.ultion.cms.document.service;

import com.ultion.cms.test.VersioningService;
import lombok.RequiredArgsConstructor;
import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.stereotype.Service;

import javax.jcr.*;
import javax.jcr.version.VersionException;
import java.io.*;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadService {


    private final VersioningService versioningService;
    private final String UPLOAD_PATH = "upload/";

    public String upload(Map<String, Object> param, String userName, String password) throws Exception {


        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials(userName,
                password.toCharArray()));

        File file = (File) param.get("file");

        String fileName = file.getName();
        String path = file.getAbsolutePath();


        final String USER_PATH = UPLOAD_PATH + session.getUserID();

        File userFolder = new File(USER_PATH);

        if (!userFolder.exists()) {
            userFolder.mkdir();
        }


        try (FileInputStream fis = new FileInputStream(path);
             FileOutputStream fos = new FileOutputStream(USER_PATH + "/" + fileName);) {

            Node root = session.getRootNode();
            // 가져오지 않았을경우 XML파일을 가져온다
            int data;
            byte[] buffer = new byte[1024];

            while ((data = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, data);
                fos.flush();
            }


//            if (!root.hasNode(fileName)) {

            Node docNode = root.addNode(session.getUserID(), "nt:file");

            Node contentNode = docNode.addNode("jcr:content", "nt:resource");
            Binary binary = session.getValueFactory().createBinary(fis);
            contentNode.setProperty("jcr:data", binary);
            contentNode.setProperty("jcr:mimeType", URLConnection.guessContentTypeFromName(file.getName()));
            Calendar created = Calendar.getInstance();
            contentNode.setProperty("jcr:lastModified", created);


            System.out.println("content 노드");
            System.out.println(JcrUtils.readFile(docNode));

            System.out.println("doc 노드");
            System.out.println( JcrUtils.readFile(contentNode));


//                versioningService.versioningBasic(root, session);
            session.save();

            System.out.println("done.");
//            }
            //저장소 내용 출력
            dump(root);
        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        } catch (VersionException e) {
            System.out.println("버전오류");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.logout();
        }
        return "success";
    }

    /**
     * 주어진 노드의 내용을 재귀적으로 출력한다
     */
    private static void dump(Node node) throws RepositoryException {
        // 노드경로 출력
        System.out.println("노드 경로: " + node.getPath());
        // Skip the virtual (and large!) jcr:system subtree
        if (node.getName().equals("jcr:system")) {
            return;
        }

        // 속성출력
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            if (property.getDefinition().isMultiple()) {
                // 다중 값 속성, 모든 값 출력
                Value[] values = property.getValues();
                for (Value value : values) {
                    System.out.println("멀티");
                    System.out.println(property.getPath() + " = "
                            + value.getString());
                }
            } else {
                // 단일 값 속성
                System.out.println("싱글");
                System.out.println(property.getPath() + " = "
                        + property.getString());
            }
        }

        // 모든 자식노드를 재귀적으로 출력
        NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            dump(nodes.nextNode());
        }
    }

}
