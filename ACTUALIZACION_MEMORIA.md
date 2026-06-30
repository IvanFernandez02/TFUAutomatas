# Actualización de la memoria escrita

Implementación actual: **Java 21 + Spring Boot + arquitectura hexagonal + React**.

## Tecnologías

| Componente | Tecnología |
|------------|------------|
| Backend | Java 21, Spring Boot 3, Maven |
| Arquitectura | Hexagonal (domain / application / infrastructure) |
| Léxico | Regex + Ollama (explicación de errores) |
| Sintáctico | Parser recursivo + Ollama (árbol JSON) |
| Semántico | Tabla de símbolos (`schema.json`) |
| Frontend | React + Vite |
| LLM local | Ollama con `llama3.2:3b` |

## Puertos

- Backend API: `8080`
- Frontend: `5173`
- Ollama: `11434` (POST a `/api/generate`)

## Restricción cumplida

`SELECCIONAR salario DESDE usuarios` produce error semántico por columna inexistente.
