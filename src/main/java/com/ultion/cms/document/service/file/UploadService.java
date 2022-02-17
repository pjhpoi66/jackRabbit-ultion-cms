package com.ultion.cms.document.service.file;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


@Service
public class UploadService {

    public void upload(List<MultipartFile> files) throws Exception {

        Repository repository = new TransientRepository();
        Session session = repository.login(new SimpleCredentials("admin",
                "admin".toCharArray()));


        Node root = session.getRootNode();

        Node folderNode = root.addNode("upload", "nt:folder");

        if (!files.isEmpty()) {
            files.forEach(file -> {
                try {
                    //upload 노드를 생성

                    Node fileNode = folderNode.addNode(file.getOriginalFilename(), "nt:file");
                    Node resNode = fileNode.addNode("jcr:content", "nt:resource");
                    resNode.setProperty("jcr:mimeType", "file");
                    resNode.setProperty("jcr:encoding", "utf-8");
                    resNode.setProperty("jcr:data", file.getInputStream());
                    session.save();
                } catch (RepositoryException | IOException e) {
                    e.printStackTrace();
                }
            });
        }

        System.out.println("root:" + root.getName());


        for (Iterator<Node> it = root.getNodes(); !it.hasNext(); ) {
            Node node = it.next();
            System.out.println(node.getPrimaryNodeType());
        }

        session.logout();
    }

}
