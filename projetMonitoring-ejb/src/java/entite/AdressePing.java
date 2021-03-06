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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author KEF10
 */
@Entity
@Table(name = "adresse_ping")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdressePing.findAll", query = "SELECT a FROM AdressePing a"),
    @NamedQuery(name = "AdressePing.findByAdresseIP", query = "SELECT a FROM AdressePing a WHERE a.adresseIP = :adresseIP"),
    @NamedQuery(name = "AdressePing.findByNom", query = "SELECT a FROM AdressePing a WHERE a.nom = :nom")})
public class AdressePing implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "adresse_IP")
    private String adresseIP;
    @Size(max = 254)
    @Column(name = "nom")
    private String nom;
    @JoinColumns({
        @JoinColumn(name = "id_machine", referencedColumnName = "id_machine"),
        @JoinColumn(name = "cle_tache", referencedColumnName = "cle_tache")})
    @ManyToOne(optional = false)
    private Pings pings;

    public AdressePing() {
    }

    public AdressePing(String adresseIP) {
        this.adresseIP = adresseIP;
    }

    public String getAdresseIP() {
        return adresseIP;
    }

    public void setAdresseIP(String adresseIP) {
        this.adresseIP = adresseIP;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Pings getPings() {
        return pings;
    }

    public void setPings(Pings pings) {
        this.pings = pings;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (adresseIP != null ? adresseIP.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AdressePing)) {
            return false;
        }
        AdressePing other = (AdressePing) object;
        if ((this.adresseIP == null && other.adresseIP != null) || (this.adresseIP != null && !this.adresseIP.equals(other.adresseIP))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.AdressePing[ adresseIP=" + adresseIP + " ]";
    }
    
}
