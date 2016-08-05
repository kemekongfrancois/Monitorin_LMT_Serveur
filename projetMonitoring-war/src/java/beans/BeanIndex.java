/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Utilisateur;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import sessionBean.Bean;

/**
 *
 * @author KEF10
 */
@Named(value = "beanIndex")
@RequestScoped
public class BeanIndex {

    @EJB
    private Bean bean;

    private String login;
    private String pass;
    public BeanIndex() {
    }
    
    public String connection(){
        Utilisateur utilisateur = bean.getUtilisateurByloginAndPass(login, pass);
        if(utilisateur==null){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Echec de l’authentification ", "Login ou mot de passe incorrect ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }else{
            return BeanMenu.pageListeTaches();
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
    
}
