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
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 *
 * @author Rémi Venant
 */
@Embeddable
public class TachePK {

    private String numSerie;

    @Column(name = "num_tache")
    private int numTache;

    protected TachePK() {
    }

    public TachePK(String numSerie, int numTache) {
        this.numSerie = numSerie;
        this.numTache = numTache;
    }

    public TachePK(Rame rame, int numTache) {
        this.numSerie = rame.getNumSerie();
        this.numTache = numTache;
    }

    public String getNumSerie() {
        return numSerie;
    }

    protected void setNumSerie(String numSerie) {
        this.numSerie = numSerie;
    }

    public int getNumTache() {
        return numTache;
    }

    protected void setNumTache(int numTache) {
        this.numTache = numTache;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.numSerie);
        hash = 47 * hash + this.numTache;
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
        final TachePK other = (TachePK) obj;
        if (this.numTache != other.numTache) {
            return false;
        }
        return Objects.equals(this.numSerie, other.numSerie);
    }

    @Override
    public String toString() {
        return "TachePK{" + numSerie + ":" + numTache + '}';
    }

}
