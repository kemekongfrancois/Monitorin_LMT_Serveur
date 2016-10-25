/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Utilisateur;
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
@Named(value = "beanUtilisateur")
@ViewScoped
public class BeanUtilisateur implements Serializable{

    @EJB
    private Bean bean;

    Utilisateur utilisateur;
    Integer idUtilisateur;
    /**
     * Creates a new instance of BeanUtilisateur
     */
    public BeanUtilisateur() {
        utilisateur = new Utilisateur();
    }
    
    

    public String pageModifieUtilisateur(Integer idUtilisateur){
        //utilisateur = bean.getUtilisateur(idUtilisateur);
        return "modifierUtilisateur?faces-redirect=true&amp&idUtilisateur=" + idUtilisateur; 
    }
    
    public String pageCreerUtilisateur(){
        utilisateur = new Utilisateur();
        return "creerUtilisateur?faces-redirect=true&amp";
    }
    
    
     public void loadUtilisateur() {
    //System.out.println("appel de la méthode load "+this);
    this.utilisateur = bean.getUtilisateur(idUtilisateur);
    }
    public String enregistreModificationUtilisateur() {
        if (utilisateur == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,"Gros Problème", "le beans est null: cause inconue");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        String resultat = null;
        try {
            resultat = bean.updateUtilisateur(utilisateur);
        } catch (Exception e) {
           // System.out.println("--------------------------------------"+e);
           resultat = "Le login, la boite mail et le numéro de téléphone doivent être unique dans toute la base de donné\n"+e;
        }
        if (resultat.equals(Bean.OK)) {
            return BeanMenu.pageAdministrerServeur();
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Impossible d'enregistrer les modifications",resultat);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

    }
    
    public String creerUtilisateur(){
        if (utilisateur == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,"Gros Problème", "le beans est null: cause inconue");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        
        String login = utilisateur.getLogin();
        String pass = utilisateur.getPass();
        String nom = utilisateur.getNom();
        String prenom = utilisateur.getPrenom();
        String type_compte = utilisateur.getTypeCompte();
        String numero_telephone = utilisateur.getNumeroTelephone();
        String boite_mail = utilisateur.getBoiteMail();
        int niveauDAlerte = utilisateur.getNiveauDAlerte();
        
        String resultat = null;
        try {
            resultat = bean.creerUtilisateur(login, pass, nom, prenom, type_compte, numero_telephone, boite_mail,niveauDAlerte);
        } catch (Exception e) {
            resultat = "Le login, la boite mail et le numéro de téléphone doivent être unique dans toute la base de donné\n"+e;
        }
        
        if (resultat.equals(Bean.OK)) {
            //bean.startRefreshStopTacheSurMachinePhy(tache.getIdTache());
            return BeanMenu.pageAdministrerServeur();
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Impossible de creer le nouvelle utilisateur",resultat);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }
    
    
    
}
