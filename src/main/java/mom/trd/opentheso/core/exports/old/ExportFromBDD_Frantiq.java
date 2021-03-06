package mom.trd.opentheso.core.exports.old;

import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import mom.trd.opentheso.bdd.helper.GroupHelper;
import mom.trd.opentheso.bdd.helper.ConceptHelper;
import mom.trd.opentheso.bdd.helper.ThesaurusHelper;
import mom.trd.opentheso.bdd.helper.nodes.NodeUri;
import mom.trd.opentheso.bdd.helper.nodes.concept.NodeConceptExport;
import mom.trd.opentheso.bdd.helper.nodes.group.NodeGroupLabel;
import mom.trd.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;


public class ExportFromBDD_Frantiq {

    private StringBuffer skos;
    private String serverArk;
    private String serverAdress;
    private boolean isArkActive;

    public void setServerArk(String serverArk) {
        this.serverArk = serverArk;
    }

    public void setServerAdress(String serverAdress) {
        this.serverAdress = serverAdress;
    }

    public void setArkActive(boolean arkActive) {
        this.isArkActive = arkActive;
    }


    public ExportFromBDD_Frantiq() {
    }
    
    

    /**
     * Fonction récursive permettant d'exporter le thésaurus de la tête jusqu'au
     * dernier descripteur
     *
     * @param ds
     * @param idThesaurus 
     * @return  
     */
    public StringBuffer exportThesaurus(HikariDataSource ds, String idThesaurus) {
        /*
         * Ecriture du thésaurus
         */
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        
        NodeThesaurus nodeThesaurus = thesaurusHelper.getNodeThesaurus(ds, idThesaurus);
       
        WriteFileSKOS_Frantiq writeFileSKOS = new WriteFileSKOS_Frantiq();
        
        // inititialisation des URI
        writeFileSKOS.setServerArk(serverArk);
        writeFileSKOS.setServerAdress(serverAdress);
        
        writeFileSKOS.writeHeader();

        
        String idArk = thesaurusHelper.getIdArkOfThesaurus(ds, idThesaurus);
        if(idArk ==null || idArk.trim().isEmpty()){
            writeFileSKOS.setURI(serverAdress);
        }
        else {
            writeFileSKOS.setURI(serverArk);
        }
    
    //    writeFileSKOS.setURI("http://opentheso.frantiq.fr/" + nodeThesaurus.getListThesaurusTraduction().get(0).getTitle());
        //	thesaurus.description);
        
        writeFileSKOS.writeThesaurus(nodeThesaurus);

        // ecriture des TopConcept
        GroupHelper conceptGroupHelper = new GroupHelper();

        ArrayList <String> tabIdGroup = conceptGroupHelper.getListIdOfGroup(ds, idThesaurus);
        for (String tabIdGroup1 : tabIdGroup) {
            writeFileSKOS.writeTopConcepts(tabIdGroup1);
        }
        
        writeFileSKOS.writeEndOfMicroThesaurusList();
        // fin du bloc thésaurus et id of TopConcept
        
        
        // écriture des Domaines et Descripteurs avec traductions
        ArrayList <NodeUri> idOfTopConcept;
        for (String tabIdGroup1 : tabIdGroup) {
            NodeGroupLabel nodeGroupLabel = conceptGroupHelper.getNodeGroupLabel(ds, tabIdGroup1, idThesaurus);
            idOfTopConcept = new ConceptHelper().getListIdsOfTopConceptsForExport(ds, tabIdGroup1, idThesaurus);
            writeFileSKOS.writeGroup(nodeGroupLabel, idOfTopConcept);
        }
        
        for (String tabIdGroup1 : tabIdGroup) {
            idOfTopConcept = new ConceptHelper().getListIdsOfTopConceptsForExport(ds, tabIdGroup1, idThesaurus);
            for (NodeUri idOfTopConcept1 : idOfTopConcept) {
                exportAllConcepts(ds, idOfTopConcept1.getIdConcept(), idThesaurus, writeFileSKOS);
            }
        }
        writeFileSKOS.endSkos();
        return writeFileSKOS.getSkosBuff();
    }
    
        /**
     * Fonction permettant d'exporter un noeud Groupe (un seul identifiant group)
     *
     * @param ds
     * @param idThesaurus 
     * @param idGroup 
     * @return  Skos
     */
    public StringBuffer exportThisGroup(HikariDataSource ds, String idThesaurus, String idGroup) {
       
        WriteFileSKOS writeFileSKOS = new WriteFileSKOS();
        
        // inititialisation des URI
        writeFileSKOS.setServerArk(serverArk);
        writeFileSKOS.setServerAdress(serverAdress);
        
        writeFileSKOS.writeHeader();

        // ecriture des TopConcept
        GroupHelper conceptGroupHelper = new GroupHelper();

        // écriture des Domaines et Descripteurs avec traductions
        NodeGroupLabel nodeGroupLabel = conceptGroupHelper.getNodeGroupLabel(ds, idGroup, idThesaurus);
        nodeGroupLabel.setIdArk(new GroupHelper().getIdArkOfGroup(ds, idGroup, idThesaurus));
            
        ArrayList<NodeUri> idOfTopConcept = new ConceptHelper().getListIdsOfTopConceptsForExport(ds, idGroup, idThesaurus);
        
        writeFileSKOS.writeGroup(nodeGroupLabel, idOfTopConcept, null);
        
        writeFileSKOS.endSkos();
        return writeFileSKOS.getSkosBuff();
    }
    
    
    /**
     * Fonction récursive permettant d'exporter le Groupe de la tête jusqu'au
     * dernier descripteur
     *
     * @param ds
     * @param idThesaurus 
     * @param idGroup 
     * @return  
     */
    public StringBuffer exportGroup(HikariDataSource ds, String idThesaurus, String idGroup) {
       
        WriteFileSKOS_Frantiq writeFileSKOS = new WriteFileSKOS_Frantiq();

        // inititialisation des URI
        writeFileSKOS.setServerArk(serverArk);
        writeFileSKOS.setServerAdress(serverAdress);       
        
        writeFileSKOS.writeHeader();

        // ecriture des TopConcept
        GroupHelper conceptGroupHelper = new GroupHelper();

        // écriture des Domaines et Descripteurs avec traductions
        NodeGroupLabel nodeGroupLabel = conceptGroupHelper.getNodeGroupLabel(ds, idGroup, idThesaurus);
            
        ArrayList <NodeUri> idOfTopConcept = new ConceptHelper().getListIdsOfTopConceptsForExport(ds, idGroup, idThesaurus);
        writeFileSKOS.writeGroup(nodeGroupLabel, idOfTopConcept);
        
        //écriture des TopConcepts
        for (NodeUri idOfTopConcept1 : idOfTopConcept) {
            exportAllConcepts(ds, idOfTopConcept1.getIdConcept(), idThesaurus, writeFileSKOS);
        }
        writeFileSKOS.endSkos();
        return writeFileSKOS.getSkosBuff();
    }    

    /**
     * Fonction permettant d'exporter Un concept
     *
     * @param ds
     * @param idThesaurus 
     * @param idConcept 
     * @return  Le Skos en String
     */
    public StringBuffer exportConcept(HikariDataSource ds, String idThesaurus, String idConcept) {
       
        WriteFileSKOS writeFileSKOS = new WriteFileSKOS();
        
        // inititialisation des URI
        writeFileSKOS.setServerArk(serverArk);
        writeFileSKOS.setServerAdress(serverAdress);
        
        writeFileSKOS.writeHeader();
        ConceptHelper conceptHelper = new ConceptHelper();
        
        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idConcept, idThesaurus, isArkActive);
        writeFileSKOS.writeDescriptor(nodeConcept, null);
        writeFileSKOS.endSkos(); 
//        System.out.println(writeFileSKOS.getSkosBuff().toString());
        
        return writeFileSKOS.getSkosBuff();
     }
    
    /**
     * Fonction permettant d'exporter Un concept
     *
     * @param ds
     * @param arkId
     * @return  Le Skos en String
     */
    public StringBuffer exportConcept(HikariDataSource ds, String arkId) {
       
        WriteFileSKOS writeFileSKOS = new WriteFileSKOS();
        
        // inititialisation des URI
        writeFileSKOS.setServerArk(serverArk);
        writeFileSKOS.setServerAdress(serverAdress);
        
        writeFileSKOS.writeHeader();
        ConceptHelper conceptHelper = new ConceptHelper();
        
        String idConcept = conceptHelper.getIdConceptFromArkId(ds, arkId);
        if(idConcept == null) return null;
        String idThesaurus = conceptHelper.getIdThesaurusFromArkId(ds, arkId);
        if(idThesaurus == null) return null;
        
        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idConcept, idThesaurus, isArkActive);
        writeFileSKOS.writeDescriptor(nodeConcept, null);
        writeFileSKOS.endSkos(); 
//        System.out.println(writeFileSKOS.getSkosBuff().toString());
        
        return writeFileSKOS.getSkosBuff();
     }    
    
    /**
     * Fonction permettant d'exporter un concept ou plusieurs concepts
     *
     * @param ds
     * @param idThesaurus 
     * @param value 
     * @param lang 
     * @return  Le Skos en String
     */
    public StringBuffer exportMultiConcept(HikariDataSource ds, String idThesaurus, String value, String lang) {
       
        WriteFileSKOS writeFileSKOS = new WriteFileSKOS();
        writeFileSKOS.writeHeader();
        
        // inititialisation des URI
        writeFileSKOS.setServerArk(serverArk);
        writeFileSKOS.setServerAdress(serverAdress);
        
        
        ConceptHelper conceptHelper = new ConceptHelper();
        
        ArrayList<NodeConceptExport> listNodeConcept = conceptHelper.getMultiConceptForExport(ds, value, idThesaurus, lang, isArkActive);
        
        for(NodeConceptExport nodeConcept : listNodeConcept) {
            writeFileSKOS.writeDescriptor(nodeConcept, null);
        }
        writeFileSKOS.endSkos(); 
        return writeFileSKOS.getSkosBuff();
    }
    
    
    /**
     * Fonction permet d'exporter une branche complète d'un thésaurus
     * en partant de n'importe quel concept, mais pas d'un Domaine 
     * @param ds
     * @param idConcept
     * @param idThesaurus 
     * @return  
     */
    public StringBuffer exportBranchOfConcept(HikariDataSource ds, String idThesaurus, String idConcept) {
        
        WriteFileSKOS_Frantiq writeFileSKOS = new WriteFileSKOS_Frantiq();
        
        // inititialisation des URI
        writeFileSKOS.setServerArk(serverArk);
        writeFileSKOS.setServerAdress(serverAdress);
        
        writeFileSKOS.writeHeader();
        NodeUri nodeUri = new NodeUri();
        nodeUri.setIdConcept(idConcept);
        exportAllConcepts(ds, nodeUri.getIdConcept(), idThesaurus, writeFileSKOS);
        writeFileSKOS.endSkos(); 
        return writeFileSKOS.getSkosBuff();
    }
    
    
    public void exportAllConcepts(HikariDataSource ds,
            String idConcept, String idThesaurus,
            WriteFileSKOS_Frantiq writeFileSKOS) {

        ConceptHelper conceptHelper = new ConceptHelper();
        
        ArrayList <String> listIdsOfConceptChildren = conceptHelper.getListChildrenOfConcept(ds, idConcept, idThesaurus);
        
        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idConcept, idThesaurus, isArkActive);
      
        /// attention il y a un problème ici, il faut vérifier pourquoi nous avons un Concept Null
        
        
        if(nodeConcept.getConcept() == null) {
            int k = 0;
            return;
        }
        
        writeFileSKOS.writeDescriptor(nodeConcept);

        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            nodeConcept = conceptHelper.getConceptForExport(ds, listIdsOfConceptChildren1, idThesaurus, isArkActive);
            writeFileSKOS.writeDescriptor(nodeConcept);
            if (!nodeConcept.getNodeListOfNT().isEmpty()) {
                for (int j = 0; j < nodeConcept.getNodeListOfNT().size(); j++) {

                    exportAllConcepts(ds,
                            nodeConcept.getNodeListOfNT().get(j).getUri().getIdConcept(),
                            idThesaurus, writeFileSKOS);

                }
            }
        }
    }

}
