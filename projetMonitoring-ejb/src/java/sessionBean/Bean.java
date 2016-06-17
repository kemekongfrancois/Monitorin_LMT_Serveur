/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import entite.Machine;
import entite.Tache;
import entite.TachePK;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import until.Until;

/**
 *
 * @author KEF10
 */
@Stateless
public class Bean {

    public static final String OSWINDOWS = "Windows";
    public static final String OSLinux = "Linux";
    public static final String OK = "ok";
    public static final String DEFAUL_PERIODE_CHECK_MACHINE = "1 * * * * ?";//represente la valeur par defaut de la période de check des machine 
    public static final String PAUSE = "PAUSE";
    public static final String START = "START";
    public static final String STOP = "STOP";
    public static final int SEUIL_ALERT_DD = 90;
    public static final String TACHE_DD = "surveiller_dd";

    @PersistenceContext(unitName = "projetMonitoring-ejbPU")
    private EntityManager em;

    public String persist(Object object) {
        try {
            em.persist(object);
            return OK;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, object+": impossible d'écrire cet objet dans la BD", e);
            //Until.savelog("impossible d'écrire dans la BD \n" + e, Until.fichieLog);
            return "ko" + e;
        }
    }

    /**
     * si ladresse ip es déja donnée à une machine on retourne null
     * @param AdresIP
     * @param port
     * @param periodeCheck
     * @param nonOS
     * @param nomMachine
     * @return 
     */
    public Machine creerMachine(String AdresIP, String port, String periodeCheck, String nonOS, String nomMachine) {
        Machine machine = getMachine(AdresIP);
        if(machine!=null){//l'adresse ip es déja utilisé
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE,AdresIP+ ": cette adresse es déja utilisé");
            return null;
        }
        machine = new Machine();
        machine.setAdresseIP(AdresIP);
        machine.setPortEcoute(port);
        machine.setNomMachine(nomMachine);
        machine.setPeriodeDeCheck(periodeCheck);
        machine.setTypeOS(nonOS);
        persist(machine);//on creer la machine dans la BD
        return getMachine(AdresIP);
    }

    /**
     *
     * @param AdresIP
     * @return null en cas de pb
     */
    public Machine getMachine(String AdresIP) {
        Query query = em.createNamedQuery("Machine.findByAdresseIP");
        query.setParameter("adresseIP", AdresIP);
        if (query.getResultList().isEmpty()) {
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING,"machine inexistante");
            return null;
        } else {
            return (Machine) query.getSingleResult();
        }
    }

    /**
     * cette fonction retourne la machine donc les caractéristique sont pris en
     * paramètre si la machine n'existe pas on là créer
     *
     * @param adresIP
     * @param port
     * @param periodeCheck
     * @param nonOS
     * @param nomMachine
     * @return null en cas de pb
     */
    public Machine verifiOuCreerMachine(String adresIP, String port, String periodeCheck, String nonOS, String nomMachine) {
        Machine machine = getMachine(adresIP);
        if (machine == null) {//la machine n'existe pas on la créer
            return creerMachine(adresIP, port, periodeCheck, nonOS, nomMachine);//on créer l'objet dans la BD
        } else {//la machine existe on l'a retourne
            return machine;
        }
    }

    public Tache getTache(int IdMachine, String cleTache) {
        TachePK cle = new TachePK(IdMachine, cleTache);
        Tache tache = em.find(Tache.class, cle);
        if (tache==null){
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING,"Tache inexistante");
            return null;
        }else{
            return tache;
        }
         
    }
    
    private Tache enregModification(Tache tache){
        try {
            Tache tacheModifier = em.merge(tache);
            Logger.getLogger(Bean.class.getName()).log(Level.INFO,  "les modification apporté à la tache a bien été effectué "+tacheModifier);
            return tacheModifier;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE,  "impossible d'enregistrer les modifications apporté à la tache",e);
            return null;
        }
    }
    
    public Tache traitementAlerte(Tache tache){
        switch(tache.getTypeTache()){
            case TACHE_DD: 
                tache.setStatue(PAUSE);
                return enregModification(tache);
                //break;
            default:
                Logger.getLogger(Bean.class.getName()).log(Level.WARNING, tache.getTypeTache() + ": ce type n'es pas reconnue ");
                return null;
        }
    }

    /**
     * retourne la liste des tache d'une machine donc l'adresse es prise en
     * paramettre
     *
     * @param ipAdresse
     * @return null en cas de pb
     */
    public List<Tache> getListTacheMachine(String ipAdresse) {
        Machine machine = getMachine(ipAdresse);
        if (machine != null) {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO,"la liste des machines es envoyé: nombre de machine="+machine.getTacheList().size());
            return machine.getTacheList();
        } else {
            return null;
        }
    }

    public Tache creerTacheSurveilleDD(String adresIpMachine, String periodeVerrification, String lettre_partition, int seuil, String statue) {
        Machine machine = getMachine(adresIpMachine);
        
        return creerTache(adresIpMachine, TACHE_DD, null, periodeVerrification, lettre_partition, seuil, statue,null);
    }

    private Tache creerTache(String adresIpMachine, String typeTache, String description_fichier, String periodeVerrification, String cleTache, int seuil, String statue, String liste_adresse) {
        Tache tacheDD = new Tache();
        Machine machine = getMachine(adresIpMachine);
        if (machine == null) {
            return null;
        } else {//la machine existe on creer la tache
            if(getTache(machine.getIdMachine(), cleTache)!=null){//cette tache existait déja
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE,cleTache+ ": cette tache existe déja sur la machine: "+adresIpMachine);
            return null;
            }
            tacheDD.setTypeTache(typeTache);
            tacheDD.setTachePK(new TachePK(machine.getIdMachine(), cleTache));
            tacheDD.setSeuilAlerte(seuil);
            tacheDD.setPeriodeVerrification(periodeVerrification);
            tacheDD.setStatue(statue);
            tacheDD.setDescriptionFichier(description_fichier);
            tacheDD.setListeAdresse(liste_adresse);
            persist(tacheDD);//on enregistre la tache dans la BD
            return getTache(machine.getIdMachine(), cleTache);
        }
    }

}
