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
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.convert.JdbcValue;
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

import java.sql.JDBCType;
import java.time.OffsetDateTime;

import static java.util.Arrays.asList;
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
public class JdbcRepositoryCustomVendorSupportedIWithConverterIntegrationTests {

    @Configuration
    @Import(TestConfiguration.class)
    static class Config {

        @Autowired
        JdbcRepositoryFactory factory;

        @Bean
        Class<?> testClass() {
            return JdbcRepositoryCustomVendorSupportedIWithConverterIntegrationTests.class;
        }

        @Bean
        JdbcCustomConversions jdbcCustomConversions() {
            return new JdbcCustomConversions(asList(OffsetDateTimeToStringConverter.INSTANCE, StringToOffsetDateTimeConverter.INSTANCE));
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
    DummyEntityRepositoryConverter dummyEntityConverterRepository;

    @Test // DATAJDBC-443
    public void saveAndLoadAnEntity_converterShouldTakePrecedence() {

        OffsetDateTime now = OffsetDateTime.now();

        dummyEntityConverterRepository.save(createDummyEntity(now));

        assertThat(dummyEntityConverterRepository.findAll())
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


    interface DummyEntityRepositoryConverter extends CrudRepository<DummyEntity, Long> {
    }


    @WritingConverter
    enum OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, JdbcValue> {

        INSTANCE;

        @Override
        public JdbcValue convert(OffsetDateTime source) {

            Object value = source.toString();
            return JdbcValue.of(value, JDBCType.VARCHAR);
        }
    }

    @ReadingConverter
    enum StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

        INSTANCE;

        @Override
        public OffsetDateTime convert(String source) {

            return OffsetDateTime.parse(source);
        }
    }

    @Data
    static class DummyEntity {

        @Id
        private Long id;

        OffsetDateTime offsetDateTime;
    }
}
