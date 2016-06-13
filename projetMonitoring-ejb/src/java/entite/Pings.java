/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entite;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "pings")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Pings.findAll", query = "SELECT p FROM Pings p"),
    @NamedQuery(name = "Pings.findByIdTache", query = "SELECT p FROM Pings p WHERE p.idTache = :idTache"),
    @NamedQuery(name = "Pings.findByPeriodeVerrification", query = "SELECT p FROM Pings p WHERE p.periodeVerrification = :periodeVerrification"),
    @NamedQuery(name = "Pings.findByNom", query = "SELECT p FROM Pings p WHERE p.nom = :nom"),
    @NamedQuery(name = "Pings.findByStatue", query = "SELECT p FROM Pings p WHERE p.statue = :statue")})
public class Pings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_tache")
    private Integer idTache;
    @Size(max = 254)
    @Column(name = "periode_verrification")
    private String periodeVerrification;
    @Size(max = 254)
    @Column(name = "nom")
    private String nom;
    @Size(max = 254)
    @Column(name = "statue")
    private String statue;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idTache")
    private List<AdressePing> adressePingList;
    @JoinColumn(name = "id_machine", referencedColumnName = "id_machine")
    @ManyToOne
    private Machine idMachine;

    public Pings() {
    }

    public Pings(Integer idTache) {
        this.idTache = idTache;
    }

    public Integer getIdTache() {
        return idTache;
    }

    public void setIdTache(Integer idTache) {
        this.idTache = idTache;
    }

    public String getPeriodeVerrification() {
        return periodeVerrification;
    }

    public void setPeriodeVerrification(String periodeVerrification) {
        this.periodeVerrification = periodeVerrification;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getStatue() {
        return statue;
    }

    public void setStatue(String statue) {
        this.statue = statue;
    }

    @XmlTransient
    public List<AdressePing> getAdressePingList() {
        return adressePingList;
    }

    public void setAdressePingList(List<AdressePing> adressePingList) {
        this.adressePingList = adressePingList;
    }

    public Machine getIdMachine() {
        return idMachine;
    }

    public void setIdMachine(Machine idMachine) {
        this.idMachine = idMachine;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTache != null ? idTache.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pings)) {
            return false;
        }
        Pings other = (Pings) object;
        if ((this.idTache == null && other.idTache != null) || (this.idTache != null && !this.idTache.equals(other.idTache))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.Pings[ idTache=" + idTache + " ]";
    }
    
}
