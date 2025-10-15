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

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import rtApi.model.HistoryAction;
import rtApi.model.HistoryActionRepository;
import rtApi.model.Rame;
import rtApi.model.RameRepository;
import rtApi.model.Tache;
import rtApi.model.Voie;
import rtApi.model.VoieRepository;
import rtApi.services.messages.RameEntranceAnswer;
import rtApi.services.messages.RameEntranceRequest;
import rtApi.services.messages.RameRemoveRequest;

/**
 *
 * @author Rémi Venant
 */
@Service
@Validated
public class RameAccessServiceImpl implements RameAccessService {

    private final VoieRepository voieRepo;

    private final RameRepository rameRepo;

    private final HistoryActionRepository historyActionRepo;

    @Autowired
    public RameAccessServiceImpl(VoieRepository voieRepo, RameRepository rameRepo,
            HistoryActionRepository historyActionRepo) {
        this.voieRepo = voieRepo;
        this.rameRepo = rameRepo;
        this.historyActionRepo = historyActionRepo;
    }

    @Override
    @Transactional
    public Rame askRameEntrance(RameEntranceRequest request) throws ConstraintViolationException, DuplicateKeyException {
        // Check if a rame already exist
        if (this.rameRepo.existsById(request.getNumSerie())) {
            throw new DuplicateKeyException("Rame with same numSerie already exist");
        }
        // Attempt to create a new Rame, and its taches to do
        Rame rame = new Rame(request.getNumSerie(), request.getTypeRame(), request.getAuteur());
        Set<Tache> rameTaches = rame.getTaches();
        Iterator<String> itRawTache = request.getTaches().iterator();
        int numTache = 1;
        while (itRawTache.hasNext()) {
            rameTaches.add(new Tache(rame, numTache, itRawTache.next()));
            numTache++;
        }
        rame = this.rameRepo.save(rame);
        // Attempt to create a new history action
        this.historyActionRepo.save(HistoryAction.createForDemande(request.getNumSerie(),
                request.getTypeRame(), request.getAuteur(), request.getTaches()));

        return rame;
    }

    @Override
    @Transactional
    public Rame answerRameEntranceRequest(RameEntranceAnswer answer) throws ConstraintViolationException, IllegalArgumentException, NoSuchElementException, DuplicateKeyException {
        // if accept, voie number must be given
        if (answer.getAccept() && answer.getVoie() == null) {
            throw new IllegalArgumentException("Missing voie to accept request");
        }
        // Retrieve rame
        Rame rame = this.rameRepo.findById(answer.getNumSerie()).orElseThrow(() -> new NoSuchElementException("Unknown rame"));
        // Check that rame has no voie yet
        if (rame.getVoie() != null) {
            throw new DuplicateKeyException("Rame déjà acceptée");
        }
        // Process according to the answer
        if (answer.getAccept()) {
            // Retrieve voie that should not be interdite may raise
            final Voie voie = this.voieRepo.findByNumVoieAndInterditeFalse(answer.getVoie()).orElseThrow(() -> new NoSuchElementException("Unknown voie"));
            // Check voie has no rame yet
            if (voie.getRame() != null) {
                throw new DuplicateKeyException("Voie déjà utilisée");
            }
            // Attempt to save voie (may raise PersistenceException)
            rame.setVoie(voie);
            rame = this.rameRepo.save(rame);
            // Attempt to create a new history action
            this.historyActionRepo.save(HistoryAction.createForEntree(rame.getNumSerie(),
                    rame.getTypeRame(), answer.getAuteur(), voie.getNumVoie()));
        } else {
            // remove Rame
            this.rameRepo.delete(rame);
            this.historyActionRepo.save(HistoryAction.createForRejet(rame.getNumSerie(),
                    rame.getTypeRame(), answer.getAuteur()));
        }

        return rame;
    }

    @Override
    @Transactional
    public void removeRame(RameRemoveRequest request) throws ConstraintViolationException, NoSuchElementException, IllegalArgumentException {
        // Retrieve rame
        Rame rame = this.rameRepo.findById(request.getNumSerie()).orElseThrow(() -> new NoSuchElementException("Unknown rame"));
        // Check that the rame has a voie and it matches the request
        if (rame.getVoie() == null || rame.getVoie().getNumVoie() != request.getVoie()) {
            throw new IllegalArgumentException("Wrong voie for the rame to be removed");
        }
        // Check that the rame does not have any task
        if (rame.getTaches() != null && !rame.getTaches().isEmpty()) {
            throw new IllegalArgumentException("The rame has still task to be done before being removed");
        }
        // Remove the voie
        this.rameRepo.delete(rame);
        // Create a history action
        this.historyActionRepo.save(HistoryAction.createForSortie(rame.getNumSerie(), rame.getTypeRame(), request.getAuteur(), request.getVoie()));
    }

}
