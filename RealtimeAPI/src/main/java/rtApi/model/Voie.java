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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 *
 * @author Rémi Venant
 */
@Entity
@Table(name = "voies")
public class Voie {

    @Id
    @Column(name = "num_voie")
    private int numVoie;

    @Column(name = "interdite", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean interdite;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "voie")
    private Rame rame;

    protected Voie() {
    }

    public Voie(int numVoie) {
        this.numVoie = numVoie;
    }

    public Voie(int numVoie, boolean interdite) {
        this.numVoie = numVoie;
        this.interdite = interdite;
    }

    public int getNumVoie() {
        return numVoie;
    }

    protected void setNumVoie(int numVoie) {
        this.numVoie = numVoie;
    }

    public boolean isInterdite() {
        return interdite;
    }

    public void setInterdite(boolean interdite) {
        this.interdite = interdite;
    }

    public Rame getRame() {
        return rame;
    }

    protected void setRame(Rame rame) {
        this.rame = rame;
        if (this.rame.getVoie() == null || !this.rame.getVoie().equals(this)) {
            this.rame.setVoie(this);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.numVoie;
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
        final Voie other = (Voie) obj;
        return this.numVoie == other.numVoie;
    }

    @Override
    public String toString() {
        return "Voie{" + "numVoie=" + numVoie + ", interdite=" + interdite + '}';
    }

}
