/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entite;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author KEF10
 */
@Entity
@Table(name = "serveur")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Serveur.findAll", query = "SELECT s FROM Serveur s"),
    @NamedQuery(name = "Serveur.findByIdServeur", query = "SELECT s FROM Serveur s WHERE s.idServeur = :idServeur"),
    @NamedQuery(name = "Serveur.findByEmailEnvoiMail", query = "SELECT s FROM Serveur s WHERE s.emailEnvoiMail = :emailEnvoiMail"),
    @NamedQuery(name = "Serveur.findByPassEnvoiMail", query = "SELECT s FROM Serveur s WHERE s.passEnvoiMail = :passEnvoiMail"),
    @NamedQuery(name = "Serveur.findByLogingSMS", query = "SELECT s FROM Serveur s WHERE s.logingSMS = :logingSMS"),
    @NamedQuery(name = "Serveur.findByMotdepasseSMS", query = "SELECT s FROM Serveur s WHERE s.motdepasseSMS = :motdepasseSMS"),
    @NamedQuery(name = "Serveur.findByNumeroCourt", query = "SELECT s FROM Serveur s WHERE s.numeroCourt = :numeroCourt"),
    @NamedQuery(name = "Serveur.findByEnvoialerteSMS", query = "SELECT s FROM Serveur s WHERE s.envoialerteSMS = :envoialerteSMS"),
    @NamedQuery(name = "Serveur.findByEnvoieAlerteMail", query = "SELECT s FROM Serveur s WHERE s.envoieAlerteMail = :envoieAlerteMail")})
public class Serveur implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_serveur")
    private Integer idServeur;
    @Size(max = 254)
    @Column(name = "email_envoi_mail")
    private String emailEnvoiMail;
    @Size(max = 254)
    @Column(name = "pass_envoi_mail")
    private String passEnvoiMail;
    @Size(max = 254)
    @Column(name = "loging_SMS")
    private String logingSMS;
    @Size(max = 254)
    @Column(name = "mot_de_passe_SMS")
    private String motdepasseSMS;
    @Size(max = 254)
    @Column(name = "numero_court")
    private String numeroCourt;
    @Column(name = "envoi_alerte_SMS")
    private Boolean envoialerteSMS;
    @Column(name = "envoie_alerte_mail")
    private Boolean envoieAlerteMail;

    public Serveur() {
    }

    public Serveur(Integer idServeur) {
        this.idServeur = idServeur;
    }

    public Integer getIdServeur() {
        return idServeur;
    }

    public void setIdServeur(Integer idServeur) {
        this.idServeur = idServeur;
    }

    public String getEmailEnvoiMail() {
        return emailEnvoiMail;
    }

    public void setEmailEnvoiMail(String emailEnvoiMail) {
        this.emailEnvoiMail = emailEnvoiMail;
    }

    public String getPassEnvoiMail() {
        return passEnvoiMail;
    }

    public void setPassEnvoiMail(String passEnvoiMail) {
        this.passEnvoiMail = passEnvoiMail;
    }

    public String getLogingSMS() {
        return logingSMS;
    }

    public void setLogingSMS(String logingSMS) {
        this.logingSMS = logingSMS;
    }

    public String getMotdepasseSMS() {
        return motdepasseSMS;
    }

    public void setMotdepasseSMS(String motdepasseSMS) {
        this.motdepasseSMS = motdepasseSMS;
    }

    public String getNumeroCourt() {
        return numeroCourt;
    }

    public void setNumeroCourt(String numeroCourt) {
        this.numeroCourt = numeroCourt;
    }

    public Boolean getEnvoialerteSMS() {
        return envoialerteSMS;
    }

    public void setEnvoialerteSMS(Boolean envoialerteSMS) {
        this.envoialerteSMS = envoialerteSMS;
    }

    public Boolean getEnvoieAlerteMail() {
        return envoieAlerteMail;
    }

    public void setEnvoieAlerteMail(Boolean envoieAlerteMail) {
        this.envoieAlerteMail = envoieAlerteMail;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idServeur != null ? idServeur.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Serveur)) {
            return false;
        }
        Serveur other = (Serveur) object;
        if ((this.idServeur == null && other.idServeur != null) || (this.idServeur != null && !this.idServeur.equals(other.idServeur))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.Serveur[ idServeur=" + idServeur + " ]";
    }
    
}
