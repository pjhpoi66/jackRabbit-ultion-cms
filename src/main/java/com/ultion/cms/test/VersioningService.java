package com.ultion.cms.test;


import org.springframework.stereotype.Service;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

@Service
public class VersioningService {



    public void versioningBasic(Node parentNode , Session session) throws RepositoryException {

        Node node = parentNode.addNode("childNode", "nt:unstructured");

        //create Versionable node
        node.addMixin("mix:versionable");
        node.setProperty("anyProperty", "Joy");
        session.save();
        Version firstVersion = node.checkin();

        //add new Version
        Node child = parentNode.getNode("childNode");
        child.checkout();
        child.setProperty("anyProperty", "무야호");
        session.save();
        child.checkin();

        //print version history
        VersionHistory history = child.getVersionHistory();

        for (VersionIterator it = history.getAllVersions(); it.hasNext(); ) {
            Version version = (Version) it.next();
            System.out.println("version:"+version.getCreated().getTime());
        }

        //restoring old verison
        child.checkout();
        child.restore(firstVersion, true);

    }


}
