/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Tache;
import java.io.Serializable;
import java.util.List;
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
@Named(value = "beanListeTachesMachine")
@ViewScoped
public class BeanListeTachesMachine implements Serializable {

    @EJB
    private Bean bean;

    private List<Tache> listTaches;
    private Tache tache;
    private String adresseMachine;

    public BeanListeTachesMachine() {
    }

    public String chargerPage(String adresseMachine) {
        System.out.println("apple de la page de la liste des taches de la machines:" + adresseMachine);
        //return "modifieMachines?faces-redirect=true&amp";
        return "listTachesMachine?faces-redirect=true&amp&adresseMachine=" + adresseMachine;

    }

    public void loadListeTache() {
        System.out.println("appel de la méthode load " + this);
        this.listTaches = bean.getListTacheMachine(adresseMachine);
        //this.machine = manageBean.getCompteById(idCompte);
    }

    private String stopeOuRedemarerTache(String statue) {
        tache.setStatue(statue);
        bean.updateTache(tache);//on enregistre la nouvelle valeur du statue dans la BD

        boolean resultat = bean.startRefreshStopTacheSurMachinePhy(tache.getIdTache());
        if (resultat) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Le statue de la tache <" + tache.getIdTache() + "> es :" + tache.getStatue(), " Les modification ont été enregistrer");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "impossible de communique avec la machine: " + tache.getIdMachine().getAdresseIP(), "Veillez démarrer la supervision de cette machine");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return null;
    }

    public String stoperTache(Tache tache) {
        this.tache = tache;
        return stopeOuRedemarerTache(Bean.STOP);
    }

    public String demarerOuRedemarerTache(Tache tache) {
        this.tache = tache;
        return stopeOuRedemarerTache(Bean.START);
    }

    public String suprimerTache(Tache tache) {
        if (tache.getStatue().equals(Bean.START)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "impossible de suprimer la tache :" + tache.getIdTache(), "Le statut ne doit pas être à <<" + Bean.START + ">> ");
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

    public String getAdresseMachine() {
        return adresseMachine;
    }

    public void setAdresseMachine(String adresseMachine) {
        this.adresseMachine = adresseMachine;
    }

    public Tache getTache() {
        return tache;
    }

    public void setTache(Tache tache) {
        this.tache = tache;
    }

}
