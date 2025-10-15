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
package rtApi.services.messages;

import jakarta.validation.constraints.NotNull;

/**
 *
 * @author Rémi Venant
 */
public class RameEntranceAnswer extends RameMessage {

    @NotNull
    private Boolean accept;

    private Integer voie;

    public Boolean getAccept() {
        return accept;
    }

    public void setAccept(Boolean accept) {
        this.accept = accept;
    }

    public Integer getVoie() {
        return voie;
    }

    public void setVoie(Integer voie) {
        this.voie = voie;
    }

}
