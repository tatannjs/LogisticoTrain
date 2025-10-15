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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "actions")
public class HistoryAction {

    @Id
    private String id;

    @Size(max = 12)
    @NotNull
    private String numSerie;

    @Size(max = 50)
    @NotNull
    private String typeRame;

    @NotNull
    private ActionType action;

    @Size(max = 50)
    @NotBlank
    @Indexed(name = "auteur", unique = false)
    private String auteur;

    @CreatedDate
    private LocalDateTime timestamp;

    private Integer voie;

    private List<String> taches;

    protected HistoryAction() {
    }

    public HistoryAction(String numSerie, String typeRame, ActionType action, String auteur, Integer voie, List<String> taches) {
        this.numSerie = numSerie;
        this.typeRame = typeRame;
        this.action = action;
        this.auteur = auteur;
        this.voie = voie;
        this.taches = taches;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
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

    protected void setTypeRame(String typeRame) {
        this.typeRame = typeRame;
    }

    public ActionType getAction() {
        return action;
    }

    protected void setAction(ActionType action) {
        this.action = action;
    }

    public String getAuteur() {
        return auteur;
    }

    protected void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getVoie() {
        return voie;
    }

    protected void setVoie(Integer voie) {
        this.voie = voie;
    }

    public List<String> getTaches() {
        return taches;
    }

    protected void setTaches(List<String> taches) {
        this.taches = taches;
    }

    @Override
    public String toString() {
        return "HistoryAction{" + "id=" + id + ", numSerie=" + numSerie + ", typeRame=" + typeRame + ", action=" + action + ", auteur=" + auteur + '}';
    }

    public static HistoryAction createForDemande(String numSerie, String typeRame, String auteur, List<String> taches) {
        return new HistoryAction(numSerie, typeRame, ActionType.demande, auteur, null, taches);
    }

    public static HistoryAction createForEntree(String numSerie, String typeRame, String auteur, Integer voie) {
        return new HistoryAction(numSerie, typeRame, ActionType.entree, auteur, voie, null);
    }

    public static HistoryAction createForRejet(String numSerie, String typeRame, String auteur) {
        return new HistoryAction(numSerie, typeRame, ActionType.rejet, auteur, null, null);
    }

    public static HistoryAction createForSortie(String numSerie, String typeRame, String auteur, Integer voie) {
        return new HistoryAction(numSerie, typeRame, ActionType.sortie, auteur, voie, null);
    }
}
