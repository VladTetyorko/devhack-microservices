--liquibase formatted sql

--changeset liquibase:1
--comment: Insert initial user
INSERT INTO users (name, email, password)
VALUES ('vlad', 'vlad@example.com', '$2a$10$SiPdB.Otd1n.1DB.qqBWo.MvAERJKkJYwYBfXtabtV7gd/pYWD4Ti');

--changeset liquibase:2
--comment: Insert Java Language & API tags
INSERT INTO tags (name)
VALUES ('OOP Principles');
INSERT INTO tags (name)
VALUES ('Collections API');
INSERT INTO tags (name)
VALUES ('Generics');
INSERT INTO tags (name)
VALUES ('Exception Handling');
INSERT INTO tags (name)
VALUES ('Multithreading');
INSERT INTO tags (name)
VALUES ('Concurrency');
INSERT INTO tags (name)
VALUES ('Java 8 Features');
INSERT INTO tags (name)
VALUES ('Streams API');
INSERT INTO tags (name)
VALUES ('Lambdas');
INSERT INTO tags (name)
VALUES ('Functional Interfaces');
INSERT INTO tags (name)
VALUES ('Java Memory Model');
INSERT INTO tags (name)
VALUES ('Garbage Collection');
INSERT INTO tags (name)
VALUES ('JVM Internals');
INSERT INTO tags (name)
VALUES ('Class Loaders');
INSERT INTO tags (name)
VALUES ('Immutable Objects');
INSERT INTO tags (name)
VALUES ('Reflection API');
INSERT INTO tags (name)
VALUES ('Annotations');
INSERT INTO tags (name)
VALUES ('Enums');
INSERT INTO tags (name)
VALUES ('String Pool');
INSERT INTO tags (name)
VALUES ('Wrapper Classes');
INSERT INTO tags (name)
VALUES ('Autoboxing');
INSERT INTO tags (name)
VALUES ('Object Class Methods');
INSERT INTO tags (name)
VALUES ('Comparable vs Comparator');

--changeset liquibase:3
--comment: Insert Spring Ecosystem tags
INSERT INTO tags (name)
VALUES ('Spring Core');
INSERT INTO tags (name)
VALUES ('Spring Boot');
INSERT INTO tags (name)
VALUES ('Spring MVC');
INSERT INTO tags (name)
VALUES ('Spring Data JPA');
INSERT INTO tags (name)
VALUES ('Spring Security');
INSERT INTO tags (name)
VALUES ('Spring AOP');
INSERT INTO tags (name)
VALUES ('Spring Profiles');
INSERT INTO tags (name)
VALUES ('Spring Context & Beans');
INSERT INTO tags (name)
VALUES ('Spring Transactions');
INSERT INTO tags (name)
VALUES ('Spring Testing');

--changeset liquibase:4
--comment: Insert Frameworks & Libraries tags
INSERT INTO tags (name)
VALUES ('Hibernate');
INSERT INTO tags (name)
VALUES ('JPA');
INSERT INTO tags (name)
VALUES ('MapStruct');
INSERT INTO tags (name)
VALUES ('Lombok');
INSERT INTO tags (name)
VALUES ('Jackson');
INSERT INTO tags (name)
VALUES ('ModelMapper');
INSERT INTO tags (name)
VALUES ('JUnit 5');
INSERT INTO tags (name)
VALUES ('Mockito');

--changeset liquibase:5
--comment: Insert Web & APIs tags
INSERT INTO tags (name)
VALUES ('REST API Design');
INSERT INTO tags (name)
VALUES ('OpenAPI / Swagger');
INSERT INTO tags (name)
VALUES ('HTTP Protocol');
INSERT INTO tags (name)
VALUES ('Request Mapping');
INSERT INTO tags (name)
VALUES ('ResponseEntity');

--changeset liquibase:6
--comment: Insert Architecture & Patterns tags
INSERT INTO tags (name)
VALUES ('Design Patterns');
INSERT INTO tags (name)
VALUES ('Clean Architecture');
INSERT INTO tags (name)
VALUES ('SOLID Principles');
INSERT INTO tags (name)
VALUES ('Domain Driven Design (DDD)');
