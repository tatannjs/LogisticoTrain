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
package rtApi;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.StreamSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.test.context.TestComponent;
import rtApi.model.HistoryAction;
import rtApi.model.HistoryActionRepository;
import rtApi.model.Rame;
import rtApi.model.RameRepository;
import rtApi.model.Tache;
import rtApi.model.TacheRepository;
import rtApi.model.Voie;
import rtApi.model.VoieRepository;

/**
 *
 * @author Rémi Venant
 */
@TestComponent
public class TestDatasetLoader {

    private static final Log LOG = LogFactory.getLog(TestDatasetLoader.class);

    private final EntityManager em;

    private final RameRepository rameRepo;

    private final TacheRepository tacheRepo;

    private final VoieRepository voieRepo;

    private final HistoryActionRepository historyActionRepo;

    public TestDatasetLoader(EntityManager em, RameRepository rameRepo, VoieRepository voieRepo,
            TacheRepository tacheRepo, HistoryActionRepository historyActionRepo) {
        this.em = em;
        this.rameRepo = rameRepo;
        this.voieRepo = voieRepo;
        this.tacheRepo = tacheRepo;
        this.historyActionRepo = historyActionRepo;
    }

    @Transactional
    public void load() {
        // Create 3 voies: 1 will be occupied, 2 will be forbiden, 3 will be free
        List<Voie> voies = StreamSupport.stream(this.voieRepo.saveAll(List.of(new Voie(1), new Voie(2, true), new Voie(3))).spliterator(), false).toList();
        // Create 1 rame with 2 task : will be on voie 1
        Rame rame1 = new Rame("serRame1", "typeRame1", "conducteurRame1");
        rame1.getTaches().addAll(List.of(new Tache(rame1, 1, "tache rame1 - 1"), new Tache(rame1, 2, "tache rame1 - 2")));
        rame1 = this.rameRepo.save(rame1);
        rame1.setVoie(voies.get(0));
        rame1 = this.rameRepo.save(rame1);
        // Create action history foir rame 1 : demande and accepted
        this.historyActionRepo.saveAll(List.of(
                HistoryAction.createForDemande(rame1.getNumSerie(), rame1.getTypeRame(),
                        rame1.getConducteurEntrant(), rame1.getTaches().stream().map(Tache::getTache).toList()),
                HistoryAction.createForEntree(rame1.getNumSerie(), rame1.getTypeRame(), "operateur", rame1.getVoie().getNumVoie())));

        // Create 1 rame with 2 task, not set on voie yet
        Rame rame2 = new Rame("serRame2", "typeRame2", "conducteurRame2");
        rame2.getTaches().addAll(List.of(new Tache(rame2, 1, "tache rame2 - 1"), new Tache(rame2, 2, "tache rame2 - 2")));
        rame2 = this.rameRepo.save(rame2);
        // Create action history foir rame 1 : demande and accepted
        this.historyActionRepo.save(HistoryAction.createForDemande(rame2.getNumSerie(), rame2.getTypeRame(),
                rame2.getConducteurEntrant(), rame2.getTaches().stream().map(Tache::getTache).toList()));

        this.em.flush();
        this.em.clear();
    }

    @Transactional
    public void unload() {
        this.historyActionRepo.deleteAll();
        this.tacheRepo.deleteAll();
        this.rameRepo.deleteAll();
        this.voieRepo.deleteAll();
    }
}
