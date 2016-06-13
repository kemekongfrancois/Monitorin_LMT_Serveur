/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import entite.Machine;
import java.util.AbstractList;
import java.util.ArrayList;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
            return "ko"+ e;
        }
    }

 
    public String creerMachine(String AdresIP,int periodeCheck, String nonOS, String nomMachine) {
        Machine machine = new Machine();
        machine.setAdresseIP(AdresIP);
        machine.setNomMachine(nomMachine);
        machine.setPeriodeDeCheck(periodeCheck);
        machine.setTypeOS(nonOS);
        
        return persist(machine);
    }
    
    public Machine getMachine(String adresseIP){
        return em.find(Machine.class, adresseIP);
    }
    
    /**
     * cette fonction retourne la machine donc les caractéristique sont pris en paramètre
     * si la machine n'existe pas on là créer
     * @param AdresIP
     * @param periodeCheck
     * @param nonOS
     * @param nomMachine
     * @return 
     */
    public Machine verifiOuCreerMachine(String AdresIP,int periodeCheck, String nonOS, String nomMachine){
        Machine machine = getMachine(AdresIP);
        if(machine==null){//la machine n'existe pas on la créer
            if(creerMachine(AdresIP, periodeCheck, nonOS, nomMachine)==OK){
                return getMachine(AdresIP);
            }else{
                return null;
            }
        }else{//la machine existe on l'a retourne
            return machine;
        }
        
    }
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
}
