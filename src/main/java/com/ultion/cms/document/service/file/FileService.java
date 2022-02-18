package com.ultion.cms.document.service.file;

import com.ultion.cms.file.FileDto;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.fs.local.LocalFileSystem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class FileService {

    public ModelAndView search(ModelAndView modelAndView, Session session, String folderPath ) throws RepositoryException {
        List<FileDto> fileDtos = new ArrayList<>();
        Node root = session.getRootNode();
        NodeIterator nodeIterator = root.getNodes("upload");

        while (nodeIterator.hasNext()) {
            Node parentNode = nodeIterator.nextNode();
            NodeIterator nodes = parentNode.getNodes();
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                FileDto dto = new FileDto();
                dto.setFileName(node.getName());
                dto.setPath(node.getPath());
                fileDtos.add(dto);
                System.out.println("path:" + node.getPath() + "\tname:" + node.getName()
                        + "\ttype:" + node.getPrimaryNodeType().getName());
            }
        }
        modelAndView.addObject("dtos", fileDtos);
        return modelAndView;
    }

    public void upload(List<MultipartFile> files , Repository repository , Session session) throws Exception {
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
    }
    public void downLoad(Session session, FileDto fileDto) throws RepositoryException {

        Node root = session.getRootNode();

        System.out.println("다운패스:" + fileDto.getPath());
        Node upload = root.getNode("upload");
        Node downLoadNode = upload.getNode( fileDto.getFileName());

        File Folder = new File(fileDto.getPath());

        if (!Folder.exists()) {
            try{
                Folder.mkdirs(); //폴더 생성합니다.
            }
            catch(Exception e){
                e.getStackTrace();
            }
        }

        try (OutputStream os = new FileOutputStream("C:\\Users\\user\\Downloads\\"+fileDto.getFileName());
             InputStream is = JcrUtils.readFile(downLoadNode);
        ) {
            IOUtils.copy(is, os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void delete(Session session) throws Exception {
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
    }

}
