package com.ultion.cms.document.service.file;

import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.fs.local.LocalFileSystem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.*;
import javax.jcr.version.VersionHistory;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


@Service
public class DeleteService {

    public void delete() throws Exception {

        Repository repository = new TransientRepository();
        Session session = repository.login(new SimpleCredentials("admin",
                "admin".toCharArray()));


        Node root = session.getRootNode();

        Iterator<Node> nodeIterator = root.getNodes();
        LocalFileSystem fileSystem = new LocalFileSystem();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            System.out.println("nodePath:" + node.getPath() + "\tnodeName:" + node.getName());
            if (node.getName().contains("upload")) {
                node.remove();
                session.save();
            }
        }

        session.logout();
    }

}
