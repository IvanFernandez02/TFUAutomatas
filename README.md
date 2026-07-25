# Mini-Compilador SQL - Microservicios

Arquitectura en **3 microservicios** con **Java 21 + Spring Boot**, **arquitectura hexagonal** y **patron Bridge**. Frontend en **React + Vite**.

## Arquitectura

```
┌──────────────┐
│   Frontend   │  React + Vite (puerto 5173)
│   (React)    │
└──────┬───────┘
       │ POST /api/analyze
       ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  SEMANTICO   │────►│   LEXICO     │     │  SINTACTICO  │
│  (Orquest.)  │     │  (Tokens)    │     │  (AST/LLM)   │
│  Puerto 8080 │◄────│  Puerto 8081 │     │  Puerto 8082 │
└──────┬───────┘     └──────────────┘     └──────────────┘
       │
       ▼
┌──────────────┐     ┌──────────────┐
│  PostgreSQL  │     │   Ollama     │
│  (BD comun)  │     │   (LLM)     │
└──────────────┘     └──────────────┘
```

### Patron Bridge

Cada microservicio expone una **abstraccion** (interfaz) con multiples **implementaciones**:

| Abstraccion | Implementaciones |
|-------------|------------------|
| `LexicalAnalyzer` | `RegexLexicalAnalyzer` (regex) |
| `SyntacticAnalyzer` | `FormalSyntacticParser` (parser) + `LlmSyntacticAnalyzer` (Ollama) |
| `LexicalPort` | `HttpLexicalAdapter` (HTTP -> lexico-service) |
| `SyntacticPort` | `HttpSyntacticAdapter` (HTTP -> sintactico-service) |

## Microservicios

| Servicio | Puerto | Funcion |
|----------|--------|---------|
| **lexico-service** | 8081 | Tokenizacion + errores lexicos |
| **sintactico-service** | 8082 | Parser formal + AST con LLM |
| **semantico-service** | 8080 | Orquestador + semantica + PostgreSQL |

### Endpoints

| Servicio | Endpoint | Descripcion |
|----------|----------|-------------|
| Lexico | `POST /api/lexical` | Recibe texto, devuelve tokens |
| Sintactico | `POST /api/syntactic` | Recibe tokens, devuelve AST |
| Semantico | `POST /api/analyze` | Endpoint principal (frontend) |
| Semantico | `GET /api/health` | Estado del sistema |
| Semantico | `GET /api/schema` | Tabla de simbolos |
| Semantico | `GET /api/tables/{name}/data` | Datos de una tabla |

## Ejecucion con Docker (Recomendado)

```bash
docker-compose up --build
```

- Frontend: `http://localhost:5173`
- API: `http://localhost:8080`

## Ejecucion sin Docker (Desarrollo)

```bash
# Terminal 1: Lexico
cd backend/lexico-service
mvn spring-boot:run

# Terminal 2: Sintactico
cd backend/sintactico-service
mvn spring-boot:run

# Terminal 3: Semantico (Orquestador)
cd backend/semantico-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 4: Frontend
cd frontend
npm install
npm run dev
```

## Requisitos

- Java 21+
- Maven 3.9+
- Node.js 18+
- Docker + Docker Compose (para ejecucion contenedorizada)
- Ollama (opcional, para LLM)

```bash
ollama pull llama3
ollama serve
```

## Estructura del Proyecto

```
sql-mini-compiler/
├── docker-compose.yml
├── backend/
│   ├── lexico-service/       # Microservicio Lexico
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/...           # Arquitectura hexagonal + Bridge
│   ├── sintactico-service/   # Microservicio Sintactico
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/...           # Arquitectura hexagonal + Bridge
│   └── semantico-service/    # Microservicio Orquestador
│       ├── Dockerfile
│       ├── pom.xml
│       └── src/...           # Arquitectura hexagonal + Bridge
└── frontend/                 # React + Vite
    ├── Dockerfile
    ├── nginx.conf
    └── src/...
```

## Pruebas

```bash
# Lexico
curl -X POST http://localhost:8081/api/lexical \
  -H "Content-Type: application/json" \
  -d '{"source": "SELECCIONAR nombre DESDE usuarios"}'

# Sintactico
curl -X POST http://localhost:8082/api/syntactic \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECCIONAR nombre DESDE usuarios", "tokens": [{"type":"SELECCIONAR","value":"SELECCIONAR","line":1,"column":0},{"type":"IDENTIFICADOR","value":"NOMBRE","line":1,"column":12},{"type":"DESDE","value":"DESDE","line":1,"column":19},{"type":"IDENTIFICADOR","value":"USUARIOS","line":1,"column":25}]}'

# Semantico (flujo completo)
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECCIONAR nombre DESDE usuarios CUANDO edad > 18"}'
```
