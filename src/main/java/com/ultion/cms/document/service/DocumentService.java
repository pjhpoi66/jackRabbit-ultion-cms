package com.ultion.cms.document.service;

import com.ultion.cms.core.web.Pagination;
import com.ultion.cms.file.FileCharsetService;
import com.ultion.cms.file.FileDto;
import com.ultion.cms.file.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.apache.jackrabbit.value.DateValue;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentService {

    @org.springframework.beans.factory.annotation.Value("${upload.path}")
    private String uploadPath;
    @org.springframework.beans.factory.annotation.Value("${jcr.rep.home}")
    private String jcrHome;
    private final FileCharsetService fileCharsetService;




    public Map<String, Object> searchListByPath(Session session, String path) throws RepositoryException {
        Map<String, Object> resultMap = new HashMap<>();
        List<FileDto> search = findWithPid(session, path);
        resultMap.put("searchList", search);
        return resultMap;
    }

    private List<FileDto> findWithPid(Session session, String path) throws RepositoryException {
        List<FileDto> buildDtoList = new ArrayList<>(); //최종 결과물을 담을 리스트
        Node root = session.getRootNode();
        if (path.equals("")) {  //경로를 루트를 받았을경우
            insertDtoList(buildDtoList, root, FileDto.builder().id(0).build(), 0);
            return buildDtoList;
        }
        String[] targetArr = path.split("/"); // 모든 슬러쉬로 경로를 구분
        Node targetNode = root; //타겟 노드가 반복되면서 마지막 경로까지 사용됨
        FileDto targetDto = FileDto.builder().id(0).pId(0).build(); //초기값은 (root) id , pid 0로 시작
        int startId = insertDtoList(buildDtoList, root, targetDto, 1); //insertDto 는 list 를받고 마지막 id+1 을반환
        for (int i = 1; i < targetArr.length; i++) { //총 경로의 단계만큼 반목
            targetNode = targetNode.getNode(targetArr[i]);
            for (FileDto dto : buildDtoList) {
                if (dto.getName().equals(targetArr[i])) { //fileDto 리스트중에 현재 찾을 경로의 이름을 가진 dto 를 찾아서 타겟으로설정
                    targetDto = dto;
                }
            }
            startId = insertDtoList(buildDtoList, targetNode, targetDto, startId);
        }
        return buildDtoList;
    }

    private int insertDtoList(List<FileDto> dtoList, Node parentNode, FileDto parentDto, int startId) throws RepositoryException {
        int idx = startId;
        //시작할 id 값을 받고 상위노드의 하위 노드들을 세팅
        NodeIterator nodeIterator = parentNode.getNodes();
        while (nodeIterator.hasNext()) {
            Node nowNode = nodeIterator.nextNode();
            if(nowNode.getName().contains(":")) { //기본 시스템이름에 : 들어감
                continue;
            }
            System.out.println("parent:" + parentDto.getName()+"\tparentId:"+parentDto.getId());
            if(nowNode.isNodeType(NodeType.NT_FOLDER)) {
                dtoList.add(FileDto.builder()
                        .id(idx)
                        .pId(parentDto.getId())
                        .name(nowNode.getName())
                        .path(nowNode.getPath())
                        .type(nowNode.isNodeType(NodeType.NT_FILE) ? FileType.FILE.getValue() : FileType.FOLDER.getValue())
                        .build()
                );
            };
            idx++;
        }
        return idx;
    }

    private Map<String, Object> makePageMap(List<FileDto> dtoList) {
        Map<String, Object> pagingMap = new HashMap<>();
        Pagination pagination = new Pagination();
        int pageSize = 5;
        pagination.setPageSize(pageSize);
        pagination.setPageNo(1);
        pagination.setTotalCount(dtoList.size());
        pagingMap.put("pageList", pagination.getPageList());
        pagingMap.put("pageNo", pagination.getPageNo());
        pagingMap.put("pageSize", pagination.getPageSize());
        pagingMap.put("prevPageNo", pagination.getPrevPageNo());
        pagingMap.put("nextPageNo", pagination.getNextPageNo());
        pagingMap.put("finalPageNo", pagination.getFinalPageNo());
        return pagingMap;
    }

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
                        //폴더리스트
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

                    //폴더리스트 데이터들
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

    public Map<String, Object> getNodeList(Map<String, Object> param, Session session) throws Exception {

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
                        fileDto.setType(FileType.FILE.getValue());
                    } else {
                        fileDto.setLastUpdate("");
                        fileDto.setType(FileType.FOLDER.getValue());
                    }
                    String owner = fileNode.getProperty(Property.JCR_CREATED_BY).getString();
                    fileDto.setOwner(owner);

                    fileList.add(fileDto);
                }
            }

        } finally {
//            session.logout();
        }

        int rootChildListSize = fileList.size();
        Pagination pagination = new Pagination();
        final int pageSize = 5;
        pagination.setPageSize(pageSize); //한페이지에 보여줄 컨텐츠 숫자
        pagination.setPageNo(Integer.parseInt(param.get("pageNo").toString())); //이동하려는 페이지 번호
        pagination.setTotalCount(rootChildListSize);//총 컨텐츠 숫자


        int count = 0;
        if (rootChildListSize > pageSize) {
            int rowNum = pagination.getStartRowNum();
            List<FileDto> newChildList = new ArrayList<>();
            while (count < pageSize) { //페이지 사이즈만큼
                int sum = count + rowNum;
                if (sum < rootChildListSize) {
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


    public String downLoad(HttpServletRequest request, HttpServletResponse response, Session session, List<FileDto> fileDtos) throws Exception {
        Node root = session.getRootNode();

        fileDtos.forEach(dto -> {
            Node resourceNode = null;
            try {
                resourceNode = findResourceNode(root, dto);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            try (OutputStream os = response.getOutputStream();
                 InputStream is = JcrUtils.readFile(resourceNode)) {
                String changedFileName = fileCharsetService.getFileName(request, response, dto.getName());
                response.setHeader("Content-Description", "file download");
                response.setHeader("Content-Disposition", "attachment; filename=\"".concat(changedFileName).concat("\""));
                response.setHeader("Content-Transfer-Encoding", "binary");


                int read = 0;
                byte[] buffer = new byte[1024];
                while ((read = is.read(buffer)) != -1) { // 1024바이트씩 계속 읽으면서 outputStream에 저장, -1이 나오면 더이상 읽을 파일이 없음
                    os.write(buffer, 0, read);
                }
            } catch (RepositoryException | IOException e) {
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

    public Node findNode(Node root, FileDto dto) throws RepositoryException {

        String[] paths = dto.getPath().substring(1).split("/");
        Node findNode = root.getNode(paths[0]);
        for (int i = 1; i < paths.length; i++) {
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


    public boolean folderNodeAdd(Map<String, Object> param, Session session) throws Exception {
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
        } finally {
//            session.logout();
        }
        return isSuccess;
    }

    public boolean upload(String nodePath, MultipartHttpServletRequest request, Session session) throws Exception {
        boolean resultAdd = false;
        nodePath = nodePath.replaceAll("//ROOT/", "");
        List<MultipartFile> files = request.getFiles("file");

        for (MultipartFile file : files) {
            try {
                //노드 접근
                Node root = session.getRootNode();
                Node targetNode = root;
                System.out.println(nodePath);
                if (!nodePath.equals("")) {
                    targetNode = root.getNode(nodePath);
                }

                if (JcrUtils.getNodeIfExists(targetNode, file.getOriginalFilename()) != null) {
                    targetNode.getNode(file.getOriginalFilename()).remove();
                }
                Node fileNode = targetNode.addNode(file.getOriginalFilename(), NodeType.NT_FILE);
                Node resNode = fileNode.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);


                resNode.setProperty(Property.JCR_MIMETYPE, NodeType.NT_FILE);
                Calendar time = Calendar.getInstance();
                DateValue dv = new DateValue(time);
                resNode.setProperty(Property.JCR_LAST_MODIFIED, dv.getDate());
                resNode.setProperty(Property.JCR_ENCODING, StandardCharsets.UTF_8.name());
                resNode.setProperty(Property.JCR_DATA, file.getInputStream());
                System.out.println("만든이:" + fileNode.getProperty(Property.JCR_CREATED_BY));

                //데이터스토어 레코드추가
                InputStream is = JcrUtils.readFile(resNode);
                DataStore dataStore = new FileDataStore();
                dataStore.init(jcrHome);
                dataStore.addRecord(is);
                session.save();

                resultAdd = true;
            } catch (IOException e) {
                System.out.println("upload fail:" + file.getName());
                e.printStackTrace();
                log.debug(e.getMessage());
            } finally {
//                session.logout;
            }
            System.out.println("upload success:" + uploadPath + "/" + file.getOriginalFilename());
        }
//        session.logout();
        return resultAdd;
    }


    //노드 삭제
    public boolean deleteNode(Map<String, Object> param, Session session) {
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

    public String reNamingFile(Node node, String newName) throws RepositoryException {
        final String parentPath = node.getParent().getPath();
        final String path = parentPath.equals("/") ? parentPath : parentPath + "/";
        node.getSession().move(node.getPath(), path + newName);
        node.getSession().save();
        return "success";
//        node.getSession().logout();
    }

}
