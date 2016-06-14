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
public class TelnetPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "id_machine")
    private int idMachine;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "cle_tache")
    private String cleTache;

    public TelnetPK() {
    }

    public TelnetPK(int idMachine, String cleTache) {
        this.idMachine = idMachine;
        this.cleTache = cleTache;
    }

    public int getIdMachine() {
        return idMachine;
    }

    public void setIdMachine(int idMachine) {
        this.idMachine = idMachine;
    }

    public String getCleTache() {
        return cleTache;
    }

    public void setCleTache(String cleTache) {
        this.cleTache = cleTache;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idMachine;
        hash += (cleTache != null ? cleTache.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TelnetPK)) {
            return false;
        }
        TelnetPK other = (TelnetPK) object;
        if (this.idMachine != other.idMachine) {
            return false;
        }
        if ((this.cleTache == null && other.cleTache != null) || (this.cleTache != null && !this.cleTache.equals(other.cleTache))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.TelnetPK[ idMachine=" + idMachine + ", cleTache=" + cleTache + " ]";
    }
    
}
