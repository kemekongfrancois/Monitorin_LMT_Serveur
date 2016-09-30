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
    //private Tache tache;
    private String adresseMachine;
    private Machine machine;

    public BeanListeTachesMachine() {
    }

    public void stoperMachine() {
        //this.machine = machine;
        BeansMachine.stopeOuRedemarerMachine(Bean.STOP,machine,bean);
    }

    
    public void demarerOuRedemarerMachine() {
        //this.machine = machine;
        BeansMachine.stopeOuRedemarerMachine(Bean.START,machine,bean);
    }
    public void testerTache(Tache tache) {
        BeanListeTaches.testerTacheStatic(bean, tache);
    }

    public String chargerPage(String adresseMachine) {
        System.out.println("apple de la page de la liste des taches de la machines:" + adresseMachine);
        //return "modifieMachines?faces-redirect=true&amp";
        return "listTachesMachine?faces-redirect=true&amp&adresseMachine=" + adresseMachine;

    }

    public void loadListeTacheEtMachine() {
        //System.out.println("appel de la méthode load " + this);
        this.listTaches = bean.getListTacheMachine(adresseMachine);
        this.machine = bean.getMachineAvecBonStatut(adresseMachine);
        //this.machine = manageBean.getCompteById(idCompte);
    }

    public void stoperTache(Tache tache) {
        //this.tache = tache;
        BeanListeTaches.stopeOuRedemarerTache(Bean.STOP,tache,bean);
    }

    public void demarerOuRedemarerTache(Tache tache) {
       // this.tache = tache;
        BeanListeTaches.stopeOuRedemarerTache(Bean.START,tache,bean);
    }

    public void suprimerTache(Tache tache) {
        BeanListeTaches.suprimerTacheStatic(tache, listTaches, bean);
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

    /*public Tache getTache() {
    return tache;
    }
    
    public void setTache(Tache tache) {
    this.tache = tache;
    }*/

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

}
