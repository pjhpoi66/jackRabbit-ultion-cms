package com.ultion.cms.document.service;

import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.stereotype.Service;

import javax.jcr.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

@Service
public class UploadService {

    private final String UPLOAD_PATH = "upload/";

    public void upload(Map<String, Object> param, String userName, String password) throws Exception {


        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials(userName,
                password.toCharArray()));


        File file = (File) param.get("file");
        String fileName = file.getName();
        String path = file.getAbsolutePath();
        FileInputStream fis = new FileInputStream(path);

        final String USER_PATH = UPLOAD_PATH + session.getUserID();

        File upLoadFolder = new File(UPLOAD_PATH);
        File userFolder = new File(USER_PATH);

        if (!upLoadFolder.exists()) {
            upLoadFolder.mkdir();
        } else if (!userFolder.exists()) {
            userFolder.mkdir();
        }

        FileOutputStream fos = new FileOutputStream(USER_PATH + "/" + fileName);
        fis = new FileInputStream(path);
        int data;
        byte[] buffer = new byte[1024];

        while ((data = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, data);
            fos.flush();
        }


        try {
            Node root = session.getRootNode();
            // 가져오지 않았을경우 XML파일을 가져온다
            if (!root.hasNode(fileName)) {
                // XML 을 가져올 구조화되지 않은 노드를 만듭니다.
                Node node = root.addNode(fileName, "nt:unstructured");
                // 생성되 노드 아래에 파일 가져오기

                session.importXML(node.getPath(), fis,
                        ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

                session.save();
                System.out.println("done.");
            }

            //저장소 내용 출력
            dump(root);
        } finally {
            session.logout();
            fos.close();
            fis.close();
        }
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
