# Mini-Compilador SQL (Java + React)

Backend en **Java 21** con **Spring Boot** y **arquitectura hexagonal**. Frontend en **React + Vite**.

## Arquitectura hexagonal

```
domain/          → modelos y ports (in/out)
application/     → casos de uso y servicios (léxico, sintaxis, semántica)
infrastructure/  → adapters REST, Ollama, schema JSON
```

## Requisitos

- Java 21+
- Maven 3.9+
- Node.js 18+
- Ollama (opcional, para árbol LLM y explicaciones léxicas)

```bash
ollama pull llama3.2:3b
ollama serve
```

## Backend

```bash
cd backend
mvn spring-boot:run
```

API en `http://localhost:8080`

Endpoints:
- `POST /api/analyze`
- `GET /api/schema`
- `GET /api/health`

## Frontend

```bash
cd frontend
npm install
npm run dev
```

UI en `http://localhost:5173`

## Nota sobre Ollama

- `http://localhost:11434` → página de estado (GET)
- `http://localhost:11434/api/generate` → requiere **POST**, no GET
- El backend Java llama a Ollama por HTTP POST automáticamente

## Pruebas

```bash
cd backend
mvn test
```
