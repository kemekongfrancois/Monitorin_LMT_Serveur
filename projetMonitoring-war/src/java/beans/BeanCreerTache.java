/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Tache;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import sessionBean.Bean;

/**
 *
 * @author KEF10
 */
@Named(value = "beanCreerTache")
@ViewScoped
public class BeanCreerTache implements Serializable{

    @EJB
    private Bean bean;

    private Tache tache;
    private String adresseMachine;
    /**
     * Creates a new instance of BeanCreerTache
     */
    public BeanCreerTache() {
        this.tache = new Tache();
        tache.setStatut(Bean.STOP);
        tache.setEnvoiyerAlerteMail(true);
        tache.setEnvoyerAlerteSms(true);;
        tache.setRedemarerAutoService(false);
        tache.setPeriodeVerrification(Bean.DEFAUL_PERIODE_CHECK_MACHINE);
        tache.setSeuilAlerte(0);
        tache.setNiveauDAlerte(1);
        
    }

    
    public String pageCreerTache(String adresseMachine) {
        this.adresseMachine = adresseMachine;
        System.out.println("apple de la page pour creer une tache sur:" + adresseMachine);
        return "creerTache?faces-redirect=true&amp&adresseMachine=" + adresseMachine;

    }
    public String pagePrecedente(){
        return "listTachesMachine?faces-redirect=true&amp&adresseMachine=" + adresseMachine;
    }
    
    public String crerTache() {

        if (tache == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,"Gros Problème", "le beans es null: cause inconue");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        String resultat = bean.creerTacheByTache(tache, adresseMachine);
        
        if (resultat.equals(Bean.OK)) {
            //bean.startRefreshStopTacheSurMachinePhy(tache.getIdTache());
            return pagePrecedente();
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Impossible de créer la tache ", "<<"+resultat+">>");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

    }
        
    public Tache getTache() {
        return tache;
    }

    public void setTache(Tache tache) {
        this.tache = tache;
    }

    public String getAdresseMachine() {
        return adresseMachine;
    }

    public void setAdresseMachine(String adresseMachine) {
        this.adresseMachine = adresseMachine;
    }
    
    
    
}
