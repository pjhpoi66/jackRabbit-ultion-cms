package com.ultion.cms.document.service;

import com.ultion.cms.core.util.DateUtil;
import com.ultion.cms.jackRabbit.JackrabbitRepositoryConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.data.DataIdentifier;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.apache.jackrabbit.core.nodetype.NodeTypeDefinitionImpl;
import org.apache.jackrabbit.spi.commons.nodetype.NodeDefinitionImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.jcr.*;
import javax.jcr.nodetype.NodeDefinition;
import java.io.*;
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

                            NodeIterator dep3 = dep2Node.getNodes();

                            if (dep3.getSize() < 1)
                                dep2NodeMap.put("hasChild", false);

                            List<Map<String, Object>> fileList = new ArrayList<>();
                            while (dep3.hasNext()){
                                Map<String, Object> fileMap = new HashMap<>();
                                Node dep3Node = dep3.nextNode();
                                fileMap.put("path", dep3Node.getPath());
                                fileList.add(fileMap);
                            }
                            dep2NodeMap.put("fileList", fileList);

                            if(dep2Node.isNodeType("nt:folder"))
                                dep2NodeList.add(dep2NodeMap);

                        }

                        dep1NodeMap.put("children", dep2NodeList);
                        dep1NodeMap.put("hasChild", true);
                    }

                    dep1NodeMap.put("name", dep1Node.getName());
                    dep1NodeMap.put("nodePath", dep1Node.getPath());
                    dep1NodeMap.put("nodeIndex", dep1Node.getIndex());

                    if(dep1Node.isNodeType("nt:folder"))
                        dep1NodeList.add(dep1NodeMap);

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

    public String downLoad(Session session, String filePath) throws RepositoryException {

        Node root = session.getRootNode();

        /*int cuttingInt =  fileDto.getPath().lastIndexOf(fileDto.getFileName());
        System.out.println("다운로드 노드 폴더패스:" + fileDto.getPath().substring(0,cuttingInt).substring(1));
        Node upload = root.getNode(fileDto.getPath().substring(0,cuttingInt).substring(1));



        Node downLoadNode = upload.getNode( fileDto.getFileName());*/

        String[] paths = filePath.substring(1).split("/");

        Node uploadNode = root.getNode(paths[0]);
        for (int i = 1; i < paths.length-1; i++) {
            uploadNode = uploadNode.getNode(paths[i]);
        }
        Node fileNode = uploadNode.getNode(paths[paths.length - 1]);

        File Folder = new File(filePath);

    /*    if (!Folder.exists()) {
            try{
                Folder.mkdirs(); //폴더 생성합니다.
            }
            catch(Exception e){
                e.getStackTrace();
            }
        }*/


        try (OutputStream os = new FileOutputStream("C:\\Users\\user\\Downloads\\" + paths[paths.length - 1]);
             InputStream is = JcrUtils.readFile(fileNode);
        ) {

            IOUtils.copy(is, os);

            return "success";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "noSuchFile";
        } catch (IOException e) {
            e.printStackTrace();
            return "amola";
        }

    }

    public Map<String, Object> getFileList(Map<String, Object> param) throws Exception {
        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        Map<String, Object> resultMap = new HashMap<>();

        int depth = (int) param.get("depth");
        if(depth > 2){
            String targetString = param.get("target").toString();
            String [] targetArr = targetString.split("/");
            System.out.println(targetArr.length);
            System.out.println(targetString);
            List<Map<String, Object>> fileList = new ArrayList<>();
            try {
                Node root = session.getRootNode();
                Node targetNode = root.getNode(targetArr[0]);
                Node targetNode2 = targetNode.getNode(targetArr[1]);

                NodeIterator fileNodes = targetNode2.getNodes();

                while (fileNodes.hasNext()) {

                    Node fileNode = fileNodes.nextNode();
                    if (fileNode.isNodeType("nt:file")) {
                        Map<String, Object> fileMap = new HashMap<>();
                        fileMap.put("filePath", fileNode.getPath());
                        fileMap.put("fileDepth", fileNode.getDepth());
                        fileList.add(fileMap);
                    }
                }

            } finally {
                session.logout();
            }
            System.out.println("222222222222222222222");
            System.out.println(fileList);

            resultMap.put("fileList", fileList);
        }

        return resultMap;
    }


    public boolean folderNodeAdd(Map<String, Object> param) throws Exception {
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
                if (nodePath.equals("")) root.addNode(nodeName, nodeType);
                else root.addNode(nodePath + "/" + nodeName, nodeType);
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



    //루트 하위노드 삭제
    public boolean deleteNode(Map<String, Object> param) {
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

}
