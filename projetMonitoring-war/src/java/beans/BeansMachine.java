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
@Named(value = "beansMachine")
@ViewScoped
public class BeansMachine implements Serializable {

    @EJB
    private Bean bean;

    private List<Machine> listMachines;
    private Machine machine;

    @PostConstruct
    public void init() {
        System.out.println("recupération de la liste des machines pour affichage");
        listMachines = bean.getAllMachineAvecBonStatut();
    }

    /**
     * cette fonction permet d'enregistre les modeifications dans la BD et sur
     * la machine physique si possible
     *
     * @param statut
     * @return
     */
    public static void stopeOuRedemarerMachine(String statut,Machine machineLocal, Bean beanLocal) {
        machineLocal.setStatut(statut);
        beanLocal.updateMachie(machineLocal);//on enregistre la nouvelle valeur du statut dans la BD
        String resultat = beanLocal.redemarerTachePrincipaleEtSousTache(machineLocal);
        if (resultat.equals(Bean.OK)) {
            //machine.setStatut(statut);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Le statut de la machine <" + machineLocal.getAdresseIP() + "> es :" + machineLocal.getStatut(), " Les modification ont été enregistrer");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        //System.out.println("msgAlert= " + resultat);
        if (statut.equals(Bean.START)) {//cette instruction permet de mettre à jour l'interface graphique avec la veritable valeur du statut
            machineLocal.setStatut(resultat);
        }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "impossible de communique avec la machine: " + machineLocal.getAdresseIP(), "Cause: "+resultat);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return;
    }

    /**
     * cette fonction permet de stoper la machine elle agit dans la BD et sur la
     * machine physique si possible
     *
     * @param machine
     * @return
     */
    public void stoperMachine(Machine machine) {
        //this.machine = machine;
        stopeOuRedemarerMachine(Bean.STOP,machine,bean);
    }

    /**
     * cette fonction permet de démarer la machine ou de la rafrechire elle agit
     * dans la BD et sur la machine physique si possible
     *
     * @param machine
     * @return
     */
    public void demarerOuRedemarerMachine(Machine machine) {
        //this.machine = machine;
        stopeOuRedemarerMachine(Bean.START,machine,bean);
    }
    
    public String suprimerMachine(Machine machine) {
        if (!machine.getStatut().equals(Bean.STOP)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "impossible de suprimer la machine :" + machine.getAdresseIP(), "Le statut doit être à <<" + Bean.STOP + ">> ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        if (bean.suprimeerMachine(machine.getAdresseIP())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès ", "La machine a été supprimé ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            listMachines.remove(machine);
            return null;
            //return chargerPage(tache.getIdMachine().getAdresseIP());
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problème inconnue ", "impossible de supprimer la tache");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
    }


    public BeansMachine() {
    }

    public List<Machine> getListMachines() {
        return listMachines;
    }

    public void setListMachines(List<Machine> listMachines) {
        this.listMachines = listMachines;
    }
public Machine getMachine() {
        return machine;
    }
    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public List<Tache> getListTache() {
        return bean.getListTacheMachine(machine.getAdresseIP());
    }
    
    

}
