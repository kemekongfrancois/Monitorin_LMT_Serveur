/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entite;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author KEF10
 */
@Embeddable
public class SurveillerDdPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "id_tache")
    private int idTache;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "lettre_partition")
    private String lettrePartition;

    public SurveillerDdPK() {
    }

    public SurveillerDdPK(int idTache, String lettrePartition) {
        this.idTache = idTache;
        this.lettrePartition = lettrePartition;
    }

    public int getIdTache() {
        return idTache;
    }

    public void setIdTache(int idTache) {
        this.idTache = idTache;
    }

    public String getLettrePartition() {
        return lettrePartition;
    }

    public void setLettrePartition(String lettrePartition) {
        this.lettrePartition = lettrePartition;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idTache;
        hash += (lettrePartition != null ? lettrePartition.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SurveillerDdPK)) {
            return false;
        }
        SurveillerDdPK other = (SurveillerDdPK) object;
        if (this.idTache != other.idTache) {
            return false;
        }
        if ((this.lettrePartition == null && other.lettrePartition != null) || (this.lettrePartition != null && !this.lettrePartition.equals(other.lettrePartition))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.SurveillerDdPK[ idTache=" + idTache + ", lettrePartition=" + lettrePartition + " ]";
    }
    
}
