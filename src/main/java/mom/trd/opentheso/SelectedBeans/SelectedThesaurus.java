package mom.trd.opentheso.SelectedBeans;

//import com.hp.hpl.jena.util.OneToManyMap;
import com.itextpdf.text.DocumentException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import mom.trd.opentheso.bdd.datas.Concept;
import mom.trd.opentheso.bdd.datas.Languages_iso639;
import mom.trd.opentheso.bdd.datas.Thesaurus;
import mom.trd.opentheso.bdd.helper.CandidateHelper;
import mom.trd.opentheso.bdd.helper.ConceptHelper;
import mom.trd.opentheso.bdd.helper.Connexion;
import mom.trd.opentheso.bdd.helper.FacetHelper;
import mom.trd.opentheso.bdd.helper.GroupHelper;
import mom.trd.opentheso.bdd.helper.LanguageHelper;
import mom.trd.opentheso.bdd.helper.TermHelper;
import mom.trd.opentheso.bdd.helper.ThesaurusHelper;
import mom.trd.opentheso.bdd.helper.ToolsHelper;
import mom.trd.opentheso.bdd.helper.UserHelper;
import mom.trd.opentheso.bdd.helper.nodes.NodeFacet;
import mom.trd.opentheso.bdd.helper.nodes.NodePreference;
import mom.trd.opentheso.bdd.helper.nodes.candidat.NodeCandidatValue;
import mom.trd.opentheso.bdd.helper.nodes.candidat.NodeTraductionCandidat;
import mom.trd.opentheso.bdd.helper.nodes.group.NodeGroup;
import mom.trd.opentheso.bdd.tools.StringPlus;
import mom.trd.opentheso.core.exports.old.ExportFromBDD;
import mom.trd.opentheso.core.jsonld.helper.JsonHelper;
import skos.SKOSXmlDocument;
import mom.trd.opentheso.core.exports.helper.ExportPrivatesDatas;
import mom.trd.opentheso.core.exports.helper.ExportStatistiques;
import mom.trd.opentheso.core.exports.privatesdatas.LineOfData;
import mom.trd.opentheso.core.exports.privatesdatas.tables.Table;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean(name = "theso", eager = true)
@SessionScoped

public class SelectedThesaurus implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Thesaurus thesaurus;
    private Thesaurus editTheso;
    private NodeGroup nodeCG;
    private String langueTrad ="";
    private String nomTrad = "";
    private ArrayList<Entry<String, String>> arrayTheso;
    private ArrayList<Entry<String, String>> arrayTrad;
    private ArrayList<Entry<String, String>> arrayFacette;
    private SelectItem[] langues;
    private SelectItem[] languesTheso;
    private List<NodeCandidatValue> filteredCandidats;
    private List<NodeCandidatValue> filteredCandidatsV;
    private List<NodeCandidatValue> filteredCandidatsA;
    private String idEdit;
    private String langueEdit;
    private String valueEdit;
    
    // Variables URL
    private String idCurl;
    private String idTurl;
    private String idLurl;
    private String idGurl; // id du groupe
    
    
    // Variables resourcesBundle
    private String langueSource; // la langue de travail par thésaurus (info de la base de données)
    private String workLanguage; // la langue de travail pour démarrer (info du fichier de configuration
    private String cheminSite;
    private String defaultThesaurusId;
    private String identifierType;
    
    private String idNaan;    
    
    
    private NodePreference nodePreference;
    private String version;
    private boolean arkActive;
    private String serverArk;
    
    // Backup
    private ArrayList<String> tablesList;
    private ArrayList<String> tablesListPrivate;
    private ArrayList<Table> sortirXml;
    
    private String nomdufichier;
    private StreamedContent file;


    

    
    
    // niveaux des classes Beans
    // theso -> treeBean -> selectedTerme -> user1
    //   4   ->    3     ->      2        ->   1
    // ordre d'initialisation des beans
    
    
    @ManagedProperty(value = "#{poolConnexion}")
    private Connexion connect;
    
    @ManagedProperty(value = "#{treeBean}")
    private TreeBean tree;


    
    @ManagedProperty(value = "#{ssTree}")
    private UnderTree uTree;
    
    @ManagedProperty(value = "#{selectedCandidat}")
    private SelectedCandidat candidat;
    
    @ManagedProperty(value = "#{statBean}")
    private StatBean statBean;
    
    @ManagedProperty(value = "#{langueBean}")
    private LanguageBean langueBean;
    
    @ManagedProperty(value = "#{vue}")
    private Vue vue;
    
    @ManagedProperty(value = "#{conceptbean}")
    private ConceptBean conceptbean;   
    /************************************** INITIALISATION **************************************/
    
    /**
     * Récupération des préférences
     *
     * @return la ressourceBundle des préférences
     */
    private ResourceBundle getBundlePref() {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
        return bundlePref;
    }

    /**
     * Récupération des préférences Ark
     *
     * @return la ressourceBundle des préférences Ark
     */
    private ResourceBundle getBundlePrefArk() {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "ark");
        return bundlePref;
    }     
    
    /**
     * récupère la variable URL et affiche le terme qu'elle désigne
     */
    public void preRenderView() {
        // if the URL is for Concept
        if (idCurl != null && idTurl != null) {

            idLurl = Locale.getDefault().toString().substring(0, 2);
            ArrayList<Languages_iso639> temp = new LanguageHelper().getLanguagesOfThesaurus(connect.getPoolConnexion(), idTurl);
            if (temp.isEmpty()) {
                idCurl = null;
                idGurl = null;                
                idTurl = null;
                return;
            } else {
                boolean lExist = false;
                for(Languages_iso639 l : temp) {
                    if(l.getId_iso639_1().trim().equals(idLurl)) {
                        lExist = true;
                    }
                }
                if(!lExist) {
                    idLurl = temp.get(0).getId_iso639_1().trim();
                }
            }
            if (new ConceptHelper().getThisConcept(connect.getPoolConnexion(), idCurl, idTurl) == null) {
                idCurl = null;
                idGurl = null;
                idTurl = null;
                return;
            }
            tree.getSelectedTerme().reInitTerme();

            // Initialisation du thésaurus et de l'arbre
            thesaurus.setId_thesaurus(idTurl);
            thesaurus.setLanguage(idLurl);

            tree.getSelectedTerme().reInitTerme();
            tree.reInit();
            tree.initTree(null, null);
            ThesaurusHelper th = new ThesaurusHelper();

            thesaurus = th.getThisThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            tree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());

            languesTheso = new LanguageHelper().getSelectItemLanguagesOneThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());

            // Initialisation du terme séléctionné et de l'arbre
            int type;
            Concept c = new ConceptHelper().getThisConcept(connect.getPoolConnexion(), idCurl, idTurl);
            if (c.isTopConcept()) {
                type = 2;
            } else {
                type = 3;
            }

            tree.getSelectedTerme().setIdTheso(idTurl);
            tree.getSelectedTerme().setIdlangue(idLurl);

            MyTreeNode mTN = new MyTreeNode(type, idCurl, idTurl, idLurl, c.getIdGroup(), "", null, null, null);
            tree.getSelectedTerme().majTerme(mTN);
            tree.reExpand();
            idCurl = null;
            idGurl = null;
            idTurl = null;
            idLurl = null;
        }
        // if the URL is for Groups 
        if (idGurl != null && idTurl != null) {
            idLurl = Locale.getDefault().toString().substring(0, 2);
            ArrayList<Languages_iso639> temp = new LanguageHelper().getLanguagesOfThesaurus(connect.getPoolConnexion(), idTurl);
            if (temp.isEmpty()) {
                idCurl = null;
                idGurl = null;
                idTurl = null;
                return;
            } else {
                boolean lExist = false;
                for(Languages_iso639 l : temp) {
                    if(l.getId_iso639_1().trim().equals(idLurl)) {
                        lExist = true;
                    }
                }
                if(!lExist) {
                    idLurl = temp.get(0).getId_iso639_1().trim();
                }
            }
            if (new GroupHelper().getThisConceptGroup(connect.getPoolConnexion(), idGurl, idTurl, idLurl) == null) {
                idCurl = null;
                idGurl = null;
                idTurl = null;
                return;
            }
            tree.getSelectedTerme().reInitTerme();

            // Initialisation du thésaurus et de l'arbre
            thesaurus.setId_thesaurus(idTurl);
            thesaurus.setLanguage(idLurl);

            tree.getSelectedTerme().reInitTerme();
            tree.reInit();
            tree.initTree(null, null);
            ThesaurusHelper th = new ThesaurusHelper();

            thesaurus = th.getThisThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            tree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());

            languesTheso = new LanguageHelper().getSelectItemLanguagesOneThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());

            // Initialisation du terme séléctionné et de l'arbre
            int type = 1;
            
       //     NodeGroup nodeGroup = new GroupHelper().getThisConceptGroup(connect.getPoolConnexion(),idGurl, idTurl, idLurl);
            /*
            Concept c = new ConceptHelper().getThisConcept(connect.getPoolConnexion(), idCurl, idTurl);
            if (c.isTopConcept()) {
                type = 2;
            } else {
                type = 3;
            }*/

            tree.getSelectedTerme().setIdTheso(idTurl);
            tree.getSelectedTerme().setIdlangue(idLurl);

            MyTreeNode mTN = new MyTreeNode(type, idGurl, idTurl, idLurl, idGurl, "", null, null, null);
            tree.getSelectedTerme().majTerme(mTN);
            tree.reExpand();
            idCurl = null;
            idGurl = null;
            idTurl = null;
            idLurl = null;
        }
    }

    /**
     * Constructeur
     */
    public SelectedThesaurus() {
        thesaurus = new Thesaurus();
        editTheso = new Thesaurus();
        nodeCG = new NodeGroup();
        ResourceBundle bundlePref = getBundlePref();
        cheminSite = bundlePref.getString("cheminSite");
        version = bundlePref.getString("version");
        String useArk = bundlePref.getString("useArk");
        arkActive = useArk.equals("true");
        serverArk = bundlePref.getString("serverArk");
        workLanguage = bundlePref.getString("workLanguage");
        defaultThesaurusId = bundlePref.getString("defaultThesaurusId");
        identifierType = bundlePref.getString("identifierType");
        
        ResourceBundle bundlePrefArk = getBundlePrefArk();
        idNaan = bundlePrefArk.getString("idNaan");
        
        idCurl = null;
        idGurl = null;        
        idTurl = null;
        idLurl = null;
        arrayTrad = new ArrayList<>();
        
    }

    @PostConstruct
    public void initTheso() {
        // récupération de la langue de travail préférée à partir du fichier de conf (péférences.properties)
        
        if (connect.getPoolConnexion() != null) {
           // langueSource = new UserHelper().getPreferenceUser(connect.getPoolConnexion()).getSourceLang();
            arrayTheso = new ArrayList<>(new ThesaurusHelper().getListThesaurus(connect.getPoolConnexion(), workLanguage).entrySet());
            langues = new LanguageHelper().getSelectItemLanguages(connect.getPoolConnexion());
            //bundlePref.getString("langueSource");
        } else {
            arrayTheso = new ArrayList<>();
        }
        StartDefaultThesauriTree();
    }
    
    /************************************** EDITION THESO BDD **************************************/
    

    /**
     * Cette fonction permet de nettoyer et réorganiser le thésaurus
     * @return 
     */
    public boolean reorganizing() {
        if(thesaurus.getId_thesaurus().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error1")));
            return false;
        }
        try {
            ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);

            if(!thesaurusHelper.reorganizingTheso(conn, thesaurus.getId_thesaurus())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("error.BDD")));
                conn.rollback();
                conn.close();
                return false;
             }                
            conn.commit();
            conn.close();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("theso.infoReorganizing") ));

            regenerateOrphan();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(CurrentUser.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return false;
    }
    
    
    /**
     * Création d'un nouveau thésaurus avec le role
     * @param idUser
     * @param idRole 
     */
    public void ajouterTheso(int idUser, int idRole) {
        if(editTheso.getTitle() == null || editTheso.getTitle().trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error1")));
        } else {
            
            ThesaurusHelper th = new ThesaurusHelper();
            th.setIdentifierType(identifierType);
            

            try {
                Connection conn = connect.getPoolConnexion().getConnection();
                conn.setAutoCommit(false);
            
                String idThesaurus = th.addThesaurusRollBack(conn, editTheso, null, arkActive);
                if(idThesaurus == null){
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error1")));
                    conn.rollback();
                    conn.close();
                    return;
                }
                if(editTheso.getTitle().isEmpty()) {
                     thesaurus.setTitle("theso_" + idThesaurus);
                }
                if(editTheso.getLanguage() == null) {
                    editTheso.setLanguage(workLanguage);
                }
                if(!th.addThesaurusTraductionRollBack(conn, editTheso)) {
                    conn.rollback();
                    conn.close();
                    return;
                 }                
            
                // ajout de role pour le thésaurus à l'utilisateur en cours
                UserHelper userHelper = new UserHelper();
                if(! userHelper.addRole(conn, idUser, idRole, idThesaurus, "")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("error.BDD")));
                    conn.rollback();
                    conn.close();
                    return;                    
                }
                conn.commit();
                conn.close();
            
                
            } catch (SQLException ex) {
                Logger.getLogger(CurrentUser.class.getName()).log(Level.SEVERE, null, ex);
            }
            maj();
            arrayTheso = new ArrayList<>(th.getListThesaurus(connect.getPoolConnexion(), langueSource).entrySet());
            vue.setCreat(false);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("theso.info1.1") +  " " + editTheso.getTitle() + " " + langueBean.getMsg("theso.info1.2")));
            editTheso = new Thesaurus();
        }
       // tree.getSelectedTerme().getUser().getUser().getId();
    }
    
    /**
     * Modification d'un thésaurus
     */
    public void editerTheso() {
        if(editTheso.getTitle() == null || editTheso.getTitle().trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error1")));
        }else {
            new ThesaurusHelper().UpdateThesaurus(connect.getPoolConnexion(), editTheso);
            vue.setEdit(false);
        }
        
    }
    
    public void getAllTables()
    {
        ExportPrivatesDatas exportData = new ExportPrivatesDatas();
        tablesList= exportData.showAllTables(connect.getPoolConnexion());
        tablesListPrivate = exportData.showPrivateTables(connect.getPoolConnexion());
    }

    /**
     * Suppréssion d'un thésaurus
     * @param id 
     */
    public void supprimerTheso(String id) {
        ThesaurusHelper th = new ThesaurusHelper();

        // on vérifie si l'utilisateur n'a plus aucun thésaurus, on garde son dernier role sans thésaurus
        // cette action est temporaire le temps de mettre en place une gestion complète des users et des groupes
        UserHelper userHelper = new UserHelper();
        Connection conn;
        try {
            conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            if(userHelper.isLastThesoOfUser(
                    connect.getPoolConnexion(),
                    tree.getSelectedTerme().getUser().getUser().getId())) {
                if(!userHelper.deleteOnlyTheThesoFromRole(
                        conn, id)){
                    conn.rollback();
                    conn.close();
                    return;                    
                }
            } 
            else {
                if(!userHelper.deleteThisRoleForThisThesaurus(
                        conn, tree.getSelectedTerme().getUser().getUser().getId(),
                        id)){
                    conn.rollback();
                    conn.close();
                    return;
                }
            }
            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(SelectedThesaurus.class.getName()).log(Level.SEVERE, null, ex);
        }

        th.deleteThesaurus(connect.getPoolConnexion(), id);
            
        arrayTheso = new ArrayList<>(th.getListThesaurus(connect.getPoolConnexion(), langueSource).entrySet());
    }
    
    /************************************** EDITION TRAD BDD **************************************/
    
    /**
     * Suppréssion d'un traduction d'un thésaurus
     * @param id l'identifiant du thésaurus
     * @param l la langue de la traduction à supprimer
     */
    public void supprimerTradTheso(String id, String l) {
        new ThesaurusHelper().deleteThesaurusTraduction(connect.getPoolConnexion(), id, l);
        remplirArrayTrad(id);
    }
    
    /**
     * Création d'une traduction d'un thésaurus
     */
    public void ajouterTraduction(){
        if(nomTrad.trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error1")));
        } else {
            vue.setTrad(false);
            String id = editTheso.getId_thesaurus();
            Thesaurus thesoTemp = new Thesaurus();
            thesoTemp.setId_thesaurus(id);
            thesoTemp.setLanguage(langueTrad);
            thesoTemp.setTitle(nomTrad);
            new ThesaurusHelper().addThesaurusTraduction(connect.getPoolConnexion(), thesoTemp);
            remplirArrayTrad(editTheso.getId_thesaurus());
        }
        nomTrad = "";
    }
    
    /************************************** CANDIDATS **************************************/
    
    /**
     * Récupération de la liste des candidats propre au thésaurus courant
     * @return la liste de candidats
     */
    public List<NodeCandidatValue> listeCandidats() {
        List<NodeCandidatValue> candidats = new ArrayList<>();
        if(thesaurus.getId_thesaurus() != null && thesaurus.getLanguage() != null && connect.getPoolConnexion() != null) {
            candidats = new CandidateHelper().getListCandidatsWaiting(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        }
        return candidats;
    }
    
    /**
     * Récupération de la liste des candidats propre au thésaurus courant
     * @return la liste de candidats
     */
    public List<NodeCandidatValue> listeCdtArchives() {
        List<NodeCandidatValue> candidats = new ArrayList<>();
        if(thesaurus.getId_thesaurus() != null && thesaurus.getLanguage() != null) {
            candidats = new CandidateHelper().getListCandidatsArchives(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        }
        return candidats;
    }
    
    /**
     * Récupération de la liste des candidats propre au thésaurus courant
     * @return la liste de candidats
     */
    public List<NodeCandidatValue> listeCdtValid() {
        List<NodeCandidatValue> candidats = new ArrayList<>();
        if(thesaurus.getId_thesaurus() != null && thesaurus.getLanguage() != null) {
            candidats = new CandidateHelper().getListCandidatsValidated(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        }
        return candidats;
    }
    
    /**
     * Récupère la liste des domaines du thésaurus courant
     * @return un tableau de SelectItem contenant les domaine permetant de les 
     * afficher dans une liste déroulante
     */
    public SelectItem[] listeDomaine() {
        ArrayList<NodeGroup> lesDomaines = new GroupHelper().getListConceptGroup(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        SelectItem[] laListe = new SelectItem[lesDomaines.size()+1];
        int i = 1;
        laListe[0] = new SelectItem("", "");
        for(NodeGroup ncg : lesDomaines) {
            laListe[i] = new SelectItem(ncg.getConceptGroup().getIdgroup(), ncg.getLexicalValue() + "(" + ncg.getConceptGroup().getIdgroup() + ")");
            i++;
        }
        return laListe;
    }
    
    /**
     * Création d'un nouveau candidat avec vérification de la valeur en entrée
     */
    public void creerCandidat() {
        if(candidat.getValueEdit() == null || candidat.getValueEdit().trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error2")));
        } else 
            if(new CandidateHelper().isCandidatExist(connect.getPoolConnexion(), candidat.getValueEdit(), thesaurus.getId_thesaurus(), thesaurus.getLanguage())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error3")));
            vue.setAddCandidat(false);

            String idCandidat = new CandidateHelper().getIdCandidatFromTitle(connect.getPoolConnexion(),
                    new StringPlus().addQuotes(candidat.getValueEdit().trim()), thesaurus.getId_thesaurus());
            if(idCandidat == null) {
                cleanEditCandidat();
                return;
            }
            else {
                candidat.getSelected().setIdConcept(idCandidat);
                vue.setAddPropCandidat(true);
               // creerPropCdt();
                return;
            }
        } else if(new TermHelper().isTermExist(connect.getPoolConnexion(), candidat.getValueEdit(), thesaurus.getId_thesaurus(), thesaurus.getLanguage())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error4")));
        } else {
            String temp = candidat.getValueEdit();
            if(! candidat.newCandidat(thesaurus.getId_thesaurus(), thesaurus.getLanguage())){
                return;
            }
            vue.setAddCandidat(false);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("theso.info2.1") +  " " + temp + " " + langueBean.getMsg("theso.info2.2")));
        }
        cleanEditCandidat();
        List<NodeCandidatValue> candidats = new ArrayList<>();
        if(thesaurus.getId_thesaurus() != null && thesaurus.getLanguage() != null && connect.getPoolConnexion() != null) {
            candidats = new CandidateHelper().getListCandidatsWaiting(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        }
    }
    
    public void creerPropCdt() {
        if(!candidat.newPropCandidat(thesaurus.getLanguage())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("error")));
            cleanEditCandidat();
            return;
        }
        vue.setAddPropCandidat(false);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("theso.info3")));
        cleanEditCandidat();
    }
    
    private void cleanEditCandidat(){
        if(candidat != null) {
            candidat.setValueEdit("");
            candidat.setNoteEdit("");
            candidat.setNiveauEdit("");
            candidat.setDomaineEdit("");
        }
    }
    
    public void creerTradCdt() {
        if(candidat.getValueEdit() == null || candidat.getValueEdit().trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error5")));
        }  else {
            for(NodeTraductionCandidat nt : candidat.getInfoCdt().getNodeTraductions()) {
                if(nt.getIdLang().trim().equals(candidat.getLangueEdit().trim())) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error6")));
                    return;
                }
            }
            String temp = candidat.getValueEdit();
            if(!candidat.newTradCdt(thesaurus.getId_thesaurus(), thesaurus.getLanguage())) {
                return;
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("theso.info4.1") +  " " + temp + " " + langueBean.getMsg("theso.info4.2")));
        }
        candidat.setValueEdit("");
        candidat.setLangueEdit("");
    }
    
    /**
     * Met à jour le candidat courant lors d'une sélection dans la table des candidats
     */
    public void majCdt() {
        candidat.maj(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
    }
    
    /************************************** MISE A JOUR **************************************/
    
    /**
     * Met à jour le thésaurus courant lors d'un changement de thésaurus
     */
    public void maj() {
        tree.getSelectedTerme().reInitTerme();
        tree.reInit();
        tree.initTree(null, null);
        statBean.reInit();
        ThesaurusHelper th = new ThesaurusHelper();
        nodePreference = tree.getSelectedTerme().getUser().getThesaurusPreferences(thesaurus.getId_thesaurus(), workLanguage);
        if(th.getThisThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), nodePreference.getSourceLang()) != null) {
            thesaurus = th.getThisThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), nodePreference.getSourceLang());
            tree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            tree.setIdThesoSelected(thesaurus.getId_thesaurus());
            tree.setDefaultLanguage(thesaurus.getLanguage());
        } else {
            thesaurus.setLanguage("");
        }
        uTree.reInit();
        uTree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        languesTheso = new LanguageHelper().getSelectItemLanguagesOneThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        candidat.maj(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        vue.setCreat(false);
    }
    
        /**
     * Met à jour le thésaurus courant lors d'un changement de thésaurus
     */
    public void StartDefaultThesauriTree() {
        if(connect.getPoolConnexion() == null){
            System.err.println("!!!!! Opentheso n'a pas pu se connecter à la base de données !!!!!!! ");
            return;
        }
        tree.getSelectedTerme().reInitTerme();
        tree.reInit();
        tree.initTree(null, null);
        statBean.reInit();
        uTree.reInit();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        if(thesaurusHelper.isThesaurusExiste(connect.getPoolConnexion(), defaultThesaurusId)) {
            thesaurus = thesaurusHelper.getThisThesaurus(connect.getPoolConnexion(), defaultThesaurusId, workLanguage);
            if(thesaurus == null) return;
            tree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            uTree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            languesTheso = new LanguageHelper().getSelectItemLanguagesOneThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            candidat.maj(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            vue.setCreat(false);
            tree.getSelectedTerme().getUser().getThesaurusPreferences(thesaurus.getId_thesaurus(), workLanguage);
        }
    }
    
    /**
     * Met à jour le thésaurus courant lors d'un changement de langue
     */
    public void update() {
        tree.getSelectedTerme().reInitTerme();
        tree.reInit();
        tree.initTree(null, null);
        statBean.reInit();
        thesaurus = new ThesaurusHelper().getThisThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        tree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        uTree.reInit();
        uTree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        languesTheso = new LanguageHelper().getSelectItemLanguagesOneThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        candidat.maj(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        vue.setCreat(false);
    }
    
/***************************************** FACETTES *****************************************/
    
    /**
     * Insère une facette dans le thésaurus
     * @param idParent 
     * @param editFacette 
     */
    public void creerFacette(String idParent, String editFacette) {
            new FacetHelper().addNewFacet(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), idParent, editFacette, thesaurus.getLanguage(), "");
            uTree.reInit();
            uTree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            vue.setFacette(false);
            vue.setOnglet(3);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", editFacette + " " + langueBean.getMsg("theso.info4.2")));
    }
    
    /**
     * Supprime une facette
     */
    public void delFacette() {
        new FacetHelper().deleteFacet(connect.getPoolConnexion(), Integer.parseInt(idEdit), thesaurus.getId_thesaurus());
        uTree.reInit();
        uTree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        vue.setFacette(false);
        vue.setOnglet(3);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("theso.info5")));
    }
    
    /**
     * Ajoute une traduction pour une facette
     */
    public void tradFacette() {
        if(valueEdit == null || valueEdit.trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error5")));
        } else {
            new FacetHelper().addFacetTraduction(connect.getPoolConnexion(), Integer.parseInt(idEdit), thesaurus.getId_thesaurus(), valueEdit, langueEdit.trim());
            valueEdit = "";
            vue.setFacette(false);
            vue.setOnglet(3);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("theso.info6")));
        }
    }
    
    /**
     * Change une traduction pour une facette
     */
    public void updateFacette() {
        if(valueEdit == null || valueEdit.trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error8")));
        } else {
            new FacetHelper().updateFacetTraduction(connect.getPoolConnexion(), Integer.parseInt(idEdit), thesaurus.getId_thesaurus(), thesaurus.getLanguage(), valueEdit);
            valueEdit = "";
            uTree.reInit();
            uTree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            vue.setFacette(false);
            vue.setOnglet(3);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("theso.info7")));
        }
    }
    
    /***************************************** AUTRE *****************************************/
    
    /**
     *  Récupère le thésaurus courant
     * @return le thésaurus s'il existe, null sinon
     */
    public Thesaurus getThisTheso() {
        String idTemp = "", lTemp = "";
        if(thesaurus.getLanguage() == null || thesaurus.getId_thesaurus() == null) return null;
        if(thesaurus.getLanguage() != null) lTemp = thesaurus.getLanguage();
        if(thesaurus.getId_thesaurus() != null) idTemp = thesaurus.getId_thesaurus();
        Thesaurus tempT = new ThesaurusHelper().getThisThesaurus(connect.getPoolConnexion(), idTemp, lTemp);
        return tempT;
    }
    
    /**
     * Cette fonction permet de regénérer les Orphelins
     * @return 
     */
    public boolean regenerateOrphan() {
        if(thesaurus.getLanguage() == null || thesaurus.getId_thesaurus() == null) return false;
        
        if(!new ToolsHelper().orphanDetect(connect.getPoolConnexion(), thesaurus.getId_thesaurus())){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error"),  "error"));
            vue.setRegenerateOrphan(0);
            return false;
        }
        return true;
    }
    
    
    /**
     * Affiche le thésaurus en cour d'édition (pas le thésaurus courant)
     * @param type
     * @param nom
     * @param langue
     * @param id 
     */
    public void afficheEditTheso(int type, String nom, String langue, String id) {
        if(type == 1) {
             vue.setCreat(false);
             vue.setEdit(false);
             vue.setTrad(false);
             vue.setLanguage(true);
             editTheso.setId_thesaurus(id);
             editTheso.setTitle(nom);
             remplirArrayTrad(id);
        } else {
            vue.setEdit(true);
            remplirEditTheso(id, langue);
        }
    }
    
    /**
     * Met à jour le thésaurus courant, l'arbre et le terme courant lors de la 
     * sélection de la traduction d'un terme dans le gestionnaire de thésaurus
     * @param id
     * @param l
     * @param type 
     */
    public void changeTermeTrad(String id, String l, int type) {
        MyTreeNode mTN = new MyTreeNode(type, id, tree.getSelectedTerme().getIdTheso(), l, tree.getSelectedTerme().getIdDomaine(), tree.getSelectedTerme().getIdTopConcept(), null, null, null);
        tree.getSelectedTerme().majTerme(mTN);
        thesaurus = new ThesaurusHelper().getThisThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), l);
        tree.reInit();
        tree.reExpand();
        vue.setOnglet(0);
    }
    
    /**
     * Récupération des traductions du thésaurus
     * @return une liste des traductions
     */
    public ArrayList<Languages_iso639> getThisTrad() {
        thesaurus.getLanguage();
        ArrayList<Languages_iso639> languages_iso639s = new ArrayList<>();
        
        ArrayList<Languages_iso639> languages_iso639s_temp = new LanguageHelper().getLanguagesOfThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus());
        
        // on replace la langue sélectionnée en premier
        for (Languages_iso639 languages_iso639 : languages_iso639s_temp) {
            if(languages_iso639.getId_iso639_1().equalsIgnoreCase(thesaurus.getLanguage()))
                languages_iso639s.add(0, languages_iso639);
            else
                languages_iso639s.add(languages_iso639);
        }
        return languages_iso639s;
        //return (new LanguageHelper().getLanguagesOfThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus()));
    }
    
    /**
     * Récupération des traductions du thésaurus
     * @return une liste des traductions
     */
    public ArrayList<Languages_iso639> getTradForSearch() {
        thesaurus.getLanguage();
        ArrayList<Languages_iso639> languages_iso639s = new ArrayList<>();
        
        ArrayList<Languages_iso639> languages_iso639s_temp = new LanguageHelper().getLanguagesOfThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus());
        
        // on replace la langue sélectionnée en premier
        for (Languages_iso639 languages_iso639 : languages_iso639s_temp) {
            if(languages_iso639.getId_iso639_1().equalsIgnoreCase(thesaurus.getLanguage()))
                languages_iso639s.add(0, languages_iso639);
            else
                languages_iso639s.add(languages_iso639);
        }
        Languages_iso639 languages_iso639_all = new Languages_iso639();
        languages_iso639_all.setFrench_name("");
        languages_iso639_all.setEnglish_name("");
        languages_iso639_all.setId_iso639_1("");
        languages_iso639_all.setId_iso639_1("");
        languages_iso639s.add(languages_iso639_all);
        return languages_iso639s;
        //return (new LanguageHelper().getLanguagesOfThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus()));
    }    
    
    public void remplirEditTheso(String id, String langue) {
        editTheso = new ThesaurusHelper().getThisThesaurus(connect.getPoolConnexion(), id, langue);
    }
    
    public void remplirArrayTrad(String id) {
        arrayTrad = new ArrayList<>(new ThesaurusHelper().getMapTraduction(connect.getPoolConnexion(), id).entrySet());
    }
    
    /**
     * Création d'un domaine avec mise à jour dans l'arbre
     */
    public void ajouterDomaine() {
        if(nodeCG.getLexicalValue().trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error7")));
        } else {
            String temp = nodeCG.getLexicalValue();
            nodeCG.setIdLang(thesaurus.getLanguage());
            nodeCG.getConceptGroup().setIdthesaurus(thesaurus.getId_thesaurus());
            GroupHelper cgh = new GroupHelper();
            int idUser = tree.getSelectedTerme().getUser().getUser().getId();
            cgh.addGroup(connect.getPoolConnexion(), nodeCG, cheminSite, arkActive, idUser);
            nodeCG = new NodeGroup();
            vue.setSelectedActionDom(PropertiesNames.noActionDom);
            tree.reInit();
            tree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", temp + " " + langueBean.getMsg("theso.info1.2")));
        }
    }
    
    /**
     * Création d'un domaine avec mise à jour dans l'arbre
     */
    public void deleteDomaine() {
        GroupHelper groupHelper = new GroupHelper();
        int idUser = tree.getSelectedTerme().getUser().getUser().getId();
        String temp = nodeCG.getLexicalValue();
        if(!groupHelper.isEmptyDomain(connect.getPoolConnexion(),
                nodeCG.getConceptGroup().getIdgroup(), 
                nodeCG.getConceptGroup().getIdthesaurus())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("theso.error7")));
            return;
        }
        groupHelper.deleteGroup(connect.getPoolConnexion(),
                nodeCG.getConceptGroup().getIdgroup(),
                nodeCG.getConceptGroup().getIdthesaurus(),
                idUser);
        vue.setSelectedActionDom(PropertiesNames.noActionDom);
        nodeCG = new NodeGroup();
        tree.reInit();
        tree.initTree(thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", temp + " " + langueBean.getMsg("theso.info1.2")));
    }    
    
    /**
     * Vérifie si le thésaurus courant a des traductions
     * @return  true s'il en a au moins une, false sinon
     */
    public boolean haveTrad() {
        return (languesTheso.length > 0);
    }
    
    /************************************** GETTERS SETTERS **************************************/
    
    /**
     * 
     * @return 
     */
    public String getMetaData() {
        if(this.tree.getSelectedTerme() != null ) {
            if(this.tree.getSelectedTerme().getIdC() != null ) {
                if(this.tree.getSelectedTerme().getIdTheso() != null ) {
                    // cas d'un domaine ou  Groupe
                    if(tree.getSelectedTerme().getType() == 1) {
                        ExportFromBDD exportFromBDD = new ExportFromBDD();
                        exportFromBDD.setServerArk(serverArk);
                        exportFromBDD.setServerAdress(cheminSite);
                        StringBuffer skos = exportFromBDD.exportThisGroup(
                                connect.getPoolConnexion(), 
                                this.tree.getSelectedTerme().getIdTheso(),
                                this.tree.getSelectedTerme().getIdC());

                    JsonHelper jsonHelper = new JsonHelper();
                    SKOSXmlDocument sKOSXmlDocument = jsonHelper.readSkosDocument(skos);
                    StringBuffer jsonLd = jsonHelper.getJsonLdForSchemaOrg(sKOSXmlDocument);
                    if(jsonLd != null)
                        return jsonLd.toString();
                    else
                        return "";
                }
                // cas d'un concept
                if(tree.getSelectedTerme().getType() == 2 || tree.getSelectedTerme().getType() == 3) {
                    ExportFromBDD exportFromBDD = new ExportFromBDD();
                    exportFromBDD.setServerArk(serverArk);
                    exportFromBDD.setServerAdress(cheminSite);
                    StringBuffer skos = exportFromBDD.exportConcept(
                            connect.getPoolConnexion(), 
                            this.tree.getSelectedTerme().getIdTheso(),
                            this.tree.getSelectedTerme().getIdC());
                    if(skos == null) return "";

                        JsonHelper jsonHelper = new JsonHelper();
                        SKOSXmlDocument sKOSXmlDocument = jsonHelper.readSkosDocument(skos);
                        if(sKOSXmlDocument == null) return "";
                        StringBuffer jsonLd = jsonHelper.getJsonLdForSchemaOrg(sKOSXmlDocument);
                        if(jsonLd != null)
                            return jsonLd.toString();
                        else
                            return "";
                    }
                }
            }
        }
        
        // Envoye les domaines (MT)
        if(thesaurus.getId_thesaurus() != null) {
            if(!thesaurus.getId_thesaurus().trim().isEmpty()) {
                ExportFromBDD exportFromBDD = new ExportFromBDD();
                exportFromBDD.setServerArk(serverArk);
                exportFromBDD.setServerAdress(cheminSite);
                StringBuffer skos = exportFromBDD.exportGroupsOfThesaurus(
                        connect.getPoolConnexion(), 
                        thesaurus.getId_thesaurus());

                JsonHelper jsonHelper = new JsonHelper();
                SKOSXmlDocument sKOSXmlDocument = jsonHelper.readSkosDocument(skos);
                StringBuffer jsonLd = jsonHelper.getJsonLdForSchemaOrgForConceptScheme(sKOSXmlDocument);
                if(jsonLd == null) return "";
                return jsonLd.toString();
            }
        }
        return "";        
        
        // ancienne version 
/*        if(this.tree.getSelectedTerme() != null ) {
            if(this.tree.getSelectedTerme().getIdC() != null ) {
                if(this.tree.getSelectedTerme().getIdTheso() != null ) {
                    // cas d'un domaine ou  Groupe
                    if(tree.getSelectedTerme().getType() == 1) {
                        ExportFromBDD exportFromBDD = new ExportFromBDD();
                        exportFromBDD.setServerArk(serverArk);
                        exportFromBDD.setServerAdress(cheminSite);
                        StringBuffer skos = exportFromBDD.exportThisGroup(
                                connect.getPoolConnexion(), 
                                this.tree.getSelectedTerme().getIdTheso(),
                                this.tree.getSelectedTerme().getIdC());

                    JsonHelper jsonHelper = new JsonHelper();
                    SKOSXmlDocument sKOSXmlDocument = jsonHelper.readSkosDocument(skos);
                    StringBuffer jsonLd = jsonHelper.getJsonLd(sKOSXmlDocument);
                    return jsonLd.toString();
                }
                // cas d'un concept
                if(tree.getSelectedTerme().getType() == 2 || tree.getSelectedTerme().getType() == 3) {
                    ExportFromBDD exportFromBDD = new ExportFromBDD();
                    exportFromBDD.setServerArk(serverArk);
                    exportFromBDD.setServerAdress(cheminSite);
                    StringBuffer skos = exportFromBDD.exportConcept(
                            connect.getPoolConnexion(), 
                            this.tree.getSelectedTerme().getIdTheso(),
                            this.tree.getSelectedTerme().getIdC());

                        JsonHelper jsonHelper = new JsonHelper();
                        SKOSXmlDocument sKOSXmlDocument = jsonHelper.readSkosDocument(skos);
                        StringBuffer jsonLd = jsonHelper.getJsonLd(sKOSXmlDocument);
                        return jsonLd.toString();
                    }
                }
            }
        }
        
        // Envoye les domaines (MT)
        if(thesaurus.getId_thesaurus() != null) {
            if(!thesaurus.getId_thesaurus().trim().isEmpty()) {
                ExportFromBDD exportFromBDD = new ExportFromBDD();
                exportFromBDD.setServerArk(serverArk);
                exportFromBDD.setServerAdress(cheminSite);
                StringBuffer skos = exportFromBDD.exportGroupsOfThesaurus(
                        connect.getPoolConnexion(), 
                        thesaurus.getId_thesaurus());

                JsonHelper jsonHelper = new JsonHelper();
                SKOSXmlDocument sKOSXmlDocument = jsonHelper.readSkosDocument(skos);
                StringBuffer jsonLd = jsonHelper.getJsonLdForConceptScheme(sKOSXmlDocument);
                if(jsonLd == null) return "";
                return jsonLd.toString();
            }
        }
        return ""; */
    }
    
    public String getVersion(){
        return version;
    }
    
    public String getCheminSite() {
        return cheminSite;
    }

    public String getServerArk() {
        return serverArk;
    }
   
    
    public TreeBean getTree() {
        return tree;
    }

    public void setTree(TreeBean tree) {
        this.tree = tree;
    }

    public Connexion getConnect() {
        return connect;
    }

    public void setConnect(Connexion connect) {
        this.connect = connect;
    }
    
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ArrayList<Entry<String, String>> getArrayTheso() {
        if(connect.getPoolConnexion() != null) {
         /*   Map p = new ThesaurusHelper().getListThesaurus(connect.getPoolConnexion(), langueSource);
            p.put("", "");*/
            if(tree.getSelectedTerme().getUser().getLangSourceEdit() == null || tree.getSelectedTerme().getUser().getLangSourceEdit().equalsIgnoreCase("")) 
                arrayTheso = new ArrayList<>(new ThesaurusHelper().getListThesaurus(connect.getPoolConnexion(),workLanguage).entrySet());
            else
                arrayTheso = new ArrayList<>(new ThesaurusHelper().getListThesaurus(
                        connect.getPoolConnexion(), tree.getSelectedTerme().getUser().getLangSourceEdit()).entrySet());
        }
        return arrayTheso;
    }
    
    /**
     * Permet de retourner la liste des thésaurus autorisée pour un role
     * @return 
     */
    public ArrayList<Entry<String, String>> getAuthorizedThesaurus() {
        if(connect.getPoolConnexion() != null) {
         /*   Map p = new ThesaurusHelper().getListThesaurus(connect.getPoolConnexion(), langueSource);
            p.put("", "");*/
         
            if(tree.getSelectedTerme().getUser().getUser().getIdRole() == 1){// cas d'un SuperAdmin
                if(tree.getSelectedTerme().getUser().getLangSourceEdit() == null || tree.getSelectedTerme().getUser().getLangSourceEdit().equalsIgnoreCase("")) 
                    arrayTheso = new ArrayList<>(new ThesaurusHelper().getListThesaurus(connect.getPoolConnexion(),
                             workLanguage).entrySet());
                else
                    arrayTheso = new ArrayList<>(new ThesaurusHelper().getListThesaurus(
                            connect.getPoolConnexion(),
                            tree.getSelectedTerme().getUser().getLangSourceEdit()).entrySet());            
            }
            else {
                if(tree.getSelectedTerme().getUser().getLangSourceEdit() == null || tree.getSelectedTerme().getUser().getLangSourceEdit().equalsIgnoreCase("")) 
                    arrayTheso = new ArrayList<>(new ThesaurusHelper().getListThesaurusOfUser(connect.getPoolConnexion(),
                            tree.getSelectedTerme().getUser().getUser().getId(),
                            workLanguage).entrySet());
                else
                    arrayTheso = new ArrayList<>(new ThesaurusHelper().getListThesaurusOfUser(
                            connect.getPoolConnexion(),
                            tree.getSelectedTerme().getUser().getUser().getId(),
                            tree.getSelectedTerme().getUser().getLangSourceEdit()).entrySet());                 
            }
            

        }
        return arrayTheso;
    }    

    public void setArrayTheso(ArrayList<Entry<String, String>> arrayTheso) {
        this.arrayTheso = arrayTheso;
    }

    public Thesaurus getEditTheso() {
        return editTheso;
    }

    public void setEditTheso(Thesaurus editTheso) {
        this.editTheso = editTheso;
    }

    public void setVueCreat(boolean creat) {
        vue.setCreat(creat);
        if(creat) {
            editTheso = new Thesaurus();
        }
    }

    public ArrayList<Entry<String, String>> getArrayTrad() {
        return arrayTrad;
    }

    public void setArrayTrad(ArrayList<Entry<String, String>> arraytrad) {
        this.arrayTrad = arraytrad;
    }

    public SelectItem[] getLangues() {
        return langues;
    }   

    public String getLangueTrad() {
        return langueTrad;
    }

    public void setLangueTrad(String langueTrad) {
        this.langueTrad = langueTrad;
    }

    public String getNomTrad() {
        return nomTrad;
    }

    public void setNomTrad(String nomTrad) {
        this.nomTrad = nomTrad;
    }

    public Vue getVue() {
        return vue;
    }

    public void setVue(Vue vue) {
        this.vue = vue;
    }

    public NodeGroup getNodeCG() {
        return nodeCG;
    }

    public void setNodeCG(NodeGroup nodeCG) {
        this.nodeCG = nodeCG;
    }

    public SelectItem[] getLanguesTheso() {
        return languesTheso;
    }

    public void setLanguesTheso(SelectItem[] languesTheso) {
        this.languesTheso = languesTheso;
    }

    public List<NodeCandidatValue> getFilteredCandidats() {
        return filteredCandidats;
    }

    public void setFilteredCandidats(List<NodeCandidatValue> filteredCandidats) {
        this.filteredCandidats = filteredCandidats;
    }

    public List<NodeCandidatValue> getFilteredCandidatsV() {
        return filteredCandidatsV;
    }

    public void setFilteredCandidatsV(List<NodeCandidatValue> filteredCandidatsV) {
        this.filteredCandidatsV = filteredCandidatsV;
    }

    public List<NodeCandidatValue> getFilteredCandidatsA() {
        return filteredCandidatsA;
    }

    public void setFilteredCandidatsA(List<NodeCandidatValue> filteredCandidatsA) {
        this.filteredCandidatsA = filteredCandidatsA;
    }

    public SelectedCandidat getCandidat() {
        return candidat;
    }

    public void setCandidat(SelectedCandidat candidat) {
        this.candidat = candidat;
    }

    public UnderTree getuTree() {
        return uTree;
    }

    public void setuTree(UnderTree uTree) {
        this.uTree = uTree;
    }

    public String getIdCurl() {
        return idCurl;
    }

    public void setIdCurl(String idCurl) {
        this.idCurl = idCurl;
    }

    public String getIdTurl() {
        return idTurl;
    }

    public void setIdTurl(String idTurl) {
        this.idTurl = idTurl;
    }

    public String getIdLurl() {
        return idLurl;
    }

    public void setIdLurl(String idLurl) {
        this.idLurl = idLurl;
    }

    public String getIdGurl() {
        return idGurl;
    }

    public void setIdGurl(String idGurl) {
        this.idGurl = idGurl;
    }
    
    public LanguageBean getLangueBean() {
        return langueBean;
    }

    public void setLangueBean(LanguageBean langueBean) {
        this.langueBean = langueBean;
    }

    public StatBean getStatBean() {
        return statBean;
    }

    public void setStatBean(StatBean statBean) {
        this.statBean = statBean;
    }

    public ArrayList<Entry<String, String>> getArrayFacette() {
        if(connect.getPoolConnexion() != null) {
            ArrayList<NodeFacet> temp = new FacetHelper().getAllFacetsOfThesaurus(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
            Map<String, String> mapTemp = new HashMap<>();
            for(NodeFacet nf : temp) {
                String value = new TermHelper().getThisTerm(connect.getPoolConnexion(), nf.getIdConceptParent(), thesaurus.getId_thesaurus(), thesaurus.getLanguage()).getLexical_value();
                mapTemp.put(String.valueOf(nf.getIdFacet()), nf.getLexicalValue() + " (" + value  + ")");
            }
            arrayFacette = new ArrayList<>(mapTemp.entrySet());
        }
        return arrayFacette;
    }

    public void setArrayFacette(ArrayList<Entry<String, String>> arrayFacette) {
        this.arrayFacette = arrayFacette;
    }

    public String getIdEdit() {
        return idEdit;
    }

    public void setIdEdit(String idEdit) {
        this.idEdit = idEdit;
    }

    public String getLangueEdit() {
        return langueEdit;
    }

    public void setLangueEdit(String langueEdit) {
        this.langueEdit = langueEdit;
    }
    
    public String getValueEdit() {
        return valueEdit;
    }

    public void setValueEdit(String valueEdit) {
        this.valueEdit = valueEdit;
    }

    public ConceptBean getConceptbean() {
        return conceptbean;
    }

    public void setConceptbean(ConceptBean conceptbean) {
        this.conceptbean = conceptbean;
    }

    public String getIdNaan() {
        return idNaan;
    }

    public void setIdNaan(String idNaan) {
        this.idNaan = idNaan;
    }

    public ArrayList<String> getTablesList() {
        return tablesList;
    }

    public void setTablesList(ArrayList<String> tablesList) {
        this.tablesList = tablesList;
    }

    public ArrayList<String> getTablesListPrivate() {
        return tablesListPrivate;
    }

    public void setTablesListPrivate(ArrayList<String> tablesListPrivate) {
        this.tablesListPrivate = tablesListPrivate;
    }

    public ArrayList<Table> getSortirXml() {
        return sortirXml;
    }

    public void setSortirXml(ArrayList<Table> sortirXml) {
        this.sortirXml = sortirXml;
    }

    public ArrayList <String> getValuesOfTables () {
        ArrayList <String> values = new ArrayList<>();
        for (Table tables : sortirXml) {
            for (LineOfData line : tables.getLineOfDatas()) {
                values.add(line.getValue());
            }
            
        }
        return values;
    }

    public String getNomdufichier() {
        return nomdufichier;
    }

    public void setNomdufichier(String nomdufichier) {
        this.nomdufichier = nomdufichier;
    }
    public StreamedContent genererdocument() throws SQLException
    {
        ExportStatistiques expo= new ExportStatistiques();
        expo.recuperatefils(connect.getPoolConnexion(), thesaurus.getId_thesaurus(), thesaurus.getLanguage());
        InputStream stream;
        java.util.Date datetoday = new java.util.Date();

        try {
            stream = new ByteArrayInputStream(expo.getDocument().getBytes("UTF-8"));
            file = new DefaultStreamedContent(stream, "application/xml", "export "+thesaurus.getId_thesaurus() + datetoday + ".txt");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DownloadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return file;
    }   
    
}
