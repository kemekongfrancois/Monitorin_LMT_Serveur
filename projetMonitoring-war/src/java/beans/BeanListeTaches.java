/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Tache;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
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
@Named(value = "beanListeTaches")
@ViewScoped
public class BeanListeTaches implements Serializable{

    @EJB
    private Bean bean;

    List<Tache> listTaches;
    public BeanListeTaches() {
        
    }

    @PostConstruct
    public void init(){
        listTaches = bean.getAllTache();
    }
    
    public String suprimerTache(Tache tache) {
        if (!tache.getStatut().equals(Bean.STOP)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "impossible de suprimer la tache :" + tache.getIdTache(), "Le statut doit être à <<" + Bean.STOP + ">> ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        if (bean.supprimerTache(tache.getIdTache())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès ", "La tache a été supprimé ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            listTaches.remove(tache);
            return null;
            //return chargerPage(tache.getIdMachine().getAdresseIP());
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problème inconnue ", "impossible de supprimer la tache");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
    }
    
    public List<Tache> getListTaches() {
        return listTaches;
    }

    public void setListTaches(List<Tache> listTaches) {
        this.listTaches = listTaches;
    }
    
}
