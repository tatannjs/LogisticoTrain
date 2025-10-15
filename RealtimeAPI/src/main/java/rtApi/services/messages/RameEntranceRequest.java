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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 *
 * @author Rémi Venant
 */
public class RameEntranceRequest extends RameMessage {

    @NotBlank
    @Size(max = 50)
    private String typeRame;

    @NotEmpty
    private List<String> taches;

    public String getTypeRame() {
        return typeRame;
    }

    public void setTypeRame(String typeRame) {
        this.typeRame = typeRame;
    }

    public List<String> getTaches() {
        return taches;
    }

    public void setTaches(List<String> taches) {
        this.taches = taches;
    }

}
