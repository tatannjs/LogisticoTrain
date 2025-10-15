/*
 * Copyright (C) 2024 Rémi Venant
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package rtApi.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Rémi Venant
 */
@Entity
@Table(name = "rames")
public class Rame {

    @Id
    @Column(name = "num_serie", length = 12)
    private String numSerie;

    @Column(name = "type_rame", length = 50, nullable = false)
    private String typeRame;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voie", nullable = true)
    private Voie voie;

    @Column(name = "conducteur_entrant", length = 50, nullable = false)
    private String conducteurEntrant;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "rame", orphanRemoval = true)
    private Set<Tache> taches = new HashSet<>();

    protected Rame() {
    }

    public Rame(String numSerie, String typeRame, String conducteurEntrant) {
        this.numSerie = numSerie;
        this.typeRame = typeRame;
        this.conducteurEntrant = conducteurEntrant;
    }

    public String getNumSerie() {
        return numSerie;
    }

    protected void setNumSerie(String numSerie) {
        this.numSerie = numSerie;
    }

    public String getTypeRame() {
        return typeRame;
    }

    public void setTypeRame(String typeRame) {
        this.typeRame = typeRame;
    }

    public Voie getVoie() {
        return voie;
    }

    public void setVoie(Voie voie) {
        this.voie = voie;
        if (this.voie.getRame() == null || !this.voie.getRame().equals(this)) {
            this.voie.setRame(this);
        }
    }

    public String getConducteurEntrant() {
        return conducteurEntrant;
    }

    public void setConducteurEntrant(String conducteurEntrant) {
        this.conducteurEntrant = conducteurEntrant;
    }

    public Set<Tache> getTaches() {
        return taches;
    }

    protected void setTaches(Set<Tache> taches) {
        this.taches = taches;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.numSerie);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rame other = (Rame) obj;
        return Objects.equals(this.numSerie, other.numSerie);
    }

    @Override
    public String toString() {
        return "Rame{" + "numSerie=" + numSerie + ", typeRame=" + typeRame + ", voie=" + voie + '}';
    }

}
