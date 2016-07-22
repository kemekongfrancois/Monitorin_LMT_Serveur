/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Machine;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
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
    
    
    public BeanAccueil() {
    }
    
 
    @PostConstruct
    public void init() {
       listMachines = bean.getListMachine();        
    }
     
    public List<Machine> getListMachines() {
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
