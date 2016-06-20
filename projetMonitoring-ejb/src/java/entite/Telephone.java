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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author KEF10
 */
@Entity
@Table(name = "telephone")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Telephone.findAll", query = "SELECT t FROM Telephone t"),
    @NamedQuery(name = "Telephone.findByIdNumeroTelephone", query = "SELECT t FROM Telephone t WHERE t.idNumeroTelephone = :idNumeroTelephone"),
    @NamedQuery(name = "Telephone.findByNumeroTelephone", query = "SELECT t FROM Telephone t WHERE t.numeroTelephone = :numeroTelephone")})
public class Telephone implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_numero_telephone")
    private Integer idNumeroTelephone;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "numero_telephone")
    private String numeroTelephone;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur")
    @ManyToOne(optional = false)
    private Utilisateur idUtilisateur;

    public Telephone() {
    }

    public Telephone(Integer idNumeroTelephone) {
        this.idNumeroTelephone = idNumeroTelephone;
    }

    public Telephone(Integer idNumeroTelephone, String numeroTelephone) {
        this.idNumeroTelephone = idNumeroTelephone;
        this.numeroTelephone = numeroTelephone;
    }

    public Integer getIdNumeroTelephone() {
        return idNumeroTelephone;
    }

    public void setIdNumeroTelephone(Integer idNumeroTelephone) {
        this.idNumeroTelephone = idNumeroTelephone;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public Utilisateur getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Utilisateur idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idNumeroTelephone != null ? idNumeroTelephone.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Telephone)) {
            return false;
        }
        Telephone other = (Telephone) object;
        if ((this.idNumeroTelephone == null && other.idNumeroTelephone != null) || (this.idNumeroTelephone != null && !this.idNumeroTelephone.equals(other.idNumeroTelephone))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.Telephone[ idNumeroTelephone=" + idNumeroTelephone + " ]";
    }
    
}
