DROP TABLE dummy_entity;
DROP TABLE dummy_entity_converter;
CREATE TABLE dummy_entity ( id SERIAL PRIMARY KEY, local_date DATE, local_time TIME, local_date_time TIMESTAMP, offset_date_time TIMESTAMP WITH TIME ZONE);
CREATE TABLE dummy_entity_converter ( id SERIAL PRIMARY KEY, offset_date_time VARCHAR(100));
