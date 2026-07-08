-- =====================================================
-- Schema SQL: Tablas del Mini-Compilador SQL en Español
-- Se ejecuta automáticamente al iniciar la aplicación
-- Coincide con la definición en schema.json (Tabla de Símbolos)
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
