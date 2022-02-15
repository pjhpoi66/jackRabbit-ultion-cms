package com.ultion.cms.document.service;

import org.apache.jackrabbit.core.TransientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.*;
import java.io.IOException;
import java.util.List;

@Service
public class UploadTestService {

    public void upload(List<MultipartFile> files) throws Exception {

        Repository repository = new TransientRepository();
        Session session = repository.login(new SimpleCredentials("admin",
                "admin".toCharArray()));

        
        Node root = session.getRootNode();


        if(!files.isEmpty()) {
            files.forEach(file -> {
                try {
                    Node fileNode = root.addNode(file.getName(), "nt:file");
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

        System.out.println("root:"+root.getName());

        for (NodeIterator it = root.getNodes(); it.hasNext(); ) {
            Node node = (Node) it.next();

            System.out.print("node:" + node.getName());
            System.out.println("\tpath:" + node.getPath());
            System.out.println(node.getProperty("jcr:mimeType"));

        }

        session.logout();
    }

}
