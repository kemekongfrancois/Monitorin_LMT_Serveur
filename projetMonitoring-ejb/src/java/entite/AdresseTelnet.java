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
@Table(name = "adresse_telnet")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdresseTelnet.findAll", query = "SELECT a FROM AdresseTelnet a"),
    @NamedQuery(name = "AdresseTelnet.findByAdresseIP", query = "SELECT a FROM AdresseTelnet a WHERE a.adresseIP = :adresseIP"),
    @NamedQuery(name = "AdresseTelnet.findByNom", query = "SELECT a FROM AdresseTelnet a WHERE a.nom = :nom")})
public class AdresseTelnet implements Serializable {

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
    private Telnet telnet;

    public AdresseTelnet() {
    }

    public AdresseTelnet(String adresseIP) {
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

    public Telnet getTelnet() {
        return telnet;
    }

    public void setTelnet(Telnet telnet) {
        this.telnet = telnet;
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
        if (!(object instanceof AdresseTelnet)) {
            return false;
        }
        AdresseTelnet other = (AdresseTelnet) object;
        if ((this.adresseIP == null && other.adresseIP != null) || (this.adresseIP != null && !this.adresseIP.equals(other.adresseIP))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.AdresseTelnet[ adresseIP=" + adresseIP + " ]";
    }
    
}
