package com.ultion.cms.document.service;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.data.DataIdentifier;
import org.apache.jackrabbit.core.data.DataRecord;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.springframework.stereotype.Service;

import javax.jcr.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class ThirdHop {

    @org.springframework.beans.factory.annotation.Value("${jcr.rep.home}")
    private String jcrHome;

    public void three(Map<String, Object> param) throws Exception {
        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin",
                "admin".toCharArray()));
        File file = (File) param.get("file");
        String fileName = file.getName();
        System.out.println("FILE NAME : " + fileName);
        String path = file.getAbsolutePath();
        System.out.println("PATH : " + path);
        FileInputStream xml = new FileInputStream(path);

        try {
            Node root = session.getRootNode();

            // 가져오지 않았을경우 XML파일을 가져온다
            if (!root.hasNode(fileName)) {
                System.out.print("Importing xml... ");
                System.out.print("가져오는중");
                // XML을 가져올 구조화되지 않은 노드를 만듭니다.
                Node node = root.addNode(fileName, "nt:unstructured");
                // 생성되 노드 아래에 파일 가져오기

                session.importXML(node.getPath(), xml,
                        ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);


                File upLoadFolder = new File("upload/");
                File userFolder = new File("upload/" + session.getUserID());
                if (!upLoadFolder.exists()) {
                    upLoadFolder.mkdir();
                    System.out.println("mkupdate");
                }
                if (!userFolder.exists()) {
                    userFolder.mkdir();
                    System.out.println("mkuser");

                }

                FileOutputStream fos = new FileOutputStream("upload/" + session.getUserID() + "/" + fileName);
                xml = new FileInputStream(path);
                int data = 0;
                byte buffer[] = new byte[1024];

                while ((data = xml.read(buffer)) != -1) {
                    fos.write(buffer, 0, data);
                    fos.flush();
                }
                xml = new FileInputStream(path);
                FileDataStore fileDataStore = new FileDataStore();
                fileDataStore.init(jcrHome);
                fileDataStore.addRecord(xml);

                Iterator<DataIdentifier> iterator = fileDataStore.getAllIdentifiers();

                while (iterator.hasNext()) {
                    DataIdentifier dataIdentifier = iterator.next();
                    System.out.println("데스토어 IDS" + dataIdentifier);
                    DataRecord dataRecord = fileDataStore.getRecordIfStored(dataIdentifier);
                    System.out.println("레코드 레퍼런스 : " + dataRecord.getReference());
                }

                fos.close();
                xml.close();

                session.save();
                System.out.println("done.");
            }

            //저장소 내용 출력
            dump(root);
        } finally {
            session.logout();
        }
    }

    /**
     * 주어진 노드의 내용을 재귀적으로 출력한다
     */
    static List<String> fileNames = new ArrayList<>();

    private static void dump(Node node) throws RepositoryException {

        // 노드경로 출력
        System.out.println("노드 경로: " + node.getPath());
        // Skip the virtual (and large!) jcr:system subtree
        System.out.println("노드 이름: " + node.getName());
        if (1 == node.getDepth()) {
            fileNames.add(node.getName());
        }
        System.out.println("리스트" + fileNames);

        System.out.println("노드 Depth: " + node.getDepth());
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
