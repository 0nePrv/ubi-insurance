-- Database-per-service логически, одна инстанция физически.
-- Сервис видит только свою БД: соблазн сделать JOIN в чужую таблицу отсекается правами.
CREATE USER policy     WITH PASSWORD 'policy';     CREATE DATABASE policy     OWNER policy;
CREATE USER rating     WITH PASSWORD 'rating';     CREATE DATABASE rating     OWNER rating;
CREATE USER telematics WITH PASSWORD 'telematics'; CREATE DATABASE telematics OWNER telematics;
CREATE USER claims     WITH PASSWORD 'claims';     CREATE DATABASE claims     OWNER claims;
CREATE USER billing    WITH PASSWORD 'billing';    CREATE DATABASE billing    OWNER billing;
CREATE USER keycloak   WITH PASSWORD 'keycloak';   CREATE DATABASE keycloak   OWNER keycloak;

-- pgvector для RAG в claims (этап 4). CREATE EXTENSION требует прав суперпользователя.
\connect claims
CREATE EXTENSION IF NOT EXISTS vector;
