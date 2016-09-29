/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.io.Serializable;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import sessionBean.Bean;
import sessionBean.BeanInitialisation;

/**
 *
 * @author KEF10
 */
@Named(value = "beanVariableGlobal")
@ApplicationScoped
public class BeanVariableGlobal implements Serializable{

    
    private List<String> listOS;
    private List<String> listTypeTache;
    private List<String> listTypeCompte;
    private List<String> listTypeStatut;
    /**
     * Creates a new instance of BeanVariableGlobal
     */
    public BeanVariableGlobal() {
        System.out.println("initialisation des variables globale de la partie web");
        listOS = BeanInitialisation.listOS;
        listTypeTache = BeanInitialisation.listTypeTache;
        listTypeCompte = BeanInitialisation.listTypeCompte;
        listTypeStatut = BeanInitialisation.listTypeStatut;
        //System.out.println("============"+listOS.get(0));
        //System.out.println("============"+listTypeTache.get(0));
    }
    
    public String couleurStatut(String statut){
        String couleur;
        switch(statut){
            case Bean.ALERTE: couleur = "#ff6633"; break;
            case Bean.START: couleur = "green"; break;
            case Bean.STOP: couleur = "black"; break;
            default : couleur = "chocolate";
        }
        return couleur;
    }
    
    public String booleanEnString(boolean valeur){
        if(valeur) return "on";
        return "off";
    }
    
    public String booleanCouleur(boolean valeur){
        if(valeur) return "blue";
        return "red";
    }

    public List<String> getListOS() {
        return listOS;
    }

    public void setListOS(List<String> listOS) {
        this.listOS = listOS;
    }

    public List<String> getListTypeTache() {
        return listTypeTache;
    }

    public void setListTypeTache(List<String> listTypeTache) {
        this.listTypeTache = listTypeTache;
    }

    public List<String> getListTypeCompte() {
        return listTypeCompte;
    }

    public void setListTypeCompte(List<String> listTypeCompte) {
        this.listTypeCompte = listTypeCompte;
    }

    public List<String> getListTypeStatut() {
        return listTypeStatut;
    }

    public void setListTypeStatut(List<String> listTypeStatut) {
        this.listTypeStatut = listTypeStatut;
    }

    
    
    
}
