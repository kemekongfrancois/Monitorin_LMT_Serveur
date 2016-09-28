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
@Named(value = "beanModifierTache")
@ViewScoped
public class BeanModifierTache implements Serializable{

    @EJB
    private Bean bean;
    Tache tache;
    Integer idTache;
    /**
     * Creates a new instance of BeanModifierTache
     */
    public BeanModifierTache() {
    }

    public String pageModifieTache(Integer idTache) {
        //System.out.println("apple de la page pour modifier une Tache:" + idTache);
        return "modifierTache?faces-redirect=true&amp&idTache=" + idTache;

    }
    
    public String pagePrecedente(){
        return "listTachesMachine?faces-redirect=true&amp&adresseMachine=" + tache.getIdMachine().getAdresseIP();
    }

    /**
     * enregistre les modification aporté à la tache et repercute les modification  sur la machine physique si possible
     * @return 
     */
    public String enregistreTache() {

        if (tache == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,"Gros Problème", "le beans es null: cause inconue");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        String resultat = bean.updateTache(tache);
        if (resultat.equals(Bean.OK)) {
            bean.startRefreshStopTacheSurMachinePhy(tache.getIdTache());
            return pagePrecedente();
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Impossible d'enregistrer les modifications",resultat);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

    }

    public void executerTache(){
        String resultat = bean.executerTache(tache);
        if(resultat.equals(Bean.OK)){
            FacesMessage msg = new FacesMessage("La tâche a été exécuter");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }else{
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"La tâche ne peut être exécuter pour le moment",resultat);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
         
    }
    public void loadTache() {
        //System.out.println("appel de la méthode load "+this);
        this.tache = bean.getTache(idTache);
    }
    
    public Tache getTache() {
        return tache;
    }

    public void setTache(Tache tache) {
        this.tache = tache;
    }

    public Integer getIdTache() {
        return idTache;
    }

    public void setIdTache(Integer idTache) {
        this.idTache = idTache;
    }
    
    
}
