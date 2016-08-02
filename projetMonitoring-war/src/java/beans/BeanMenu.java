/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author KEF10
 */
@Named(value = "beanMenu")
@ManagedBean
public class BeanMenu {
 
    public BeanMenu() {
    }
    
    public String pageAccueil(){
        System.out.println("apple de la page d'accueil");
        return "accueil?faces-redirect=true&amp";
    }
    
    public String pagelistMachines(){
        System.out.println("apple de la page list Machine");
        return "listMachines?faces-redirect=true&amp";
    }
    
    public String pageAdministrerServeur(){
        return "administrerServeur?faces-redirect=true&amp";
    }

    
    
}
