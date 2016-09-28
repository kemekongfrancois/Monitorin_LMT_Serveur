/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Machine;
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
    private Machine machine;

    public BeanListeTachesMachine() {
    }

    /**
     * cette fonction permet d'enregistre les modeifications dans la BD et sur
     * la machine physique si possible
     *
     * @param statue
     * @return
     */
    private String stopeOuRedemarerMachine(String statue) {
        machine.setStatue(statue);
        bean.updateMachie(machine);//on enregistre la nouvelle valeur du statue dans la BD
        String resultat = bean.redemarerTachePrincipaleEtSousTache(machine);
        if (resultat.equals(Bean.OK)) {
            //machine.setStatue(statue);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Le statue de la machine <" + machine.getAdresseIP() + "> es :" + machine.getStatue(), " Les modification ont été enregistrer");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

        //System.out.println("msgAlert= " + resultat);
        if (statue.equals(Bean.START)) {//cette instruction permet de mettre à jour l'interface graphique avec la veritable valeur du statue
            machine.setStatue(resultat);
        }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "impossible de communique avec la machine: " + machine.getAdresseIP(), "Cause: "+resultat);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return null;
    }
    
    public void testerTache(Tache tache){
        String resultat = bean.testTache(tache);
        FacesMessage msg = new FacesMessage(resultat);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    /**
     * cette fonction permet de stoper la machine elle agit dans la BD et sur la
     * machine physique si possible
     *
     * @return
     */
    public String stoperMachine() {
        return stopeOuRedemarerMachine(Bean.STOP);
    }

    /**
     * cette fonction permet de démarer la machine ou de la rafrechire elle agit
     * dans la BD et sur la machine physique si possible
     *
     * @return
     */
    public String demarerOuRedemarerMachine() {
        return stopeOuRedemarerMachine(Bean.START);
    }
    
    public String chargerPage(String adresseMachine) {
        System.out.println("apple de la page de la liste des taches de la machines:" + adresseMachine);
        //return "modifieMachines?faces-redirect=true&amp";
        return "listTachesMachine?faces-redirect=true&amp&adresseMachine=" + adresseMachine;

    }

    public void loadListeTacheEtMachine() {
        //System.out.println("appel de la méthode load " + this);
        this.listTaches = bean.getListTacheMachine(adresseMachine);
        this.machine = bean.getMachineAvecBonStatue(adresseMachine);
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
        if (!tache.getStatue().equals(Bean.STOP)) {
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

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

}
