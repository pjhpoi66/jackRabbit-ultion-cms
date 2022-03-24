package com.ultion.cms.document.service;

import com.ultion.cms.core.web.Pagination;
import com.ultion.cms.file.FileDto;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeService {
    public Map<String, Object> getBuildDtoList(Session session, String path) throws RepositoryException {
        Map<String, Object> rootMap = new HashMap<>();
        Map<String, Object> targetMap = rootMap;
        Node root = session.getRootNode();
        System.out.println("path:" + path);
        String[] targetArr = path.split("/"); // 모든 슬러쉬로 경로를 구분
        Node targetNode = root; //타겟 노드가 반복되면서 마지막 경로까지 사용됨
        List<FileDto> rootFiles = new ArrayList<>();
        if (path.equals("/") || path.equals("//")) {
            targetMap = insertFolderList(targetMap, targetNode);
        }
        for (int i = 0; i < targetArr.length; i++) { //총 경로의 단계만큼 반목
            if (i == 0) {
                targetMap = insertFolderList(targetMap, targetNode);
            }else {
                targetNode = targetNode.getNode(targetArr[i]);
                targetMap = insertFolderList(targetMap, targetNode);
            }
        }
        //페이징
        List<FileDto> fileDtoList = (List<FileDto>) targetMap.get("fileList");
        Map<String, Object> returnMap = new HashMap<>();
        rootMap.put("pagingMap", makePageMap(fileDtoList));
        return rootMap;
    }

    private Map<String, Object> insertFolderList(Map<String, Object> targetMap, Node parentNode) throws RepositoryException {
        List<FileDto> dtoList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        //시작할 id 값을 받고 상위노드의 하위 노드들을 세팅
        NodeIterator nodeIterator = parentNode.getNodes();
        while (nodeIterator.hasNext()) {
            Node nowNode = nodeIterator.nextNode();
            if (nowNode.getName().contains(":")) { //기본 시스템이름에 : 들어감
                continue;
            }
            dtoList.add(FileDto.builder()
                    .name(nowNode.getName())
                    .path(nowNode.getPath())
                    .type(nowNode.isNodeType(NodeType.NT_FOLDER) ? NodeType.NT_FOLDER : NodeType.NT_FILE)
                    .build()
            );
        }
        resultMap.put("fileList", dtoList);
        resultMap.put("name", parentNode.getName() == null ? "root" : parentNode.getName());
        targetMap.put("children", resultMap);

        return resultMap;
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
}
