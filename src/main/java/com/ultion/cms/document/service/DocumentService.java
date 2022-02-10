package com.ultion.cms.document.service;

import com.ultion.cms.jackRabbit.JackrabbitRepositoryConfigFactory;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    @Autowired
    JackrabbitRepositoryConfigFactory jackrabbitRepositoryConfigFactory;

    private static List<Map<String, Object>> nodeList = new ArrayList<>();

    public Map<String, Object> getNodeList() throws Exception {
        Repository repository = JcrUtils.getRepository();
        Session session = repository.login(new SimpleCredentials("admin",
                "admin".toCharArray()));
        System.out.println("콘픽파일넴" + jackrabbitRepositoryConfigFactory.getConfigFilename());
        System.out.println("쩨시알홈" + jackrabbitRepositoryConfigFactory.getJcrHome());
        System.out.println("프퍼티파일넴" + jackrabbitRepositoryConfigFactory.getPropertiesFilename());
        System.out.println("222222222222222222222222222222222");

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

    //노드 출력
    private static void dump(Node node) throws RepositoryException {

        String nodePath = node.getPath();
        String nodeName = node.getName();
        int nodeDepth = node.getDepth();

        System.out.println("노드 경로: " + nodePath);
        System.out.println("노드 이름: " + nodeName);
        System.out.println("노드 Depth: " + nodeDepth);

        if (nodeName.equals("jcr:system")) {
            return;
        }

        if (1 == nodeDepth && !nodeName.contains(":")) {
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("nodeName", nodeName);
            nodeMap.put("nodePath", nodePath);
            nodeList.add(nodeMap);
        }


        // 속성출력
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            if (property.getDefinition().isMultiple()) {
                // 다중 값을가지는 속성, 모든 값 출력
                Value[] values = property.getValues();
                for (Value value : values) {
                    System.out.println("멀티");
                    System.out.println(property.getPath() + " = "
                            + value.getString());
                }
            } else {
                // 단일 값을 가지는 속성
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
