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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.test.context.ActiveProfiles;
import rtApi.configuration.MongoConfig;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import(MongoConfig.class)
@ActiveProfiles("mongo-test")
public class HistoryActionTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    public HistoryActionTest() {
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
        this.mongoTemplate.remove(new BasicQuery("{}"), HistoryAction.class);
    }

    /**
     * Test of getTimestamp method, of class HistoryAction.
     */
    @Test
    public void testGetTimestamp() {
        System.out.println("getTimestamp");
        final LocalDateTime now = LocalDateTime.now();
        HistoryAction act = this.mongoTemplate.save(new HistoryAction("numSerie", "typeRame",
                ActionType.entree, "auteur", 1, List.of("tache1", "tache2")));
        assertThat(act.getTimestamp()).as("Timestamp not null and close to now").isNotNull()
                .isCloseTo(now, Assertions.within(1, ChronoUnit.SECONDS));
    }

}
