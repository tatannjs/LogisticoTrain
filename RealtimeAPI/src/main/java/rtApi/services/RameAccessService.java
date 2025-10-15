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
package rtApi.services;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.NoSuchElementException;
import org.springframework.dao.DuplicateKeyException;
import rtApi.model.Rame;
import rtApi.services.messages.RameEntranceAnswer;
import rtApi.services.messages.RameEntranceRequest;
import rtApi.services.messages.RameRemoveRequest;

/**
 *
 * @author Rémi Venant
 */
public interface RameAccessService {

    /**
     * Register a rame entrance request. Will create a Rame, its tasks and the
     * demande HistoryAction. If an error happend, will do nothing.
     *
     * @param request the rame entrance request
     * @return the create rame in db
     * @throws ConstraintViolationException if one of the parameter is invalid
     * @throws DuplicateKeyException if the rame numSerie already exist in db
     */
    Rame askRameEntrance(@NotNull @Valid RameEntranceRequest request) throws ConstraintViolationException, DuplicateKeyException;

    /**
     * Accept or reject a rame entrace request. If accept: Will update the Rame
     * with the voie and create the accept HistoryAction If reject: will remove
     * the rame and its task, and create the reject historyAction If an error
     * happend, will do nothing.
     *
     * @param answer the rame entrance answer
     * @return the updated or deleted rame
     * @throws ConstraintViolationException if one of the parameter is invalid
     * @throws IllegalArgumentException if accept but no voie provided
     * @throws NoSuchElementException if the rame is unknown or if the voie (if
     * accept) is unknown or forbidden
     * @throws DuplicateKeyException if the voie is already used or if the rame
     * is already set on a voie
     */
    Rame answerRameEntranceRequest(@NotNull @Valid RameEntranceAnswer answer) throws ConstraintViolationException, IllegalArgumentException, NoSuchElementException, DuplicateKeyException;

    /**
     * Remove a rame from the entrepot. The rame must be free of task, a one the
     * proper voie. The rame will be removed from db and an action will be set
     *
     * @param request the request
     * @throws ConstraintViolationException if one of the parameters is invalid
     * @throws NoSuchElementException if the rame is unknown
     * @throws IllegalArgumentException if the voie does not match or if the
     * rame has still task to be done
     */
    void removeRame(@NotNull @Valid RameRemoveRequest request) throws ConstraintViolationException, NoSuchElementException, IllegalArgumentException;
}
