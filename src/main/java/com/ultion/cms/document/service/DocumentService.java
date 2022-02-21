package com.ultion.cms.document.service;

import com.ultion.cms.core.util.DateUtil;
import com.ultion.cms.file.FileDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
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

                            if (dep2Node.isNodeType("nt:folder"))
                                dep2NodeList.add(dep2NodeMap);

                        }

                        dep1NodeMap.put("children", dep2NodeList);
                        dep1NodeMap.put("hasChild", true);
                    }

                    dep1NodeMap.put("name", dep1Node.getName());
                    dep1NodeMap.put("nodePath", dep1Node.getPath());
                    dep1NodeMap.put("nodeIndex", dep1Node.getIndex());

                    if (dep1Node.isNodeType("nt:folder"))
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

    public String downLoad(Session session, FileDto dto) throws RepositoryException {

        Node root = session.getRootNode();

        String[] paths = dto.getPath().substring(1).split("/");

        Node uploadNode = root.getNode(paths[0]);
        for (int i = 1; i < paths.length - 1; i++) {
            uploadNode = uploadNode.getNode(paths[i]);
        }
        Node fileNode = uploadNode.getNode(paths[paths.length - 1]);


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

        String targetString = param.get("target").toString();
        String[] targetArr = targetString.split("/");

        List<FileDto> fileList = new ArrayList<>();
        try {
            Node node = session.getRootNode();
            if (1 < depth) {
                for (int i = 0; i < depth - 1; i++) {
                    node = node.getNode(targetArr[i]);
                }
            }

            NodeIterator fileNodes = node.getNodes();
            while (fileNodes.hasNext()) {

                Node fileNode = fileNodes.nextNode();
                if (fileNode.isNodeType("nt:file") || fileNode.isNodeType("nt:folder")) {
                    FileDto fileDto = new FileDto();
                    fileDto.setName(fileNode.getName());
                    fileDto.setPath(fileNode.getPath());

                    fileList.add(fileDto);
                }
            }

        } finally {
            session.logout();
        }
        System.out.println(fileList);

        resultMap.put("fileList", fileList);


        return resultMap;
    }


    public boolean folderNodeAdd(Map<String, Object> param) throws Exception {
        boolean isSuccess = false;
        String nodeName = param.get("nodeName").toString();
        String nodeType = param.get("nodeType").toString();
        String nodePath = param.get("nodePath").toString();
        nodePath = nodePath.replaceAll("//ROOT/", "");

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
            session.logout();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return isSuccess;
    }

    public boolean upload(String nodePath, MultipartHttpServletRequest request) throws Exception {
        boolean resultAdd = false;
        nodePath = nodePath.replaceAll("//ROOT/", "");
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
                Node fileNode = targetNode.addNode(file.getOriginalFilename(), NodeType.NT_FILE);
                Node resNode = fileNode.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);
                resNode.setProperty(Property.JCR_MIMETYPE, NodeType.NT_FILE);
                resNode.setProperty(Property.JCR_LAST_MODIFIED, Calendar.getInstance());
                resNode.setProperty(Property.JCR_ENCODING, StandardCharsets.UTF_8.name());
                resNode.setProperty(Property.JCR_DATA, file.getInputStream());

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
            }
            System.out.println("upload success:" + uploadPath + "/" + file.getOriginalFilename());
        }

        return resultAdd;
    }


    //노드 삭제
    public boolean deleteNode(Map<String, Object> param) {
        boolean isSuccess = false;
        String nodePath = param.get("nodePath").toString();
        nodePath = nodePath.replaceAll("//ROOT/", "");
        try {
            Repository repository = JcrUtils.getRepository();
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

            Node root = session.getRootNode();
            Node targetNode = root.getNode(nodePath);

            targetNode.remove();
            session.save();
            session.logout();

            isSuccess = true;
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        return isSuccess;
    }

}
