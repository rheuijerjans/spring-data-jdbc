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
import org.junit.ClassRule;
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
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick Heuijerjans
 */
@ContextConfiguration
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

    }

    @ClassRule
    public static final SpringClassRule classRule = new SpringClassRule();
    @Rule
    public SpringMethodRule methodRule = new SpringMethodRule();

    @Autowired
    DummyEntityRepository repository;

    @Test // DATAJDBC-443
    @IfProfileValue(name = "current.database.is.not.mssql", value = "true")
    public void saveAndLoadAnEntity() {

        final OffsetDateTime now = OffsetDateTime.now();

        DummyEntity entity = repository.save(createDummyEntity(now));

        repository.findById(entity.getId());

        assertThat(repository.findAll())
                .hasSize(1)
                .first()
                .satisfies(dummyEntity ->
                        assertThat(dummyEntity.getOffsetDateTime()).isEqualTo(now));
    }

    private static DummyEntity createDummyEntity(OffsetDateTime now) {

        DummyEntity entity = new DummyEntity();
        entity.setOffsetDateTime(now);

        return entity;
    }

    interface DummyEntityRepository extends CrudRepository<DummyEntity, Long> {
    }

    @Data
    static class DummyEntity {

        @Id
        private Long id;

        OffsetDateTime offsetDateTime;
    }
}
