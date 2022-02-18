package com.ultion.cms.document.service;

import com.ultion.cms.core.util.DateUtil;
import com.ultion.cms.jackRabbit.JackrabbitRepositoryConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.data.DataIdentifier;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.jcr.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class DocumentService {

    @org.springframework.beans.factory.annotation.Value("${upload.path}")
    private String uploadPath;
    @org.springframework.beans.factory.annotation.Value("${jcr.rep.home}")
    private String jcrHome;

    private static List<Map<String, Object>> nodeList = new ArrayList<>();

    public Map<String, Object> indexPageLoad() throws Exception {
        Map<String, Object> result = new HashMap<>();

        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        try {
            Node root = session.getRootNode();
            NodeIterator dep1 = root.getNodes();
            List<Map<String, Object>> dep1NodeList = new ArrayList<>();

            while (dep1.hasNext()) {
                Node dep1Node = dep1.nextNode();
                NodeIterator dep2 = dep1Node.getNodes();

                if (!dep1Node.getName().contains(":")) {
                    Map<String, Object> dep1NodeMap = new HashMap<>();

                    if (dep2.getSize() < 1) {
                        dep1NodeMap.put("hasChild", false);
                    } else {
                        dep2 = dep1Node.getNodes();
                        List<Map<String, Object>> dep2NodeList = new ArrayList<>();
                        while (dep2.hasNext()) {
                            Node dep2Node = dep2.nextNode();
                            Map<String, Object> dep2NodeMap = new HashMap<>();
                            dep2NodeMap.put("name", dep2Node.getName());
                            dep2NodeMap.put("nodePath", dep2Node.getPath());
                            dep2NodeMap.put("nodeIndex", dep2Node.getIndex());
                            dep2NodeList.add(dep2NodeMap);

                            NodeIterator s = dep2Node.getNodes();
                            while (s.hasNext()) {
                                System.out.println("루삥뽕");
                                System.out.println(s.nextNode().getName());
                            }
                        }

                        dep1NodeMap.put("children", dep2NodeList);
                        dep1NodeMap.put("hasChild", true);
                    }

                    dep1NodeMap.put("name", dep1Node.getName());
                    dep1NodeMap.put("nodePath", dep1Node.getPath());
                    dep1NodeMap.put("nodeIndex", dep1Node.getIndex());

                    dep1NodeList.add(dep1NodeMap);
                }
            }

            result.put("dep1NodeList", dep1NodeList);

            session.save();
        } finally {
            session.logout();
        }

        return result;
    }

    public boolean customNodeAdd(Map<String, Object> param) throws Exception {
        boolean isSuccess = false;
        String nodeName = param.get("nodeName").toString();
        String nodeType = param.get("nodeType").toString();
        String nodePath = param.get("nodePath").toString();
        nodePath = nodePath.replaceAll("//ROOT/", "");

        System.out.println("노드패스 : " + nodePath);
        JackrabbitRepositoryConfigFactory config = new JackrabbitRepositoryConfigFactory();
        config.create();

        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        nodeType = (nodeType.equals("folder")) ? "nt:folder" : "nt:file";
        try {
            Node root = session.getRootNode();
            if ("nt:folder".equals(nodeType)) {
                root.addNode(nodePath + "/" + nodeName, nodeType);
            }

            isSuccess = true;
            session.save();
        } catch (Exception e) {
            log.debug(e.getMessage());
        } finally {
            session.logout();
        }
        return isSuccess;
    }

    public boolean upload(String nodePath, MultipartHttpServletRequest request) throws Exception {
        boolean resultAdd = false;
        nodePath = nodePath.replaceAll("//ROOT/", "");
        System.out.println("노패: "+nodePath);
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

                //노드 접근
                Repository repository = JcrUtils.getRepository();
                Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

                Node root = session.getRootNode();
                Node targetNode = root.getNode(nodePath);
                Node fileNode = targetNode.addNode(file.getOriginalFilename(), "nt:file");
                Node resNode = fileNode.addNode("jcr:content", "nt:resource");
                resNode.setProperty("jcr:mimeType", "file");
                resNode.setProperty("jcr:encoding", "utf-8");
                resNode.setProperty("jcr:data", file.getInputStream());
                resNode.setProperty("jcr:name", file.getOriginalFilename());
                resNode.setProperty("path", fileNode.getPath());
                resNode.setProperty("nodePath", resNode.getPath());
                resNode.setProperty("index", resNode.getIndex());
                System.out.println("파일노드 인덱스!!" + fileNode.getProperty("index").getString());
                file.transferTo(temp);

                //데이터스토어 레코드추가
                FileInputStream is = new FileInputStream(uploadPath + "\\" + temp);
                DataStore dataStore = new FileDataStore();
                dataStore.init(jcrHome);
                dataStore.addRecord(is);

                session.save();
                session.logout();

                resultAdd = true;
            } catch (IOException e) {
                System.out.println("upload fail:" + file.getName());
                e.printStackTrace();
                log.debug(e.getMessage());
                resultAdd = false;

            }
            System.out.println("upload success:" + uploadPath + "/" + file.getOriginalFilename());
        }

        return resultAdd;
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

    /**
     * 주어진 노드의 내용을 재귀적으로 출력한다
     */
    public static void dump(Node node) throws RepositoryException {

        String nodePath = node.getPath();
        String nodeName = node.getName();
        int nodeDepth = node.getDepth();
        System.out.println("노드 이름: " + nodeName);

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
                        fileMap.put("fileName", property.getValue().getString());
                    } else if ("path".equals(propertyName)) {
                        fileMap.put("path", property.getValue().getString());
                    } else if ("index".equals(propertyName)) {
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

    public Map<String, Object> getNodeList(Map<String, Object> param) throws Exception {
        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        try {
            Node root = session.getRootNode();

            //저장소 내용 출력
            dump(root);
        } finally {
            session.logout();
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("nodeList", nodeList);

        return resultMap;
    }

}
