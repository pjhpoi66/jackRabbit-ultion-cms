package com.ultion.cms.document.service;

import com.ultion.cms.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.data.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.jcr.*;
import java.io.*;
import java.util.*;

@Service
@Slf4j
public class ThirdHop {

    @org.springframework.beans.factory.annotation.Value("${upload.path}")
    private String uploadPath;
    @org.springframework.beans.factory.annotation.Value("${jcr.rep.home}")
    private String jcrHome;
    @org.springframework.beans.factory.annotation.Value("${file.path}")
    private String filePath;

    private static List<Map<String, Object>> nodeList = new ArrayList<>();

    public Map<String, Object> indexPageLoad() throws Exception{
        Map<String, Object> result = new HashMap<>();

        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        try {
            Node root = session.getRootNode();

            //저장소 내용 출력
            ThirdHop.dump(root);
        } finally {
            session.logout();
        }

        return result;
    }

    public Map<String, Object> getNodeList() throws Exception {
        Repository repository = JcrUtils.getRepository();

        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        try {
            Node root = session.getRootNode();

            //저장소 내용 출력
            ThirdHop.dump(root);
        } finally {
            session.logout();
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("nodeList", nodeList);

        return resultMap;
    }

    /**
     * 주어진 노드의 내용을 재귀적으로 출력한다
     */
    public static void dump(Node node) throws RepositoryException {

        String nodePath = node.getPath();
        String nodeName = node.getName();
        int nodeDepth = node.getDepth();
        System.out.println("노드 이름: " + nodeName);
       /* System.out.println("노드 경로: " + nodePath);

        System.out.println("노드 Depth: " + nodeDepth);*/
        if (nodeName.equals("jcr:system")) {
            return;
        }

        if ("file".equals(nodeName)) {

            PropertyIterator properties = node.getProperties();

            int a = 0;
            Map<String, Object> fileMap = new HashMap<>();
            while (properties.hasNext()) {
                Property property = properties.nextProperty();

                if (a == 0) {
                    fileMap = new HashMap<>();
                }

                String propertyName = property.getName();
                if (propertyName.equals("fileName") || propertyName.equals("path") || propertyName.equals("index")) {
                    if ("fileName".equals(propertyName)) {
                        System.out.println("111111111111111111");
                        fileMap.put("fileName", property.getValue().getString());
                    } else if ("path".equals(propertyName)) {
                        System.out.println("22222222222222222");
                        fileMap.put("path", property.getValue().getString());
                    } else if ("index".equals(propertyName)) {
                        System.out.println("3333333333333333333");
                        fileMap.put("index", property.getValue().getString());
                    }

                }
                if (fileMap.containsKey("fileName") && fileMap.containsKey("path") && fileMap.containsKey("index")) {
                    a++;
                    nodeList.add(fileMap);
                } else {
                    a = 0;
                }

            }
            System.out.println(nodeList);
        }

        // 모든 자식노드를 재귀적으로 출력
        NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            dump(nodes.nextNode());
        }
    }

    public Map<String, Object> uploadTest2(MultipartHttpServletRequest request) throws Exception {

        List<MultipartFile> files = request.getFiles("file");

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fileExt = FilenameUtils.getExtension(fileName);
            String fileBaseName = FilenameUtils.getBaseName(fileName);

            try {
                //일반 파일 업로드
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                System.out.println("uploading:" + uploadPath + File.separator + file.getOriginalFilename() + ".....");
                File temp = new File(fileBaseName + "_" + DateUtil.getNowDate() + "." + fileExt);
                file.transferTo(temp);

                //데이터스토어 레코드추가
                FileInputStream is = new FileInputStream(uploadPath + "\\" + temp);
                DataStore dataStore = new FileDataStore();
                dataStore.init(jcrHome);
                dataStore.addRecord(is);

                //노드 접근
                Repository repository = JcrUtils.getRepository();
                Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

                Node root = session.getRootNode();
                Node cms = root.addNode("cms", "nt:unstructured");
                cms.setProperty("name", "upload");
                cms.setProperty("message", "upload folder??");

                long uploadNodeSize = cms.getNodes().getSize();
                System.out.println("사이즈:" + uploadNodeSize);

                Node upload = cms.addNode("upload", "nt:unstructured");
                Node fileNode = upload.addNode("file");
                fileNode.setProperty("fileName", fileBaseName + "_" + DateUtil.getNowDate() + "." + fileExt);
                fileNode.setProperty("fileSize", file.getSize());
                fileNode.setProperty("registerDate", DateUtil.getNowDateTime());
                fileNode.setProperty("modifyDate", DateUtil.getNowDateTime());
                fileNode.setProperty("extension", fileExt);
                fileNode.setProperty("path", uploadPath);
                fileNode.setProperty("nodePath", fileNode.getPath());
                fileNode.setProperty("index", fileNode.getIndex());
                fileNode.setProperty("cmsIndex", cms.getIndex());
                System.out.println("파일노드 인덱스!!" + fileNode.getProperty("index").getString());
                session.save();
                dump(root);

                session.logout();

            } catch (IOException e) {
                System.out.println("upload fail:" + file.getName());
                e.printStackTrace();
                log.debug(e.getMessage());

            }
            System.out.println("upload success:" + uploadPath + "/" + file.getOriginalFilename());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("nodeList", nodeList);

        return resultMap;
    }

    //루트 하위노드 삭제
    public boolean deleteNode() {
        boolean isSuccess = false;

        try {
            Repository repository = JcrUtils.getRepository();
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

            Node root = session.getRootNode();
            Node cms = root.getNode("cms");
            root.remove();
            cms.remove();
            session.save();
            session.logout();

        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        return isSuccess;
    }

    public Map<String, Object> download() {
        Map<String, Object> result = new HashMap<>();

        try {
            Repository repository = JcrUtils.getRepository();
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            Node root = session.getRootNode();
            Node cms = root.getNode("cms");

            DataStore ds = new FileDataStore();
            ds.init("jackrabbit_1");

            Iterator<DataIdentifier> iterator = ds.getAllIdentifiers();
            while (iterator.hasNext()) {
                DataIdentifier did = iterator.next();

            }

        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        return result;
    }
}
