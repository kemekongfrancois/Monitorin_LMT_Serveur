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
@Table(name = "surveille_fichier_existe")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SurveilleFichierExiste.findAll", query = "SELECT s FROM SurveilleFichierExiste s"),
    @NamedQuery(name = "SurveilleFichierExiste.findByIdTache", query = "SELECT s FROM SurveilleFichierExiste s WHERE s.idTache = :idTache"),
    @NamedQuery(name = "SurveilleFichierExiste.findByCheminDuFichier", query = "SELECT s FROM SurveilleFichierExiste s WHERE s.cheminDuFichier = :cheminDuFichier"),
    @NamedQuery(name = "SurveilleFichierExiste.findByPeriodeVerrification", query = "SELECT s FROM SurveilleFichierExiste s WHERE s.periodeVerrification = :periodeVerrification"),
    @NamedQuery(name = "SurveilleFichierExiste.findByStatue", query = "SELECT s FROM SurveilleFichierExiste s WHERE s.statue = :statue")})
public class SurveilleFichierExiste implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_tache")
    private Integer idTache;
    @Size(max = 254)
    @Column(name = "chemin_du_fichier")
    private String cheminDuFichier;
    @Column(name = "periode_verrification")
    private Integer periodeVerrification;
    @Size(max = 254)
    @Column(name = "statue")
    private String statue;
    @JoinColumn(name = "id_machine", referencedColumnName = "id_machine")
    @ManyToOne
    private Machine idMachine;

    public SurveilleFichierExiste() {
    }

    public SurveilleFichierExiste(Integer idTache) {
        this.idTache = idTache;
    }

    public Integer getIdTache() {
        return idTache;
    }

    public void setIdTache(Integer idTache) {
        this.idTache = idTache;
    }

    public String getCheminDuFichier() {
        return cheminDuFichier;
    }

    public void setCheminDuFichier(String cheminDuFichier) {
        this.cheminDuFichier = cheminDuFichier;
    }

    public Integer getPeriodeVerrification() {
        return periodeVerrification;
    }

    public void setPeriodeVerrification(Integer periodeVerrification) {
        this.periodeVerrification = periodeVerrification;
    }

    public String getStatue() {
        return statue;
    }

    public void setStatue(String statue) {
        this.statue = statue;
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
        if (!(object instanceof SurveilleFichierExiste)) {
            return false;
        }
        SurveilleFichierExiste other = (SurveilleFichierExiste) object;
        if ((this.idTache == null && other.idTache != null) || (this.idTache != null && !this.idTache.equals(other.idTache))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.SurveilleFichierExiste[ idTache=" + idTache + " ]";
    }
    
}
