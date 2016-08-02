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
    private List<Tache> listTache;

    @PostConstruct
    public void init() {
        listMachines = bean.getAllMachineAvecBonStatue();
    }

    /**
     * cette fonction permet d'enregistre les modeifications dans la BD et sur
     * la machine physique
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

        System.out.println("msgAlert= " + resultat);
        if (statue.equals(Bean.START)) {//cette instruction permet de mettre à jour l'interface graphique avec la veritable valeur du statue
            machine.setStatue(resultat);
        }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "impossible de communique avec la machine: " + machine.getAdresseIP(), "Cause: "+resultat);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return null;
    }

    /**
     * cette fonction permet de stoper la tacheelle agit dans la BD et sur la
     * machine physique si possible
     *
     * @param machine
     * @return
     */
    public String stoperMachine(Machine machine) {
        this.machine = machine;
        return stopeOuRedemarerMachine(Bean.STOP);
    }

    /**
     * cette fonction permet de démarer la tache ou de la rafrechire elle agit
     * dans la BD et sur la machine physique si possible
     *
     * @param machine
     * @return
     */
    public String demarerOuRedemarerMachine(Machine machine) {
        this.machine = machine;
        return stopeOuRedemarerMachine(Bean.START);
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