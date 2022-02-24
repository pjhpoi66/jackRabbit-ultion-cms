package com.ultion.cms.document.service;

import com.ultion.cms.core.util.DateUtil;
import com.ultion.cms.core.web.Pagination;
import com.ultion.cms.file.FileDto;
import com.ultion.cms.file.FileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.apache.jackrabbit.value.DateValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class DocumentService {

    @org.springframework.beans.factory.annotation.Value("${upload.path}")
    private String uploadPath;
    @org.springframework.beans.factory.annotation.Value("${jcr.rep.home}")
    private String jcrHome;

    public Map<String, Object> indexPageLoad(Session session) throws Exception {
        Map<String, Object> result = new HashMap<>();
        try {
            Node root = session.getRootNode();
            NodeIterator dep1 = root.getNodes();
            List<Map<String, Object>> dep1NodeList = new ArrayList<>();
            List<FileDto> rootChildList = new ArrayList<>();

            while (dep1.hasNext()) {
                Node dep1Node = dep1.nextNode();

                if (!dep1Node.getName().contains(":")) {
                    FileDto fileDto = new FileDto();
                    fileDto.setName(dep1Node.getName());
                    fileDto.setName(dep1Node.getPath());
                    rootChildList.add(fileDto);
                }

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

            int rootChildListSize = rootChildList.size();
            Pagination pagination = new Pagination();
            int pageSize = 5;
            pagination.setPageSize(pageSize);
            pagination.setPageNo(1);
            pagination.setTotalCount(rootChildListSize);

            int count = 0;
            if (rootChildListSize > pageSize) {
                List<FileDto> newChildList = new ArrayList<>();
                while (count < pageSize) {
                    count++;
                    FileDto fileDto = rootChildList.get(count);
                    newChildList.add(fileDto);
                }
                rootChildList = newChildList;
            }
            Map<String, Object> pagingMap = new HashMap<>();
            pagingMap.put("pageList", pagination.getPageList());
            pagingMap.put("pageNo", pagination.getPageNo());
            pagingMap.put("pageSize", pagination.getPageSize());
            pagingMap.put("prevPageNo", pagination.getPrevPageNo());
            pagingMap.put("nextPageNo", pagination.getNextPageNo());
            pagingMap.put("finalPageNo", pagination.getFinalPageNo());

            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("fileList", rootChildList);
            fileMap.put("pagingMap", pagingMap);
            result.put("fileMap", fileMap);

            session.save();
        } finally {

//            session.logout();
        }
        return result;
    }

    public String downLoad(Session session, List<FileDto> fileDtos) throws RepositoryException {
        Node root = session.getRootNode();

        fileDtos.forEach(dto -> {
            Node resourceNode = null;
            try {
                resourceNode = findResourceNode(root, dto);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            try (OutputStream os = new FileOutputStream("C:\\Users\\user\\Downloads\\" + resourceNode.getName());
                 InputStream is = JcrUtils.readFile(resourceNode);) {
                IOUtils.copy(is, os);
            } catch (RepositoryException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
//        session.logout();
        return "success";

    }

    public Node findResourceNode(Node root, FileDto dto) throws RepositoryException {
        String[] paths = dto.getPath().substring(1).split("/");
        Node findNode = root.getNode(paths[0]);
        for (int i = 1; i < paths.length - 1; i++) {
            findNode = findNode.getNode(paths[i]);
        }
        return findNode.getNode(paths[paths.length - 1]);
    }

    public Node findFileNode(Node root, FileDto dto) throws RepositoryException {
        String[] paths = dto.getPath().substring(1).split("/");
        Node findNode = root.getNode(paths[0]);
        for (int i = 1; i < paths.length - 1; i++) {
            findNode = findNode.getNode(paths[i]);
        }
        return findNode;
    }


    public String getNameByDto(FileDto dto) {
        return dto.getPath().substring(dto.getPath().lastIndexOf("/")).replace("/", "");
    }

    public String getNameByPath(String path) {
        return path.substring(path.lastIndexOf("/")).replace("/", "");
    }

    public Map<String, Object> getNodeList(Map<String, Object> param,Session session) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        int depth = Integer.parseInt(param.get("depth").toString());
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


                    if (fileNode.isNodeType(("nt:file"))) {
                        Node res = fileNode.getNode(Property.JCR_CONTENT);
                        Date date = res.getProperty(Property.JCR_LAST_MODIFIED).getDate().getTime();
                        String lastUpdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                        fileDto.setLastUpdate(lastUpdate);
                        fileDto.setContentType(FileType.FILE.getValue());
                    } else {
                        fileDto.setLastUpdate("");
                        fileDto.setContentType(FileType.FOLDER.getValue());
                    }
                    fileList.add(fileDto);
                }
            }

        } finally {
//            session.logout();
        }

        int rootChildListSize = fileList.size();
        Pagination pagination = new Pagination();
        final int pageSize = 5;
        pagination.setPageSize(pageSize);
        pagination.setPageNo(Integer.parseInt(param.get("pageNo").toString()));
        pagination.setTotalCount(rootChildListSize);


        int count = 0;
        if (rootChildListSize > pageSize) {
            int rowNum = pagination.getStartRowNum();
            List<FileDto> newChildList = new ArrayList<>();
            while (count < pageSize) { //페이지 사이즈만큼
                int sum = count + rowNum;
                if(sum < rootChildListSize){
                    FileDto fileDto = fileList.get(sum);
                    newChildList.add(fileDto);
                }
                count++;
            }
            fileList = newChildList;
        }
        Map<String, Object> pagingMap = new HashMap<>();
        pagingMap.put("pageList", pagination.getPageList());
        pagingMap.put("pageNo", pagination.getPageNo());
        pagingMap.put("pageSize", pagination.getPageSize());
        pagingMap.put("prevPageNo", pagination.getPrevPageNo());
        pagingMap.put("nextPageNo", pagination.getNextPageNo());
        pagingMap.put("finalPageNo", pagination.getFinalPageNo());
        resultMap.put("pagingMap", pagingMap);
        resultMap.put("fileList", fileList);
        return resultMap;
    }


    public boolean folderNodeAdd(Map<String, Object> param , Session session) throws Exception {
        boolean isSuccess = false;
        String nodeName = param.get("nodeName").toString();
        String nodeType = "nt:folder";
        String nodePath = param.get("nodePath").toString();
        nodePath = nodePath.replaceAll("//ROOT/", "");

        System.out.println("노드패스 : " + nodePath);
        try {
            Node root = session.getRootNode();
            System.out.println("노패 : " + nodePath);
            if (nodePath.equals("")) {
                root.addNode(nodeName, nodeType);
            } else {
                root.addNode(nodePath + "/" + nodeName, nodeType);
            }
            isSuccess = true;
            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
//            session.logout();
        }
        return isSuccess;
    }

    public boolean upload(String nodePath, MultipartHttpServletRequest request) throws Exception {
        boolean resultAdd = false;
        nodePath = nodePath.replaceAll("//ROOT/", "");
        List<MultipartFile> files = request.getFiles("file");
        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fileExt = FilenameUtils.getExtension(fileName);
            String fileBaseName = FilenameUtils.getBaseName(fileName);
            try {
                //일반 파일 업로드
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                File temp = new File(fileBaseName + "_" + DateUtil.getNowDate() + "." + fileExt);

                //노드 접근
                Node root = session.getRootNode();
                Node targetNode = root;
                System.out.println(nodePath);
                if (!nodePath.equals("")) {
                    targetNode = root.getNode(nodePath);
                }

                if (JcrUtils.getNodeIfExists(targetNode, file.getOriginalFilename()) == null) {
                    Node fileNode = targetNode.addNode(file.getOriginalFilename(), NodeType.NT_FILE);
                    Node resNode = fileNode.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);

                    resNode.setProperty(Property.JCR_MIMETYPE, NodeType.NT_FILE);
                    Calendar time = Calendar.getInstance();
                    DateValue dv = new DateValue(time);
                    resNode.setProperty(Property.JCR_LAST_MODIFIED, dv.getDate());
                    resNode.setProperty(Property.JCR_ENCODING, StandardCharsets.UTF_8.name());
                    resNode.setProperty(Property.JCR_DATA, file.getInputStream());

                    file.transferTo(temp);

                    //데이터스토어 레코드추가
                    FileInputStream is = new FileInputStream(uploadPath + "\\" + temp);
                    DataStore dataStore = new FileDataStore();
                    dataStore.init(jcrHome);
                    dataStore.addRecord(is);
                    session.save();
                }


                resultAdd = true;
            } catch (IOException e) {
                System.out.println("upload fail:" + file.getName());
                e.printStackTrace();
                log.debug(e.getMessage());
            }finally {
            }
            System.out.println("upload success:" + uploadPath + "/" + file.getOriginalFilename());
        }
//        session.logout();
        return resultAdd;
    }


    //노드 삭제
    public boolean deleteNode(Map<String, Object> param ,Session session) {
        boolean isSuccess = false;
        String nodePath = param.get("nodePath").toString();
        nodePath = nodePath.replaceAll("//ROOT/", "");
        try {
            Node root = session.getRootNode();
            Node targetNode = root.getNode(nodePath);
            targetNode.remove();
            session.save();
//            session.logout();
            isSuccess = true;
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return isSuccess;
    }

    public void reNamingFile(Node node, String newName) throws RepositoryException {
        node.getSession().move(node.getPath(), node.getParent().getPath()  + newName);
        node.getSession().save();
//        node.getSession().logout();
    }

}
