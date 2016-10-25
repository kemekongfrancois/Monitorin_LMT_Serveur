/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Machine;
import java.io.Serializable;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import sessionBean.Bean;

/**
 *
 * @author KEF10
 */
@Named(value = "beanModifierMachine")
@ViewScoped
public class BeanModifierMachine implements Serializable {

    @EJB
    private Bean bean;

    private Machine machine;
    private String adresMachine;

    /**
     * Creates a new instance of BeanModifierMachine
     */
    public BeanModifierMachine() {
    }

    public String pageModifieMachine(String adresseMachine) {
        System.out.println("apple de la page pour modifier une machine:" + adresseMachine);
        return "modifieMachine?faces-redirect=true&amp&adresseMachine=" + adresseMachine;

    }

    /**
     * enregistre les modification aporté à la machine et repercute les
     * modification sur la machine physique si possible
     *
     * @return
     */
    public String enregistreMachine() {

        if (machine == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Gros Problème", "le beans est null: cause inconue");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        String resultat = bean.updateMachie(machine);
        if (resultat.equals(bean.OK)) {
            if (!machine.getStatut().equals(Bean.STOP)) {//on applique les modification sur la machine physique si celle-ci n'a pas la valeur de STOP
                bean.redemarerTachePrincipaleEtSousTache(machine);
            }
            return "listMachines?faces-redirect=true&amp";
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Impossible d'enregistrer les modifications", resultat);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

    }

    public void loadMachine() {
        System.out.println("appel de la méthode load " + this);
        this.machine = bean.getMachineByIP(adresMachine);
        //this.machine = manageBean.getCompteById(idCompte);
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public String getAdresMachine() {
        return adresMachine;
    }

    public void setAdresMachine(String adresMachine) {
        this.adresMachine = adresMachine;
    }

}
