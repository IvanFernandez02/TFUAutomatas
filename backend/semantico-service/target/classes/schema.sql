-- =====================================================
-- Schema SQL: Tablas del Mini-Compilador SQL en Espanol
-- Se ejecuta automaticamente al iniciar la aplicacion
-- =====================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id      SERIAL PRIMARY KEY,
    nombre  VARCHAR(255) NOT NULL,
    edad    INTEGER,
    email   VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS productos (
    id      SERIAL PRIMARY KEY,
    nombre  VARCHAR(255) NOT NULL,
    precio  DECIMAL(10, 2)
);
