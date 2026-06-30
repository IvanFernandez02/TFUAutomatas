# Casos de prueba

## Backend Java

```bash
cd backend
mvn test
```

## Consultas válidas

1. `SELECCIONAR nombre, edad DESDE usuarios CUANDO edad > 18`
2. `INSERTAR EN usuarios (nombre, edad) VALORES ('Ana', 25)`
3. `MODIFICAR usuarios ESTABLECER edad = 30 CUANDO nombre = 'Ana'`
4. `ELIMINAR DESDE usuarios CUANDO edad < 18`

## Error semántico obligatorio

- `SELECCIONAR salario DESDE usuarios`

## Errores léxicos

- `SELECCIONAR @nombre DESDE usuarios`

Ollama puede explicar el error léxico en `lexical_explanation`.

## Arranque

```bash
# Terminal 1
cd backend && mvn spring-boot:run

# Terminal 2
cd frontend && npm run dev
```
