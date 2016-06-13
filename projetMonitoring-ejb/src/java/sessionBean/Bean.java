/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import entite.Machine;
import entite.SurveillerDd;
import entite.SurveillerDdPK;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author KEF10
 */
@Stateless
public class Bean {
    public static String OSWINDOWS = "Windows";
    public static String OSLinux= "Linux";
    public static String OK = "ok";
            

    @PersistenceContext(unitName = "projetMonitoring-ejbPU")
    private EntityManager em;

    public String persist(Object object) {
        try {
            em.persist(object);
            return OK;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, e);
            return "ko"+ e;
        }
    }

 
    public Machine creerMachine(String AdresIP,String port,int periodeCheck, String nonOS, String nomMachine) {
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
    public Machine verifiOuCreerMachine(String adresIP,String port,int periodeCheck, String nonOS, String nomMachine){
        Machine machine = getMachine(adresIP);
        if(machine==null){//la machine n'existe pas on la créer
            return creerMachine(adresIP,port,periodeCheck, nonOS, nomMachine);//on créer l'objet dans la BD
        }else{//la machine existe on l'a retourne
            return machine;
        }   
    }
    
    private SurveillerDd getSurveillerDd(int IdMachine, String lettre_partition){
        SurveillerDdPK cle = new SurveillerDdPK(IdMachine, lettre_partition);
        return em.find(SurveillerDd.class, cle);
    }
    
    public SurveillerDd creerTacheSurveilleDD(String adresIpMachine, int periode, String lettre_partition,int seuil, String statue){
        SurveillerDd tacheDD = new SurveillerDd();
        Machine machine = getMachine(adresIpMachine);
        if(machine==null){
            String msg = "machine inexistante";
            System.out.println(msg);
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, msg);
            return null;
        }else{//la machine existe on creer la tache
            tacheDD.setIdMachine(machine);
            tacheDD.setSurveillerDdPK(new SurveillerDdPK(machine.getIdMachine(), lettre_partition));
            tacheDD.setPeriodeVerrification(periode);
            tacheDD.setSeuilPourAlerte(seuil);
            tacheDD.setStatue(statue);
            persist(tacheDD);//on enregistre la tache dans la BD
            return getSurveillerDd(machine.getIdMachine(), lettre_partition);
        }    
    }  
}
