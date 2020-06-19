/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jdbc.repository;

import lombok.Data;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactory;
import org.springframework.data.jdbc.testing.DatabaseProfileValueSource;
import org.springframework.data.jdbc.testing.TestConfiguration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests storing and retrieving extra data types that are supported by Postgres.
 *
 * @author Rick Heuijerjans
 */
@ContextConfiguration
@ActiveProfiles("postgres")
@ProfileValueSourceConfiguration(DatabaseProfileValueSource.class)
@Transactional
public class JdbcRepositoryCustomVendorSupportedIntegrationTests {

    @Configuration
    @Import(TestConfiguration.class)
    static class Config {

        @Autowired
        JdbcRepositoryFactory factory;

        @Bean
        Class<?> testClass() {
            return JdbcRepositoryCustomVendorSupportedIntegrationTests.class;
        }

        @Bean
        DummyEntityRepository dummyEntityRepository() {
            return factory.getRepository(DummyEntityRepository.class);
        }

        @Bean
        DummyEntityRepositoryConverter dummyEntityConverterRepository() {
            return factory.getRepository(DummyEntityRepositoryConverter.class);
        }

    }

    @ClassRule
    public static final SpringClassRule classRule = new SpringClassRule();
    @Rule
    public SpringMethodRule methodRule = new SpringMethodRule();

    @Autowired
    DummyEntityRepository repository;

    @Autowired
    DummyEntityRepositoryConverter dummyEntityConverterRepository;

    @Test // DATAJDBC-443
    public void saveAndLoadAnEntity() {


        Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        DummyEntity entityBeforeSave = createDummyEntity(clock);

        repository.save(entityBeforeSave);

        // todo assert softly
        assertThat(repository.findAll())
                .hasSize(1)
                .first()
                .satisfies(dummyEntity -> {
                    assertThat(dummyEntity.getLocalDate()).isEqualTo(LocalDate.now(clock));
                    assertThat(dummyEntity.getLocalTime()).isEqualTo(LocalTime.now(clock).withNano(111000000));
                    assertThat(dummyEntity.getLocalDateTime()).isEqualTo(LocalDateTime.now(clock).withNano(111000000));
                    assertThat(dummyEntity.getOffsetDateTime()).isEqualTo(OffsetDateTime.now(clock));
                });
    }

    private static DummyEntity createDummyEntity(Clock clock) {
        DummyEntity entity = new DummyEntity();
        entity.setLocalDate(LocalDate.now(clock));
        entity.setLocalTime(LocalTime.now(clock).withNano(111000000));
        entity.setLocalDateTime(LocalDateTime.now(clock).withNano(111000000));
        entity.setOffsetDateTime(OffsetDateTime.now(clock));

        return entity;
    }

    private static DummyEntityConverter createDummyEntityConverter(OffsetDateTime now) {

        DummyEntityConverter entity = new DummyEntityConverter();
        entity.setOffsetDateTime(now);

        return entity;
    }

    interface DummyEntityRepository extends CrudRepository<DummyEntity, Long> {
    }

    interface DummyEntityRepositoryConverter extends CrudRepository<DummyEntityConverter, Long> {
    }

    @Data
    static class DummyEntity {

        @Id
        private Long id;

        LocalDate localDate;

        LocalTime localTime;

        OffsetDateTime offsetDateTime;

        LocalDateTime localDateTime;
    }

    @Data
    static class DummyEntityConverter {

        @Id
        private Long id;

        OffsetDateTime offsetDateTime;
    }
}
