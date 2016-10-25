/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Serveur;
import java.io.Serializable;
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
@Named(value = "beanModifierServeur")
@ViewScoped
public class BeanModifierServeur implements Serializable{
@EJB
    private Bean bean;

    private Serveur serveur;
    
    public BeanModifierServeur() {
    }
    @PostConstruct
    public void init(){
        this.serveur = bean.getServeurOuInitialiseBD();
    }
    
    public String pageModifierServeur() {
        System.out.println("apple de la page pour modifier le Serveur:");
        return "modifierServeur?faces-redirect=true&amp";

    }
    
    public String enregistreServeur() {

        if (serveur == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,"Gros Problème", "le beans est null: cause inconue");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        String resultat = bean.creerOuModifierServeur(serveur.getEmailEnvoiMail(), serveur.getPassEnvoiMail(), serveur.getLogingSMS(), serveur.getMotdepasseSMS(), serveur.getNumeroCourt(), serveur.getEnvoialerteSMS(), serveur.getEnvoieAlerteMail());
        if (resultat.equals(bean.OK)) {
            return "administrerServeur?faces-redirect=true&amp";
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Impossible d'enregistrer les modifications",resultat);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

    }

    public Serveur getServeur() {
        return serveur;
    }

    public void setServeur(Serveur serveur) {
        this.serveur = serveur;
    }

    
    
}
