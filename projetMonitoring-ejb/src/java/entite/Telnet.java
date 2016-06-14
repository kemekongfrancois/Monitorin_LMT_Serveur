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
@Table(name = "telnet")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Telnet.findAll", query = "SELECT t FROM Telnet t"),
    @NamedQuery(name = "Telnet.findByIdMachine", query = "SELECT t FROM Telnet t WHERE t.telnetPK.idMachine = :idMachine"),
    @NamedQuery(name = "Telnet.findByCleTache", query = "SELECT t FROM Telnet t WHERE t.telnetPK.cleTache = :cleTache")})
public class Telnet implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TelnetPK telnetPK;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "telnet")
    private List<AdresseTelnet> adresseTelnetList;
    @JoinColumns({
        @JoinColumn(name = "id_machine", referencedColumnName = "id_machine", insertable = false, updatable = false),
        @JoinColumn(name = "cle_tache", referencedColumnName = "cle_tache", insertable = false, updatable = false)})
    @OneToOne(optional = false)
    private Tache tache;

    public Telnet() {
    }

    public Telnet(TelnetPK telnetPK) {
        this.telnetPK = telnetPK;
    }

    public Telnet(int idMachine, String cleTache) {
        this.telnetPK = new TelnetPK(idMachine, cleTache);
    }

    public TelnetPK getTelnetPK() {
        return telnetPK;
    }

    public void setTelnetPK(TelnetPK telnetPK) {
        this.telnetPK = telnetPK;
    }

    @XmlTransient
    public List<AdresseTelnet> getAdresseTelnetList() {
        return adresseTelnetList;
    }

    public void setAdresseTelnetList(List<AdresseTelnet> adresseTelnetList) {
        this.adresseTelnetList = adresseTelnetList;
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
        hash += (telnetPK != null ? telnetPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Telnet)) {
            return false;
        }
        Telnet other = (Telnet) object;
        if ((this.telnetPK == null && other.telnetPK != null) || (this.telnetPK != null && !this.telnetPK.equals(other.telnetPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.Telnet[ telnetPK=" + telnetPK + " ]";
    }
    
}
