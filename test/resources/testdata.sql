CREATE SCHEMA IF NOT EXISTS my_schema;

CREATE TABLE my_schema.test_table (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255),
  measure_date DATE,
  height NUMERIC
);

INSERT INTO my_schema.test_table (id, name, measure_date, height) VALUES (1, 'John Doe', '2023-01-01', 180.5);
INSERT INTO my_schema.test_table (id, name, measure_date, height) VALUES (2, 'Julia Jung', '2023-02-15', 165.2);
INSERT INTO my_schema.test_table (id, name, measure_date, height) VALUES (3, 'Sam Smith', '2023-03-10', 172.8);
