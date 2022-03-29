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


//    public Map<String, Object> searchListByPath(Session session, String path) throws RepositoryException {
//        Map<String, Object> resultMap = new HashMap<>();
//        List<FileDto> search = findWithPid(session, path);
//        resultMap.put("searchList", search);
//        return resultMap;
//    }

    private List<FileDto> findWithPid(Session session, String path) throws RepositoryException {
        List<FileDto> buildDtoList = new ArrayList<>(); //최종 결과물을 담을 리스트
        Node root = session.getRootNode();
        FileDto targetDto = FileDto.builder().name("root").id(-1).pId(-1).build(); //초기값은 (root) id , pid 0로 시작

        if (path.equals("")) {  //경로를 루트를 받았을경우
            insertDtoList(buildDtoList, root, targetDto, 0, NodeType.NT_FOLDER);
            buildDtoList.add(targetDto);
            return buildDtoList;
        }
        String[] targetArr = path.split("/"); // 모든 슬러쉬로 경로를 구분
        Node targetNode = root; //타겟 노드가 반복되면서 마지막 경로까지 사용됨
        buildDtoList.add(targetDto);
        int startId = insertDtoList(buildDtoList, root, targetDto, 1, NodeType.NT_FOLDER); //insertDto 는 list 를받고 마지막 id+1 을반환
        for (int i = 1; i < targetArr.length; i++) { //총 경로의 단계만큼 반목
            targetNode = targetNode.getNode(targetArr[i]);
            for (FileDto dto : buildDtoList) {
                if (dto.getName().equals(targetArr[i])) { //fileDto 리스트중에 현재 찾을 경로의 이름을 가진 dto 를 찾아서 타겟으로설정
                    targetDto = dto;
                }
            }
            startId = insertDtoList(buildDtoList, targetNode, targetDto, startId, NodeType.NT_FOLDER);
        }
        return buildDtoList;
    }

    private int insertDtoList(List<FileDto> dtoList, Node parentNode, FileDto parentDto, int startId, String nodeType) throws RepositoryException {
        int idx = startId;
        //시작할 id 값을 받고 상위노드의 하위 노드들을 세팅
        NodeIterator nodeIterator = parentNode.getNodes();
        while (nodeIterator.hasNext()) {
            Node nowNode = nodeIterator.nextNode();
            if (nowNode.getName().contains(":")) { //기본 시스템이름에 : 들어감
                continue;
            }
            System.out.println("name=" + nowNode.getName() + "\tparent:" + parentDto.getName() + "\tparentId:" + parentDto.getId());
            if (nowNode.isNodeType(nodeType)) {
                if (nowNode.isNodeType(NodeType.NT_FILE)) {
                    Node res = nowNode.getNode(Property.JCR_CONTENT);
                    Date date = res.getProperty(Property.JCR_LAST_MODIFIED).getDate().getTime();
                    String lastUpdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                }
                dtoList.add(FileDto.builder()
                        .id(idx)
                        .pId(parentDto.getId())
                        .name(nowNode.getName())
                        .path(nowNode.getPath())
                        .lastUpdate(nowNode.isNodeType(NodeType.NT_FILE) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowNode.getNode(Property.JCR_CONTENT).getProperty(Property.JCR_LAST_MODIFIED).getDate().getTime()) : "")
                        .owner(nowNode.getProperty(Property.JCR_CREATED_BY).getString())
                        .type(nodeType)
                        .build()
                );
            }
            ;
            idx++;
        }
        return idx;
    }

    private Map<String, Object> makePageMap(List<FileDto> dtoList, String pageNum) {
        if (pageNum.length() == 0) {
            pageNum = "1";
        }
        Pagination pagination = new Pagination();
        int pageSize = 5;
        pagination.setPageSize(pageSize);
        pagination.setTotalCount(dtoList.size());
        pagination.setPageNo(Integer.parseInt(pageNum));
        int startNum = pageSize * (Integer.parseInt(pageNum) - 1);

        List<FileDto> fileList = new ArrayList<>();
        if (dtoList.size() > pageSize) {
            List<FileDto> newChildList = new ArrayList<>();
            for (int i = 0; startNum + i < dtoList.size(); i++) {
                FileDto fileDto = dtoList.get(startNum + i);
                newChildList.add(fileDto);
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
        pagingMap.put("fileList", fileList);
        System.out.println("fileLIstSize:" + fileList.size());
        return pagingMap;
    }


    public Map<String, Object> getNodeList(Map<String, Object> param, Session session) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String path = param.get("target").toString();
        String[] targetArr = path.split("/");


        Node root = session.getRootNode();
        Node fileNode = root;

        if (path.length() != 0) {
            for (String arr : targetArr) {
                fileNode = fileNode.getNode(arr);
            }
        }
        List<FileDto> folderList = findWithPid(session, "");
        List<FileDto> fileList = new ArrayList<>();
        FileDto targetDto = FileDto.builder().name("root").id(-1).pId(-1).build(); //초기값은 (root) id , pid 0로 시작
        insertDtoList(fileList, fileNode, targetDto, 1, NodeType.NT_FILE);
        String pageNo = (param.get("pageNo") == null) ? "1" : param.get("pageNo").toString();
        resultMap.put("pagingMap", makePageMap(fileList, pageNo));
        resultMap.put("folderList", folderList);
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
