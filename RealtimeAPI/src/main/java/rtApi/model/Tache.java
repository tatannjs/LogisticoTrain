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
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 *
 * @author Rémi Venant
 */
@Entity
@Table(name = "taches")
public class Tache {

    @EmbeddedId
    private TachePK id;

    @Lob
    @Column(length = 512, nullable = false)
    private String tache;

    @MapsId(value = "numSerie")
    @JoinColumn(name = "num_serie", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Rame rame;

    protected Tache() {
    }

    public Tache(Rame rame, int numTache, String tache) {
        this.id = new TachePK(rame, numTache);
        this.rame = rame;
        this.tache = tache;
    }

    public TachePK getId() {
        return id;
    }

    protected void setId(TachePK id) {
        this.id = id;
    }

    public String getTache() {
        return tache;
    }

    public void setTache(String tache) {
        this.tache = tache;
    }

    public Rame getRame() {
        return rame;
    }

    protected void setRame(Rame rame) {
        this.rame = rame;
        this.id.setNumSerie(rame.getNumSerie());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.id);
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
        final Tache other = (Tache) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Tache{" + "id=" + id + '}';
    }

}
