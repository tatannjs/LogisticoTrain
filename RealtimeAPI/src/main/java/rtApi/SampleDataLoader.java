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
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.StreamSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import rtApi.model.ActionType;
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
@Profile("sample-data")
@Component
public class SampleDataLoader implements CommandLineRunner {

    private static final Log LOG = LogFactory.getLog(SampleDataLoader.class);

    private final RameRepository rameRepo;

    private final TacheRepository tacheRepo;

    private final VoieRepository voieRepo;

    private final HistoryActionRepository histoActionRepo;

    private final EntityManager em;

    @Autowired
    public SampleDataLoader(RameRepository rameRepo, TacheRepository tacheRepo,
            VoieRepository voieRepo, HistoryActionRepository histoActionRepo, EntityManager entityManager) {
        this.rameRepo = rameRepo;
        this.tacheRepo = tacheRepo;
        this.voieRepo = voieRepo;
        this.histoActionRepo = histoActionRepo;
        this.em = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        this.clearDbs();
        LOG.info("Create voie");
        List<Voie> voies = this.createVoies();
        List<Rame> rames = this.createRames(voies);
        this.createActions(rames);
    }

    public void clearDbs() {
        LOG.info("Clear db");
        this.histoActionRepo.deleteAll();
        this.tacheRepo.deleteAll();
        this.rameRepo.deleteAll();
        this.voieRepo.deleteAll();
        this.em.flush();
        this.em.clear();
    }

    public List<Voie> createVoies() {
        return StreamSupport.stream(
                this.voieRepo.saveAll(List.of(new Voie(1), new Voie(2, true),
                        new Voie(3), new Voie(4), new Voie(5), new Voie(6))).spliterator(),
                false).toList();
    }

    public List<Rame> createRames(List<Voie> voies) {
        // 1 rame on Voie 1 with task
        Rame r1 = new Rame("a34jedhas", "ZTER", "jpaul");
        r1.getTaches().addAll(List.of(new Tache(r1, 1, "Vérifier circuit hydro"), new Tache(r1, 2, "changer barre coupe-circuit")));
        r1 = this.rameRepo.save(r1);
        r1.setVoie(voies.get(0));
        r1 = this.rameRepo.save(r1);
        // 1 rame one Voie 5 with tasks
        Rame r2 = new Rame("bf3R3jd", "72500", "fpark");
        r2.getTaches().addAll(List.of(new Tache(r2, 1, "Vérifier réservoir toilette A"), new Tache(r2, 2, "mettre à jour manuels")));
        r2 = this.rameRepo.save(r2);
        r2.setVoie(voies.get(4));
        r2 = this.rameRepo.save(r2);
        // 1 rame with no Voie
        Rame r3 = new Rame("c422dksjs", "Corail", "jpaul");
        r3.getTaches().addAll(List.of(new Tache(r3, 1, "Changer siège V1-83")));
        r3 = this.rameRepo.save(r3);
        // 1 rame one Voie 6 with no more task
        Rame r4 = new Rame("ff234ds", "73500", "fpark");
        r4 = this.rameRepo.save(r4);
        r4.setVoie(voies.get(5));
        r4 = this.rameRepo.save(r4);

        return List.of(r1, r2, r3, r4);
    }

    public List<HistoryAction> createActions(List<Rame> rames) {
        final ArrayList<HistoryAction> allSavedActions = new ArrayList<>();
        final HATimestampSupplier tsSu = new HATimestampSupplier();

        // Create consistent actions on old rame
        allSavedActions.addAll(StreamSupport.stream(
                this.histoActionRepo.saveAll(List.of(
                        tsSu.withTime(HistoryAction.createForDemande("azh34ds", "ZTER", "fpark", List.of("Vérifier voyant porte A", "Verifier inter porte 32-D"))),
                        tsSu.withTime(HistoryAction.createForEntree("azh34ds", "ZTER", "htoka", 2)),
                        tsSu.withTime(new HistoryAction("azh34ds", "ZTER", ActionType.realTaches, "fgulik", 2, List.of("Vérifier voyant porte A", "Verifier inter porte 32-D"))),
                        tsSu.withTime(new HistoryAction("azh34ds", "ZTER", ActionType.ajoutTaches, "fgulik", 2, List.of("Vérifier voyant porte B"))),
                        tsSu.withTime(new HistoryAction("azh34ds", "ZTER", ActionType.realTaches, "ampuiz", 2, List.of("Vérifier voyant porte B"))),
                        tsSu.withTime(new HistoryAction("azh34ds", "ZTER", ActionType.sortie, "jpaul", 2, null))
                )).spliterator(),
                false).toList());

        // Create consistent actions on rejected rame2
        Rame rame = rames.get(1);
        allSavedActions.addAll(StreamSupport.stream(
                this.histoActionRepo.saveAll(List.of(
                        tsSu.withTime(HistoryAction.createForDemande(rame.getNumSerie(), rame.getTypeRame(), "fpark", List.of("Vérifier réservoir toilette A", "Vérifier réservoir toilette B", "mettre à jour manuels"))),
                        tsSu.withTime(HistoryAction.createForRejet(rame.getNumSerie(), rame.getTypeRame(), "htoka"))
                )).spliterator(),
                false).toList());

        // Create consistent actions on rame1
        rame = rames.get(0);
        allSavedActions.addAll(StreamSupport.stream(
                this.histoActionRepo.saveAll(List.of(
                        tsSu.withTime(HistoryAction.createForDemande(rame.getNumSerie(), rame.getTypeRame(), "jpaul", List.of("Vérifier circuit hydro", "Verifier equipement cabine"))),
                        tsSu.withTime(HistoryAction.createForEntree(rame.getNumSerie(), rame.getTypeRame(), "htoka", rame.getVoie().getNumVoie())),
                        tsSu.withTime(new HistoryAction(rame.getNumSerie(), rame.getTypeRame(), ActionType.realTaches, "fgulik", rame.getVoie().getNumVoie(), List.of("Verifier equipement cabine"))),
                        tsSu.withTime(new HistoryAction(rame.getNumSerie(), rame.getTypeRame(), ActionType.ajoutTaches, "fgulik", rame.getVoie().getNumVoie(), List.of("changer barre coupe-circuit")))
                )).spliterator(),
                false).toList());

        // Create consistent actions on rame2
        rame = rames.get(1);
        allSavedActions.addAll(StreamSupport.stream(
                this.histoActionRepo.saveAll(List.of(
                        tsSu.withTime(HistoryAction.createForDemande(rame.getNumSerie(), rame.getTypeRame(), "fpark", List.of("Vérifier réservoir toilette A", "Vérifier réservoir toilette B", "mettre à jour manuels"))),
                        tsSu.withTime(HistoryAction.createForEntree(rame.getNumSerie(), rame.getTypeRame(), "htoka", rame.getVoie().getNumVoie())),
                        tsSu.withTime(new HistoryAction(rame.getNumSerie(), rame.getTypeRame(), ActionType.realTaches, "ampuiz", rame.getVoie().getNumVoie(), List.of("Vérifier réservoir toilette B")))
                )).spliterator(),
                false).toList());

        // Create consistent actions on rame3
        rame = rames.get(2);
        allSavedActions.addAll(StreamSupport.stream(
                this.histoActionRepo.saveAll(List.of(
                        tsSu.withTime(HistoryAction.createForDemande(rame.getNumSerie(), rame.getTypeRame(), "jpaul", List.of("Changer siège V1-83")))
                )).spliterator(),
                false).toList());

        // Create consistent actions on rame4
        rame = rames.get(3);
        allSavedActions.addAll(StreamSupport.stream(
                this.histoActionRepo.saveAll(List.of(
                        tsSu.withTime(HistoryAction.createForDemande(rame.getNumSerie(), rame.getTypeRame(), "fpark", List.of("Vérifier circuit hydro", "Verifier equipement cabine"))),
                        tsSu.withTime(HistoryAction.createForEntree(rame.getNumSerie(), rame.getTypeRame(), "htoka", rame.getVoie().getNumVoie())),
                        tsSu.withTime(new HistoryAction(rame.getNumSerie(), rame.getTypeRame(), ActionType.realTaches, "fgulik", rame.getVoie().getNumVoie(), List.of("Vérifier circuit hydro", "Verifier equipement cabine")))
                )).spliterator(),
                false).toList());

        return allSavedActions;
    }

    private static class HATimestampSupplier {

        private LocalDateTime current = LocalDateTime.of(2022, Month.MARCH, 1, 10, 10);
        private final int minTimeSpent = 5;
        private final int maxTimeSpent = 60 * 3;
        private final Random rndGenerator = new Random();

        public HistoryAction withTime(HistoryAction ha) {
            ha.setTimestamp(current);
            this.current = this.current.plusMinutes(this.generateTimeSpentMinutes());
            return ha;
        }

        private long generateTimeSpentMinutes() {
            return rndGenerator.nextInt(minTimeSpent, maxTimeSpent + 1);
        }
    }
}
