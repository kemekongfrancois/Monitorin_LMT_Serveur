/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import entite.Machine;
import entite.Tache;
import entite.TachePK;
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
    public static final String OSLinux= "Linux";
    public static final String OK = "ok";
    public static final String TACHE_DD = "surveiller_dd";
            

    @PersistenceContext(unitName = "projetMonitoring-ejbPU")
    private EntityManager em;

    public String persist(Object object) {
        try {
            em.persist(object);
            return OK;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, e);
            Until.savelog("impossible d'écrire dans la BD \n"+e, Until.fichieLog);
            return "ko"+ e;
        }
    }

 
    public Machine creerMachine(String AdresIP,String port,String periodeCheck, String nonOS, String nomMachine) {
        Machine machine = new Machine();
        machine.setAdresseIP(AdresIP);
        machine.setPortEcoute(port);
        machine.setNomMachine(nomMachine);
        machine.setPeriodeDeCheck(periodeCheck);
        machine.setTypeOS(nonOS);
        persist(machine);//on creer la machine dans la BD
        return getMachine(AdresIP);
    }
    
    private Machine getMachine(String AdresIP){
        Query query = em.createNamedQuery("Machine.findByAdresseIP");
        query.setParameter("adresseIP", AdresIP);
        if(query.getResultList().isEmpty()){
            return null;
        }else{
            return (Machine) query.getSingleResult();
        }
    }
    
    /**
     * cette fonction retourne la machine donc les caractéristique sont pris en paramètre
     * si la machine n'existe pas on là créer
     * @param adresIP
     * @param port
     * @param periodeCheck
     * @param nonOS
     * @param nomMachine
     * @return null en cas de pb
     */
    public Machine verifiOuCreerMachine(String adresIP,String port,String periodeCheck, String nonOS, String nomMachine){
        Machine machine = getMachine(adresIP);
        if(machine==null){//la machine n'existe pas on la créer
            return creerMachine(adresIP,port,periodeCheck, nonOS, nomMachine);//on créer l'objet dans la BD
        }else{//la machine existe on l'a retourne
            return machine;
        }   
    }
    
    private Tache getTache(int IdMachine, String cleTache){
        TachePK cle = new TachePK(IdMachine, cleTache);
        return em.find(Tache.class, cle);
    }
    
    public Tache creerTacheSurveilleDD(String adresIpMachine, String periodeVerrification, String lettre_partition,int seuil, String statue){
        Tache tacheDD = new Tache();
        Machine machine = getMachine(adresIpMachine);
        if(machine==null){
            String msg = "machine inexistante";
            System.out.println(msg);
            return null;
        }else{//la machine existe on creer la tache
            tacheDD.setTypeTache(TACHE_DD);
            tacheDD.setTachePK(new TachePK(machine.getIdMachine(), lettre_partition));
            tacheDD.setSeuilAlerte(seuil);
            tacheDD.setPeriodeVerrification(periodeVerrification);
            tacheDD.setStatue(statue);
            
            persist(tacheDD);//on enregistre la tache dans la BD
            return getTache(machine.getIdMachine(), lettre_partition);
        }    
    }  
}
