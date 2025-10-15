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

import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author Rémi Venant
 */
@DataJpaTest
@ActiveProfiles({"db-hsqldb"})
public class TacheTest {

    @Autowired
    private TestEntityManager em;

    public TacheTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        this.em.clear();
        this.em.flush();
    }

    /**
     * Test of getId method, of class Tache.
     */
    @Test
    public void testCreateRameAndTacheAtOnce() {
        System.out.println("testCreateRameAndTacheAtOnce");
        Rame rame = new Rame("rame1", "typeRame1", "unConducteur");
        rame.getTaches().addAll(List.of(
                new Tache(rame, 1, "tache1"),
                new Tache(rame, 2, "tache2")));
        rame = this.em.persistAndFlush(rame);
        this.em.clear();

        Rame gotRame = this.em.find(Rame.class, rame.getNumSerie());
        assertThat(gotRame).as("Rame in db is found").isNotNull();
        Set<Tache> taches = gotRame.getTaches();
        assertThat(taches).as("Taches in db are founder").hasSize(2);
    }

    @Test
    public void testUpdateRame() {
        System.out.println("testCreateRameAndTacheAtOnce");
        Rame rame = new Rame("rame1", "typeRame1", "unConducteur");
        rame.getTaches().addAll(List.of(
                new Tache(rame, 1, "tache1"),
                new Tache(rame, 2, "tache2")));
        rame = this.em.persistAndFlush(rame);
        this.em.clear();

        Rame gotRame = this.em.find(Rame.class, rame.getNumSerie());
        gotRame.setTypeRame("toto");
        gotRame = this.em.persistAndFlush(gotRame);
        assertThat(gotRame).as("Rame in db is found").isNotNull();
    }

    @Test
    public void testRemoveAndAddTache() {
        System.out.println("testCreateRameAndTacheAtOnce");
        Rame rame = new Rame("rame1", "typeRame1", "unConducteur");
        rame.getTaches().addAll(List.of(
                new Tache(rame, 1, "tache1"),
                new Tache(rame, 2, "tache2")));
        rame = this.em.persistAndFlush(rame);
        this.em.clear();

        Rame gotRame = this.em.find(Rame.class, rame.getNumSerie());
        Tache tacheToRemove = gotRame.getTaches().iterator().next();
        int numTacheToRemove = tacheToRemove.getId().getNumTache();
        System.out.println("Tache to remove num tache: " + numTacheToRemove);
        int numTacheLeft = numTacheToRemove == 1 ? 2 : 1;
        boolean resRemove = gotRame.getTaches().remove(tacheToRemove);
        assertThat(resRemove).as("One tache removed").isTrue();
        gotRame.getTaches().add(new Tache(gotRame, 3, "tache3"));
        assertThat(gotRame.getTaches()).as("2 taches remainings before saving")
                .hasSize(2)
                .map(t -> t.getId().getNumTache()).containsExactlyInAnyOrder(3, numTacheLeft);
        gotRame = this.em.persistAndFlush(gotRame);
        this.em.clear();

        gotRame = this.em.find(Rame.class, rame.getNumSerie());
        assertThat(gotRame).as("Rame in db is found").isNotNull();
        assertThat(gotRame.getTaches()).as("2 taches remainings")
                .hasSize(2).map(t -> t.getId()
                .getNumTache()).containsExactlyInAnyOrder(3, numTacheLeft);
    }

    @Test
    public void unableToSetVoieAlreadyOccupied() {
        System.out.println("unableToSetVoieAlreadyOccupied");
        Voie v1 = this.em.persist(new Voie(1));
        Rame rame1 = new Rame("rame1", "typeRame1", "unConducteur");
        rame1.setVoie(v1);
        rame1 = this.em.persist(rame1);

        final Rame rame2 = this.em.persistAndFlush(new Rame("rame2", "typeRame2", "unConducteur"));
        this.em.clear();

        System.out.println("Set bad voie to rame2");
        Rame rTest = this.em.find(Rame.class, rame2.getNumSerie());
        Assertions.assertThatThrownBy(() -> {
            rTest.setVoie(v1);
            this.em.persistAndFlush(rTest);
        }).isInstanceOf(PersistenceException.class);

    }

}
