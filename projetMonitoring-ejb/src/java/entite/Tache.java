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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "tache")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Tache.findAll", query = "SELECT t FROM Tache t"),
    @NamedQuery(name = "Tache.findByIdTache", query = "SELECT t FROM Tache t WHERE t.idTache = :idTache"),
    @NamedQuery(name = "Tache.findByNom", query = "SELECT t FROM Tache t WHERE t.nom = :nom"),
    @NamedQuery(name = "Tache.findByListeAdresse", query = "SELECT t FROM Tache t WHERE t.listeAdresse = :listeAdresse"),
    @NamedQuery(name = "Tache.findBySeuilAlerte", query = "SELECT t FROM Tache t WHERE t.seuilAlerte = :seuilAlerte"),
    @NamedQuery(name = "Tache.findByDescriptionFichier", query = "SELECT t FROM Tache t WHERE t.descriptionFichier = :descriptionFichier"),
    @NamedQuery(name = "Tache.findByStatue", query = "SELECT t FROM Tache t WHERE t.statue = :statue"),
    @NamedQuery(name = "Tache.findByPeriodeVerrification", query = "SELECT t FROM Tache t WHERE t.periodeVerrification = :periodeVerrification"),
    @NamedQuery(name = "Tache.findByTypeTache", query = "SELECT t FROM Tache t WHERE t.typeTache = :typeTache")})
public class Tache implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tache")
    private Integer idTache;
    @Size(max = 254)
    @Column(name = "nom")
    private String nom;
    @Size(max = 254)
    @Column(name = "liste_adresse")
    private String listeAdresse;
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
    @JoinColumn(name = "id_machine", referencedColumnName = "id_machine")
    @ManyToOne
    private Machine idMachine;

    public Tache() {
    }

    public Tache(Integer idTache) {
        this.idTache = idTache;
    }

    public Integer getIdTache() {
        return idTache;
    }

    public void setIdTache(Integer idTache) {
        this.idTache = idTache;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getListeAdresse() {
        return listeAdresse;
    }

    public void setListeAdresse(String listeAdresse) {
        this.listeAdresse = listeAdresse;
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
        if (!(object instanceof Tache)) {
            return false;
        }
        Tache other = (Tache) object;
        if ((this.idTache == null && other.idTache != null) || (this.idTache != null && !this.idTache.equals(other.idTache))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.Tache[ idTache=" + idTache + " ]";
    }
    
}
