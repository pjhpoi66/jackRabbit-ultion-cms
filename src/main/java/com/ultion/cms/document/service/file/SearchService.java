package com.ultion.cms.document.service.file;

import com.ultion.cms.file.FileDto;
import org.apache.jackrabbit.core.TransientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.List;

@Service

public class SearchService {

    public ModelAndView search(ModelAndView modelAndView, Session session, String folderPath) throws RepositoryException {
        Repository repository = new TransientRepository();
        List<FileDto> fileDtos = new ArrayList<>();


        Node root = session.getRootNode();
        NodeIterator nodeIterator = root.getNodes(folderPath);

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

        session.logout();
        return modelAndView;
    }


}
