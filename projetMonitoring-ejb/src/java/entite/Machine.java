/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entite;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author KEF10
 */
@Entity
@Table(name = "machine")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Machine.findAll", query = "SELECT m FROM Machine m"),
    @NamedQuery(name = "Machine.findByIdMachine", query = "SELECT m FROM Machine m WHERE m.idMachine = :idMachine"),
    @NamedQuery(name = "Machine.findByAdresseIP", query = "SELECT m FROM Machine m WHERE m.adresseIP = :adresseIP"),
    @NamedQuery(name = "Machine.findByPortEcoute", query = "SELECT m FROM Machine m WHERE m.portEcoute = :portEcoute"),
    @NamedQuery(name = "Machine.findByNomMachine", query = "SELECT m FROM Machine m WHERE m.nomMachine = :nomMachine"),
    @NamedQuery(name = "Machine.findByTypeOS", query = "SELECT m FROM Machine m WHERE m.typeOS = :typeOS"),
    @NamedQuery(name = "Machine.findByPeriodeDeCheck", query = "SELECT m FROM Machine m WHERE m.periodeDeCheck = :periodeDeCheck"),
    @NamedQuery(name = "Machine.findByStatue", query = "SELECT m FROM Machine m WHERE m.statue = :statue")})
public class Machine implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_machine")
    private Integer idMachine;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "adresse_IP")
    private String adresseIP;
    @Size(max = 254)
    @Column(name = "Port_Ecoute")
    private String portEcoute;
    @Size(max = 254)
    @Column(name = "nom_machine")
    private String nomMachine;
    @Size(max = 254)
    @Column(name = "type_OS")
    private String typeOS;
    @Size(max = 254)
    @Column(name = "periode_de_check")
    private String periodeDeCheck;
    @Size(max = 254)
    @Column(name = "statue")
    private String statue;
    @OneToMany(mappedBy = "idMachine")
    private List<Tache> tacheList;

    public Machine() {
    }

    public Machine(Integer idMachine) {
        this.idMachine = idMachine;
    }

    public Machine(Integer idMachine, String adresseIP) {
        this.idMachine = idMachine;
        this.adresseIP = adresseIP;
    }

    public Integer getIdMachine() {
        return idMachine;
    }

    public void setIdMachine(Integer idMachine) {
        this.idMachine = idMachine;
    }

    public String getAdresseIP() {
        return adresseIP;
    }

    public void setAdresseIP(String adresseIP) {
        this.adresseIP = adresseIP;
    }

    public String getPortEcoute() {
        return portEcoute;
    }

    public void setPortEcoute(String portEcoute) {
        this.portEcoute = portEcoute;
    }

    public String getNomMachine() {
        return nomMachine;
    }

    public void setNomMachine(String nomMachine) {
        this.nomMachine = nomMachine;
    }

    public String getTypeOS() {
        return typeOS;
    }

    public void setTypeOS(String typeOS) {
        this.typeOS = typeOS;
    }

    public String getPeriodeDeCheck() {
        return periodeDeCheck;
    }

    public void setPeriodeDeCheck(String periodeDeCheck) {
        this.periodeDeCheck = periodeDeCheck;
    }

    public String getStatue() {
        return statue;
    }

    public void setStatue(String statue) {
        this.statue = statue;
    }

    @XmlTransient
    public List<Tache> getTacheList() {
        return tacheList;
    }

    public void setTacheList(List<Tache> tacheList) {
        this.tacheList = tacheList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMachine != null ? idMachine.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Machine)) {
            return false;
        }
        Machine other = (Machine) object;
        if ((this.idMachine == null && other.idMachine != null) || (this.idMachine != null && !this.idMachine.equals(other.idMachine))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.Machine[ idMachine=" + idMachine + " ]";
    }
    
}
