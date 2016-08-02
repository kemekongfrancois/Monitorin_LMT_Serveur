/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Machine;
import entite.Tache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;
import sessionBean.Bean;

/**
 *
 * @author KEF10
 */
@Named(value = "beanAccueil")
@ViewScoped
//@ManagedBean
public class BeanAccueil implements Serializable{

    @EJB
    private Bean bean;

    private List<Machine> listMachines;
    private Tache tache;

    public Tache getTache() {
        return tache;
    }

    public void setTache(Tache tache) {
        this.tache = tache;
    }
    
    
    public BeanAccueil() {
    }
    
    public String suprimerTache(String id, String nom) {
        System.out.println("id="+id+" nom="+nom);
        String page = "pageDeTest?faces-redirect=true&amp";
        /*Compte cpt = manageBean.getCompteLoginPass(id, nom);
        if (cpt == null) {
        FacesMessage message = new FacesMessage("Login ou mot de passe incorrect ");
        message.setSeverity(FacesMessage.SEVERITY_ERROR);//ceci permet de dire que le message à affiché sera de type Erreur
        FacesContext.getCurrentInstance().addMessage(null, message);
        
        page = "accueil";
        } else {
        //FacesMessage message = new FacesMessage("connection OK");
        //FacesContext.getCurrentInstance().addMessage(null, message);
        
        //initialisation();
        compte = cpt;
        if(cpt.getTypecompte().equals("supAdmin")) page = "PageSuperAdmin?faces-redirect=true&amp";
        else page = "pageUtilisateur?faces-redirect=true&amp";
        }*/
        return page;
    }
 
    @PostConstruct
    public void init() {
       listMachines = bean.getAllMachineAvecBonStatue();        
    }
     
    public List<Machine> getListMachines() {
        /*for (Machine machine : listMachines) {
        System.out.println(machine.getAdresseIP());
        }*/
        return listMachines;
    }
     
    public void onTabChange(TabChangeEvent event) {
        FacesMessage msg = new FacesMessage("Chargement", event.getTab().getTitle());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
         
    public void onTabClose(TabCloseEvent event) {
        FacesMessage msg = new FacesMessage("Cloture", event.getTab().getTitle());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
   
}
