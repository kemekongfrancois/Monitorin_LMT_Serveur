/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Serveur;
import entite.Utilisateur;
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
@Named(value = "beanAdministrerServeur")
@ViewScoped
public class BeanAdministrerServeur implements Serializable{

    @EJB
    private Bean bean;

    private Serveur serveur;
    private List<Utilisateur> allUtilisateur;
    
    public BeanAdministrerServeur() {
    }
    
    @PostConstruct
    public void init(){
        this.serveur = bean.getServeurOuInitialiseBD();
        this.allUtilisateur = bean.getAllUtilisateur();
    }

    public void suprimerUtilisateur(Utilisateur utilisateur){
         
        if (bean.supprimerUtilisateur(utilisateur.getIdUtilisateur())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès ", "L'utilisateur a été supprimé ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            allUtilisateur.remove(utilisateur);
            return ;
            //return chargerPage(tache.getIdMachine().getAdresseIP());
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problème inconnue ", "impossible de supprimer l'utilisateur");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return ;
        }
    }
    
    public Serveur getServeur() {
        return serveur;
    }

    public void setServeur(Serveur serveur) {
        this.serveur = serveur;
    }

    public List<Utilisateur> getAllUtilisateur() {
        return allUtilisateur;
    }

    public void setAllUtilisateur(List<Utilisateur> allUtilisateur) {
        this.allUtilisateur = allUtilisateur;
    }
    
    
    
}
