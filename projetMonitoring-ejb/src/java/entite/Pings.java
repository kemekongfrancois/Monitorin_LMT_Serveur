/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entite;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author KEF10
 */
@Entity
@Table(name = "pings")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Pings.findAll", query = "SELECT p FROM Pings p"),
    @NamedQuery(name = "Pings.findByIdMachine", query = "SELECT p FROM Pings p WHERE p.pingsPK.idMachine = :idMachine"),
    @NamedQuery(name = "Pings.findByCleTache", query = "SELECT p FROM Pings p WHERE p.pingsPK.cleTache = :cleTache")})
public class Pings implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PingsPK pingsPK;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pings")
    private List<AdressePing> adressePingList;
    @JoinColumns({
        @JoinColumn(name = "id_machine", referencedColumnName = "id_machine", insertable = false, updatable = false),
        @JoinColumn(name = "cle_tache", referencedColumnName = "cle_tache", insertable = false, updatable = false)})
    @OneToOne(optional = false)
    private Tache tache;

    public Pings() {
    }

    public Pings(PingsPK pingsPK) {
        this.pingsPK = pingsPK;
    }

    public Pings(int idMachine, String cleTache) {
        this.pingsPK = new PingsPK(idMachine, cleTache);
    }

    public PingsPK getPingsPK() {
        return pingsPK;
    }

    public void setPingsPK(PingsPK pingsPK) {
        this.pingsPK = pingsPK;
    }

    @XmlTransient
    public List<AdressePing> getAdressePingList() {
        return adressePingList;
    }

    public void setAdressePingList(List<AdressePing> adressePingList) {
        this.adressePingList = adressePingList;
    }

    public Tache getTache() {
        return tache;
    }

    public void setTache(Tache tache) {
        this.tache = tache;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pingsPK != null ? pingsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pings)) {
            return false;
        }
        Pings other = (Pings) object;
        if ((this.pingsPK == null && other.pingsPK != null) || (this.pingsPK != null && !this.pingsPK.equals(other.pingsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.Pings[ pingsPK=" + pingsPK + " ]";
    }
    
}
