package com.ultion.cms.document.service;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.*;

public class CustomNode {
    public static void RegisterFileType(Session session) throws Exception {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();

        NodeTypeTemplate nodeType = nodeTypeManager.createNodeTypeTemplate();
        nodeType.setName("FileType");
        String[] str = {"nt:resource"};
        nodeType.setDeclaredSuperTypeNames(str);
        nodeType.setMixin(false);
        nodeType.setQueryable(true);


        PropertyDefinitionTemplate path = nodeTypeManager.createPropertyDefinitionTemplate();
        path.setName("jcr:path");
        path.setRequiredType(PropertyType.PATH);
        path.setQueryOrderable(false);
        path.setFullTextSearchable(false);
        nodeType.getPropertyDefinitionTemplates().add(path);

        PropertyDefinitionTemplate nom = nodeTypeManager.createPropertyDefinitionTemplate();
        nom.setName("jcr:nom");
        nom.setRequiredType(PropertyType.STRING);
        nom.setQueryOrderable(true);
        nom.setFullTextSearchable(true);
        nodeType.getPropertyDefinitionTemplates().add(nom);

        PropertyDefinitionTemplate description = nodeTypeManager.createPropertyDefinitionTemplate();
        description.setName("jcr:description");
        description.setRequiredType(PropertyType.STRING);
        description.setQueryOrderable(true);
        description.setFullTextSearchable(true);
        nodeType.getPropertyDefinitionTemplates().add(description);

        PropertyDefinitionTemplate motsCles = nodeTypeManager.createPropertyDefinitionTemplate();
        motsCles.setName("jcr:motsCles");
        motsCles.setRequiredType(PropertyType.STRING);
        motsCles.setQueryOrderable(true);
        motsCles.setFullTextSearchable(true);
        nodeType.getPropertyDefinitionTemplates().add(motsCles);

        PropertyDefinitionTemplate size = nodeTypeManager.createPropertyDefinitionTemplate();
        size.setName("jcr:size");
        size.setRequiredType(PropertyType.STRING);
        size.setQueryOrderable(true);
        size.setFullTextSearchable(false);
        nodeType.getPropertyDefinitionTemplates().add(size);

        PropertyDefinitionTemplate users = nodeTypeManager.createPropertyDefinitionTemplate();
        users.setName("jcr:users");
        users.setRequiredType(PropertyType.STRING);
        users.setQueryOrderable(true);
        users.setFullTextSearchable(false);
        nodeType.getPropertyDefinitionTemplates().add(users);

        PropertyDefinitionTemplate groupe = nodeTypeManager.createPropertyDefinitionTemplate();
        groupe.setName("jcr:groupe");
        groupe.setRequiredType(PropertyType.STRING);
        groupe.setQueryOrderable(true);
        groupe.setFullTextSearchable(false);
        nodeType.getPropertyDefinitionTemplates().add(groupe);

        PropertyDefinitionTemplate custom = nodeTypeManager.createPropertyDefinitionTemplate();
        custom.setName("jcr:custom");
        custom.setRequiredType(PropertyType.STRING);
        custom.setQueryOrderable(true);
        custom.setFullTextSearchable(false);
        nodeType.getPropertyDefinitionTemplates().add(custom);

        NodeType newnodetype = nodeTypeManager.registerNodeType(nodeType, true);
        session.save();
    }

    public static void showNodeTypes(Session session){

        NodeTypeManager manager;
        try {
            manager = (NodeTypeManager)session.getWorkspace().getNodeTypeManager();

            NodeTypeIterator nodeTypeIterator = manager.getAllNodeTypes();
            NodeType actual;

            while (nodeTypeIterator.hasNext()){
                System.out.println("----------------");
                actual= (NodeType)nodeTypeIterator.next();
                System.out.println(actual.getName());
                for(PropertyDefinition propertyDef:actual.getPropertyDefinitions()) {
                    System.out.println(propertyDef.getName() +" --> Mandatory: " + propertyDef.isMandatory());
                }
            }

        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
