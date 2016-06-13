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
@Table(name = "surveille_fichier_taille")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SurveilleFichierTaille.findAll", query = "SELECT s FROM SurveilleFichierTaille s"),
    @NamedQuery(name = "SurveilleFichierTaille.findByIdTache", query = "SELECT s FROM SurveilleFichierTaille s WHERE s.idTache = :idTache"),
    @NamedQuery(name = "SurveilleFichierTaille.findByCheminDuFichier", query = "SELECT s FROM SurveilleFichierTaille s WHERE s.cheminDuFichier = :cheminDuFichier"),
    @NamedQuery(name = "SurveilleFichierTaille.findByPeriodeVerrification", query = "SELECT s FROM SurveilleFichierTaille s WHERE s.periodeVerrification = :periodeVerrification"),
    @NamedQuery(name = "SurveilleFichierTaille.findByNom", query = "SELECT s FROM SurveilleFichierTaille s WHERE s.nom = :nom"),
    @NamedQuery(name = "SurveilleFichierTaille.findByStatue", query = "SELECT s FROM SurveilleFichierTaille s WHERE s.statue = :statue")})
public class SurveilleFichierTaille implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_tache")
    private Integer idTache;
    @Size(max = 254)
    @Column(name = "chemin_du_fichier")
    private String cheminDuFichier;
    @Size(max = 254)
    @Column(name = "periode_verrification")
    private String periodeVerrification;
    @Size(max = 254)
    @Column(name = "nom")
    private String nom;
    @Size(max = 254)
    @Column(name = "statue")
    private String statue;
    @JoinColumn(name = "id_machine", referencedColumnName = "id_machine")
    @ManyToOne
    private Machine idMachine;

    public SurveilleFichierTaille() {
    }

    public SurveilleFichierTaille(Integer idTache) {
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
        if (!(object instanceof SurveilleFichierTaille)) {
            return false;
        }
        SurveilleFichierTaille other = (SurveilleFichierTaille) object;
        if ((this.idTache == null && other.idTache != null) || (this.idTache != null && !this.idTache.equals(other.idTache))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.SurveilleFichierTaille[ idTache=" + idTache + " ]";
    }
    
}
