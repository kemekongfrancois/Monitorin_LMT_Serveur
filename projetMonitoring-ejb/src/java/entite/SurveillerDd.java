/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entite;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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
@Table(name = "surveiller_dd")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SurveillerDd.findAll", query = "SELECT s FROM SurveillerDd s"),
    @NamedQuery(name = "SurveillerDd.findByIdTache", query = "SELECT s FROM SurveillerDd s WHERE s.surveillerDdPK.idTache = :idTache"),
    @NamedQuery(name = "SurveillerDd.findByStatue", query = "SELECT s FROM SurveillerDd s WHERE s.statue = :statue"),
    @NamedQuery(name = "SurveillerDd.findByLettrePartition", query = "SELECT s FROM SurveillerDd s WHERE s.surveillerDdPK.lettrePartition = :lettrePartition"),
    @NamedQuery(name = "SurveillerDd.findByPeriodeVerrification", query = "SELECT s FROM SurveillerDd s WHERE s.periodeVerrification = :periodeVerrification"),
    @NamedQuery(name = "SurveillerDd.findBySeuilPourAlerte", query = "SELECT s FROM SurveillerDd s WHERE s.seuilPourAlerte = :seuilPourAlerte")})
public class SurveillerDd implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected SurveillerDdPK surveillerDdPK;
    @Size(max = 254)
    @Column(name = "statue")
    private String statue;
    @Column(name = "periode_verrification")
    private Integer periodeVerrification;
    @Column(name = "seuil_pour_alerte")
    private Integer seuilPourAlerte;
    @JoinColumn(name = "id_machine", referencedColumnName = "id_machine")
    @ManyToOne
    private Machine idMachine;

    public SurveillerDd() {
    }

    public SurveillerDd(SurveillerDdPK surveillerDdPK) {
        this.surveillerDdPK = surveillerDdPK;
    }

    public SurveillerDd(int idTache, String lettrePartition) {
        this.surveillerDdPK = new SurveillerDdPK(idTache, lettrePartition);
    }

    public SurveillerDdPK getSurveillerDdPK() {
        return surveillerDdPK;
    }

    public void setSurveillerDdPK(SurveillerDdPK surveillerDdPK) {
        this.surveillerDdPK = surveillerDdPK;
    }

    public String getStatue() {
        return statue;
    }

    public void setStatue(String statue) {
        this.statue = statue;
    }

    public Integer getPeriodeVerrification() {
        return periodeVerrification;
    }

    public void setPeriodeVerrification(Integer periodeVerrification) {
        this.periodeVerrification = periodeVerrification;
    }

    public Integer getSeuilPourAlerte() {
        return seuilPourAlerte;
    }

    public void setSeuilPourAlerte(Integer seuilPourAlerte) {
        this.seuilPourAlerte = seuilPourAlerte;
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
        hash += (surveillerDdPK != null ? surveillerDdPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SurveillerDd)) {
            return false;
        }
        SurveillerDd other = (SurveillerDd) object;
        if ((this.surveillerDdPK == null && other.surveillerDdPK != null) || (this.surveillerDdPK != null && !this.surveillerDdPK.equals(other.surveillerDdPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entite.SurveillerDd[ surveillerDdPK=" + surveillerDdPK + " ]";
    }
    
}
