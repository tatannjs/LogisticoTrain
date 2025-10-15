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

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
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
public class RameRepositoryTest {

    @Autowired
    private RameRepository rameRepo;

    @Autowired
    private TestEntityManager em;

    public RameRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        this.rameRepo.save(new Rame("serie1", "typeA", "conducteur"));
        this.em.flush();
        this.em.clear();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testCreateRameWithSameIdWillUpdate() {
        this.rameRepo.save(new Rame("serie1", "typeB", "conducteur2"));
        this.em.flush();
        this.em.clear();
    }

    @Test
    public void unableToSetVoieAlreadyOccupied() {
        System.out.println("unableToSetVoieAlreadyOccupied");
        Voie v1 = this.em.persist(new Voie(1));
        Rame rame1 = new Rame("rame1", "typeRame1", "unConducteur");
        rame1 = this.rameRepo.save(rame1);
        rame1.setVoie(v1);
        this.rameRepo.save(rame1);

        final Rame rame2 = this.rameRepo.save(new Rame("rame2", "typeRame2", "unConducteur"));
        this.em.flush();
        this.em.clear();

        System.out.println("Set bad voie to rame2");
        Rame rTest = this.rameRepo.findById(rame2.getNumSerie()).get();
        Assertions.assertThatThrownBy(() -> {
            rTest.setVoie(v1);
            this.rameRepo.save(rTest);
            this.em.flush();
            this.em.clear();
        }).isInstanceOf(ConstraintViolationException.class);

    }

}
