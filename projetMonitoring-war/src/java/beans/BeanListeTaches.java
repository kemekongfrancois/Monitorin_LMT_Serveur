/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entite.Tache;
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
@Named(value = "beanListeTaches")
@ViewScoped
public class BeanListeTaches implements Serializable {

    @EJB
    private Bean bean;

    List<Tache> listTaches;

    public BeanListeTaches() {

    }

    @PostConstruct
    public void init() {
        listTaches = bean.getAllTache();
    }

    public static void suprimerTacheStatic(Tache tache, List<Tache> listTachesLocal, Bean beanLocal) {
        if (!tache.getStatut().equals(Bean.STOP)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "impossible de suprimer la tache :" + tache.getIdTache(), "Le statut doit être à <<" + Bean.STOP + ">> ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (beanLocal.supprimerTache(tache.getIdTache())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès ", "La tache a été supprimé ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            listTachesLocal.remove(tache);
            return;
            //return chargerPage(tache.getIdMachine().getAdresseIP());
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problème inconnue ", "impossible de supprimer la tache");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
    }

    public void suprimerTache(Tache tache) {
        suprimerTacheStatic(tache, listTaches, bean);
    }

    public static void stopeOuRedemarerTache(String statut, Tache tache, Bean beanLocal) {
        tache.setStatut(statut);
        beanLocal.updateTache(tache);//on enregistre la nouvelle valeur du statut dans la BD

        boolean resultat = beanLocal.startRefreshStopTacheSurMachinePhy(tache.getIdTache());
        if (resultat) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Le statut de la tache <" + tache.getIdTache() + "> es :" + tache.getStatut(), " Les modification ont été enregistrer");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "impossible de communique avec la machine: " + tache.getIdMachine().getAdresseIP(), "Veillez démarrer la supervision de cette machine");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return;
    }

    public void stoperTache(Tache tache) {
        //this.tache = tache;
        stopeOuRedemarerTache(Bean.STOP, tache, bean);
    }

    public void demarerOuRedemarerTache(Tache tache) {
        // this.tache = tache;
        stopeOuRedemarerTache(Bean.START, tache, bean);
    }

    /**
     * cette fonction sera réutilisable dans d'autre beans vue quelle es static.
     * elle permettra de tester les fonctions
     *
     * @param beanLocal
     * @param tache
     */
    public static void testerTacheStatic(Bean beanLocal, Tache tache) {
        FacesMessage msg = new FacesMessage();
        String resultat = beanLocal.testTache(tache);
        if (resultat == null) {
            //msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problème avec la machine hôte ou l’agent" , Bean.PB_AGENT + "\n ou \n " + Bean.INACCESSIBLE);
            msg.setSeverity(FacesMessage.SEVERITY_FATAL);
            msg.setSummary("Problème avec la machine hôte ou l’agent");
            msg.setDetail(Bean.PB_AGENT + "\n ou \n " + Bean.INACCESSIBLE);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        String enteteAlerte = "Résultat de l’exécution de la tâche id= " + tache.getIdTache() + "\n valeur= " + resultat;
        msg.setSummary(enteteAlerte);
        switch (tache.getTypeTache()) {
            case Bean.TACHE_DD:
                if (resultat.equals(Bean.PB)) {
                    //msg = new FacesMessage(FacesMessage.SEVERITY_WARN,enteteAlerte, Bean.ERREUR_PARTITION_DD);
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    msg.setDetail(Bean.ERREUR_PARTITION_DD);
                } else {
                    //msg = new FacesMessage(FacesMessage.SEVERITY_INFO,enteteAlerte,"Le pourcentage d’occupation de la partition << " + tache.getNom() + " >> est de :" + resultat + "%");
                    msg.setSeverity(FacesMessage.SEVERITY_INFO);
                    msg.setDetail("Le pourcentage d’occupation de la partition << " + tache.getNom() + " >> est de :" + resultat + "%");
                }
                break;
            case Bean.TACHE_PROCESSUS:
                switch (resultat) {
                    case Bean.OK:
                        msg.setSeverity(FacesMessage.SEVERITY_INFO);
                        msg.setDetail("Le processus << " + tache.getNom() + " >> est bien en cours de fonctionnement");
                        break;
                    case Bean.KO:
                        msg.setSeverity(FacesMessage.SEVERITY_WARN);
                        msg.setDetail("Le processus << " + tache.getNom() + " >> n'est pas en cours de fonctionnement");
                        break;
                    default:
                        //cas ou il ya eu une exception
                        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                        msg.setDetail("Une Exception c’est produite lors de l’exécution");
                        break;
                }
                break;
            case Bean.TACHE_SERVICE:
                switch (resultat) {
                    case Bean.OK:
                        msg.setSeverity(FacesMessage.SEVERITY_INFO);
                        msg.setDetail("Le service << " + tache.getNom() + " >> est bien en cours de fonctionnement");
                        break;
                    case Bean.KO:
                        msg.setSeverity(FacesMessage.SEVERITY_WARN);
                        msg.setDetail("Le service << " + tache.getNom() + " >> n'est pas en cours de fonctionnement");
                        break;
                    default:
                        //cas ou il ya eu une exception
                        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                        msg.setDetail("Une Exception c’est produite lors de l’exécution");
                        break;
                }
                break;
            case Bean.TACHE_PING:
                switch (resultat) {
                    case Bean.OK:
                        msg.setSeverity(FacesMessage.SEVERITY_INFO);
                        msg.setDetail("Le ping vers << " + tache.getNom() + " >> est OK");
                        break;
                    default:
                        msg.setSeverity(FacesMessage.SEVERITY_WARN);
                        msg.setDetail("Le ping vers << " + tache.getNom() + " >> ne passe pas");
                        break;
                }
                break;
            case Bean.TACHE_FICHIER_EXISTE:
                switch (resultat) {
                    case Bean.OK:
                        msg.setSeverity(FacesMessage.SEVERITY_INFO);
                        msg.setDetail("Le fichier << " + tache.getNom() + " >> existe");
                        break;
                    default:
                        msg.setSeverity(FacesMessage.SEVERITY_WARN);
                        msg.setDetail("Le fichier<< " + tache.getNom() + " >> n'existe pas");
                        break;
                }
                break;
            case Bean.TACHE_TAILLE_FICHIER:
                if (resultat.equals(Bean.PB)) {
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    msg.setDetail("Une Exception c’est produit lors de l’exécution ceci peut être dû au fait que le fichier n’existe pas");
                } else {
                    msg.setSeverity(FacesMessage.SEVERITY_INFO);

                    msg.setDetail("La taille du fichier << " + tache.getNom() + " >> est de :" + resultat + " octets soit " + new Integer(resultat) / 1024 + " kilooctets");
                }
                break;
            case Bean.TACHE_TELNET:
                switch (resultat) {
                    case Bean.OK:
                        msg.setSeverity(FacesMessage.SEVERITY_INFO);
                        msg.setDetail("Le ping vers l'adresse << " + tache.getNom() + " >>  est OK");
                        break;
                    default:
                        msg.setSeverity(FacesMessage.SEVERITY_WARN);
                        msg.setDetail("Le Telne ne passe pas");
                        break;
                }
                break;
            case Bean.TACHE_DATE_MODIFICATION_DERNIER_FICHIER:
                if (resultat.equals(Bean.PB)) {
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    msg.setDetail("Une Exception c’est produit lors de l’exécution ceci peut être dû au fait que le repertoire n’est pas valide");
                } else {
                    msg.setSeverity(FacesMessage.SEVERITY_INFO);
                    msg.setDetail("La date de modification du répertoire : << " + tache.getNom() + " >> est :\n" + resultat);
                }
                break;
            case Bean.TACHE_UPTIME_MACHINE:
                if (resultat.equals(Bean.PB)) {
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    msg.setDetail("Une Exception c’est produit lors de l’exécution ceci peut être dû au fait que le fichier \"uptime.exe\" ne se trouve pas dans le même repertoire que l'agent");
                } else {
                    msg.setSeverity(FacesMessage.SEVERITY_INFO);
                    msg.setDetail("La machine  << " + tache.getIdMachine().getAdresseIP() + " >> est allumée depuis " + resultat + " jours");
                }
                break;
            case Bean.TACHE_TEST_LIEN:
                switch (resultat) {
                    case Bean.OK:
                        msg.setSeverity(FacesMessage.SEVERITY_INFO);
                        msg.setDetail("Le lien << " + tache.getNom() + " >> est UP");
                        break;
                    case Bean.KO:
                        msg.setSeverity(FacesMessage.SEVERITY_WARN);
                        msg.setDetail("Le lien << " + tache.getNom() + " >> est DOWN");
                        break;
                    default:
                        //cas ou il ya eu une exception
                        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                        msg.setDetail("Une Exception c’est produite lors de l’exécution");
                        break;
                }
                break;
            default:
                // msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,enteteAlerte, resultat);
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                msg.setDetail(resultat);
                break;
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void testerTache(Tache tache) {
        testerTacheStatic(bean, tache);
    }

    public List<Tache> getListTaches() {
        return listTaches;
    }

    public void setListTaches(List<Tache> listTaches) {
        this.listTaches = listTaches;
    }

}
