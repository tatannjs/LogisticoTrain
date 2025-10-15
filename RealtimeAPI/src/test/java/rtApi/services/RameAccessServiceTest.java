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

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import rtApi.TestDatasetLoader;
import rtApi.model.HistoryActionRepository;
import rtApi.model.Rame;
import rtApi.model.RameRepository;
import rtApi.model.TacheRepository;
import rtApi.services.messages.RameEntranceAnswer;
import rtApi.services.messages.RameEntranceRequest;

/**
 *
 * @author Rémi Venant
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@Import({JPAConfig.class, MongoConfig.class})
@Import({TestDatasetLoader.class})
@ActiveProfiles({"no-broker", "db-hsqldb", "mongo-test"})
public class RameAccessServiceTest {

    @Autowired
    private RameAccessService testedSvc;

    @Autowired
    private TacheRepository tacheRepo;

    @Autowired
    private RameRepository rameRepo;

    @Autowired
    private HistoryActionRepository historyActionRepo;

    @Autowired
    private EntityManager em;

    @Autowired
    private TestDatasetLoader datasetLoader;

    public RameAccessServiceTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        this.datasetLoader.load();
    }

    @AfterEach
    public void tearDown() {
        this.datasetLoader.unload();
    }

    @Test
    public void baseTest() {
        assertThat(this.rameRepo.count()).as("Rame repo ready").isEqualTo(2);
        assertThat(this.tacheRepo.count()).as("Tache repo ready").isEqualTo(4);
        assertThat(this.historyActionRepo.count()).as("History action repo ready").isEqualTo(3);
    }

    /**
     * Test of askRameEntrance method, of class RameAccessServiceImpl.
     */
    @Test
    public void testAskRameEntranceArgValidation() {
        System.out.println("testAskRameEntranceArgValidation");
        final RameEntranceRequest rer = buildRameEntranceRequest("", "aRame", "aConducteur", List.of("a tache"));
        Assertions.assertThatThrownBy(() -> {
            Rame rame = this.testedSvc.askRameEntrance(rer);
        }).as("Cannot add rame entrance with invalid numSerie").isInstanceOf(ConstraintViolationException.class);

        final RameEntranceRequest rer2 = buildRameEntranceRequest("aNumSerie", "", "aConducteur", List.of("a tache"));
        Assertions.assertThatThrownBy(() -> {
            Rame rame = this.testedSvc.askRameEntrance(rer2);
        }).as("Cannot add rame entrance with invalid typeRame").isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void testAskRameEntranceDuplicateKeyEx() {
        System.out.println("testAskRameEntranceDuplicateKeyEx");
        final RameEntranceRequest rer = buildRameEntranceRequest("serRame1", "aRame", "aConducteur", List.of("a tache"));
        Assertions.assertThatThrownBy(() -> {
            Rame rame = this.testedSvc.askRameEntrance(rer);
        }).as("Cannot add rame entrance with existing numSerie").isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void testAskRameEntraceDoNothinOnEx() {
        System.out.println("testAskRameEntraceDoNothinOnEx");
        final long initNbRame = this.rameRepo.count();
        final long initNbTache = this.tacheRepo.count();
        final long initNbHistoAct = this.historyActionRepo.count();
        final RameEntranceRequest rer = buildRameEntranceRequest("serRame1", "aRame", "aConducteur", List.of("a tache"));
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.askRameEntrance(rer);
        }).as("Cannot add rame entrance with existing numSerie").isInstanceOf(Exception.class);
        assertThat(this.rameRepo.count()).as("Still same num. of rames").isEqualTo(initNbRame);
        assertThat(this.tacheRepo.count()).as("Still same num. of taches").isEqualTo(initNbTache);
        assertThat(this.historyActionRepo.count()).as("Still same num. of histoActions").isEqualTo(initNbHistoAct);
    }

    @Test
    public void testAskRameEntraceOk() {
        System.out.println("testAskRameEntraceOk");
        final long initNbRame = this.rameRepo.count();
        final long initNbTache = this.tacheRepo.count();
        final long initNbHistoAct = this.historyActionRepo.count();
        final RameEntranceRequest rer = buildRameEntranceRequest("aNewRame", "aRame", "aConducteur", List.of("a tache", "a tache2"));
        Rame rame = this.testedSvc.askRameEntrance(rer);
        assertThat(rame).as("A rame is returned").isNotNull();
        assertThat(this.rameRepo.count()).as("added 1 rame").isEqualTo(initNbRame + 1);
        assertThat(this.tacheRepo.count()).as("added 2 taches").isEqualTo(initNbTache + 2);
        assertThat(this.historyActionRepo.count()).as("added 1 histoAction").isEqualTo(initNbHistoAct + 1);
    }

    @Test
    public void testAnswerRameEntranceRequestArgValidation() {
        System.out.println("testAnswerRameEntranceRequestArgValidation");
        final RameEntranceAnswer ans = buildRameEntranceAnswer("", true, "anOperateur", 3);
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.answerRameEntranceRequest(ans);
        }).as("Cannot add rame entrance answer with invalid numSerie").isInstanceOf(ConstraintViolationException.class);

        final RameEntranceAnswer ans2 = buildRameEntranceAnswer("serRame2", null, "anOperateur", 3);
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.answerRameEntranceRequest(ans2);
        }).as("Cannot add rame entrance answer with invalid numSerie").isInstanceOf(ConstraintViolationException.class);

        final RameEntranceAnswer ans3 = buildRameEntranceAnswer("serRame2", true, "anOperateur", null);
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.answerRameEntranceRequest(ans3);
        }).as("Cannot add rame entrance answer with invalid numVoie when accept").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testAnswerRameEntranceRequestNoSuchElem() {
        System.out.println("testAnswerRameEntranceRequestNoSuchElem");
        final long initNbRame = this.rameRepo.count();
        final long initNbTache = this.tacheRepo.count();
        final long initNbHistoAct = this.historyActionRepo.count();

        final RameEntranceAnswer ans = buildRameEntranceAnswer("unknownRame", true, "anOperateur", 3);
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.answerRameEntranceRequest(ans);
        }).as("Cannot add rame entrance answer with unknown numSerie").isInstanceOf(NoSuchElementException.class);

        final RameEntranceAnswer ans2 = buildRameEntranceAnswer("serRame2", true, "anOperateur", 4);
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.answerRameEntranceRequest(ans2);
        }).as("Cannot add rame entrance answer with unknown voie").isInstanceOf(NoSuchElementException.class);

        final RameEntranceAnswer ans3 = buildRameEntranceAnswer("serRame2", true, "anOperateur", 2);
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.answerRameEntranceRequest(ans3);
        }).as("Cannot add rame entrance answer with forbidden voie").isInstanceOf(NoSuchElementException.class);

        assertThat(this.rameRepo.count()).as("Still same num. of rames").isEqualTo(initNbRame);
        assertThat(this.tacheRepo.count()).as("Still same num. of taches").isEqualTo(initNbTache);
        assertThat(this.historyActionRepo.count()).as("Still same num. of histoActions").isEqualTo(initNbHistoAct);
    }

    @Test
    public void testAnswerRameEntranceRequestDupKey() {
        System.out.println("testAnswerRameEntranceRequestDupKey");
        final long initNbRame = this.rameRepo.count();
        final long initNbTache = this.tacheRepo.count();
        final long initNbHistoAct = this.historyActionRepo.count();

        final RameEntranceAnswer ans = buildRameEntranceAnswer("serRame1", true, "anOperateur", 3);
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.answerRameEntranceRequest(ans);
        }).as("Cannot add rame entrance answer with already set rame").isInstanceOf(DuplicateKeyException.class);

        final RameEntranceAnswer ans2 = buildRameEntranceAnswer("serRame2", true, "anOperateur", 1);
        Assertions.assertThatThrownBy(() -> {
            this.testedSvc.answerRameEntranceRequest(ans2);
        }).as("Cannot add rame entrance answer with with already used voie").isInstanceOf(DuplicateKeyException.class);

        assertThat(this.rameRepo.count()).as("Still same num. of rames").isEqualTo(initNbRame);
        assertThat(this.tacheRepo.count()).as("Still same num. of taches").isEqualTo(initNbTache);
        assertThat(this.historyActionRepo.count()).as("Still same num. of histoActions").isEqualTo(initNbHistoAct);
    }

    @Test
    public void testAnswerRameEntranceRequestOk() {
        System.out.println("testAnswerRameEntranceRequestOk");
        final long initNbRame = this.rameRepo.count();
        final long initNbTache = this.tacheRepo.count();
        final long initNbHistoAct = this.historyActionRepo.count();

        final RameEntranceAnswer ans = buildRameEntranceAnswer("serRame2", true, "anOperateur", 3);
        Rame rame = this.testedSvc.answerRameEntranceRequest(ans);

        assertThat(rame).as("A rame is returned").isNotNull();
        assertThat(rame.getVoie()).as("A rame has a proper voie").isNotNull().extracting("numVoie").isEqualTo(3);

        assertThat(this.rameRepo.count()).as("added 0 rame").isEqualTo(initNbRame);
        assertThat(this.tacheRepo.count()).as("added 0 taches").isEqualTo(initNbTache);
        assertThat(this.historyActionRepo.count()).as("added 1 histoAction").isEqualTo(initNbHistoAct + 1);
    }

    static RameEntranceRequest buildRameEntranceRequest(String numSerie, String typeRame, String auteur, List<String> taches) {
        final RameEntranceRequest rq = new RameEntranceRequest();
        rq.setNumSerie(numSerie);
        rq.setTypeRame(typeRame);
        rq.setAuteur(auteur);
        rq.setTaches(taches);
        return rq;
    }

    static RameEntranceAnswer buildRameEntranceAnswer(String numSerie, Boolean accept, String auteur, Integer voie) {
        final RameEntranceAnswer ans = new RameEntranceAnswer();
        ans.setNumSerie(numSerie);
        ans.setAccept(accept);
        ans.setAuteur(auteur);
        ans.setVoie(voie);
        return ans;
    }
}
