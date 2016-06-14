/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entite;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author KEF10
 */
@Entity
@Table(name = "tache")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Tache.findAll", query = "SELECT t FROM Tache t"),
    @NamedQuery(name = "Tache.findByIdMachine", query = "SELECT t FROM Tache t WHERE t.tachePK.idMachine = :idMachine"),
    @NamedQuery(name = "Tache.findByCleTache", query = "SELECT t FROM Tache t WHERE t.tachePK.cleTache = :cleTache"),
    @NamedQuery(name = "Tache.findBySeuilAlerte", query = "SELECT t FROM Tache t WHERE t.seuilAlerte = :seuilAlerte"),
    @NamedQuery(name = "Tache.findByDescriptionFichier", query = "SELECT t FROM Tache t WHERE t.descriptionFichier = :descriptionFichier"),
    @NamedQuery(name = "Tache.findByStatue", query = "SELECT t FROM Tache t WHERE t.statue = :statue"),
    @NamedQuery(name = "Tache.findByPeriodeVerrification", query = "SELECT t FROM Tache t WHERE t.periodeVerrification = :periodeVerrification"),
    @NamedQuery(name = "Tache.findByTypeTache", query = "SELECT t FROM Tache t WHERE t.typeTache = :typeTache")})
public class Tache implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TachePK tachePK;
    @Column(name = "seuil_alerte")
    private Integer seuilAlerte;
    @Size(max = 254)
    @Column(name = "description_fichier")
    private String descriptionFichier;
    @Size(max = 254)
    @Column(name = "statue")
    private String statue;
    @Size(max = 254)
    @Column(name = "periode_verrification")
    private String periodeVerrification;
    @Size(max = 254)
    @Column(name = "type_tache")
    private String typeTache;
    @JoinColumn(name = "id_machine", referencedColumnName = "id_machine", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Machine machine;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "tache")
    private Telnet telnet;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "tache")
    private Pings pings;

    public Tache() {
    }

    public Tache(TachePK tachePK) {
        this.tachePK = tachePK;
    }

    public Tache(int idMachine, String cleTache) {
        this.tachePK = new TachePK(idMachine, cleTache);
    }

    public TachePK getTachePK() {
        return tachePK;
    }

    public void setTachePK(TachePK tachePK) {
        this.tachePK = tachePK;
    }

    public Integer getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(Integer seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public String getDescriptionFichier() {
        return descriptionFichier;
    }

    public void setDescriptionFichier(String descriptionFichier) {
        this.descriptionFichier = descriptionFichier;
    }

    public String getStatue() {
        return statue;
    }

    public void setStatue(String statue) {
        this.statue = statue;
    }

    public String getPeriodeVerrification() {
        return periodeVerrification;
    }

    public void setPeriodeVerrification(String periodeVerrification) {
        this.periodeVerrification = periodeVerrification;
    }

    public String getTypeTache() {
        return typeTache;
    }

    public void setTypeTache(String typeTache) {
        this.typeTache = typeTache;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Telnet getTelnet() {
        return telnet;
    }

    public void setTelnet(Telnet telnet) {
        this.telnet = telnet;
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
        hash += (tachePK != null ? tachePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Tache)) {
            return false;
        }
        Tache other = (Tache) object;
        if ((this.tachePK == null && other.tachePK != null) || (this.tachePK != null && !this.tachePK.equals(other.tachePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.Tache[ tachePK=" + tachePK + " ]";
    }
    
}
