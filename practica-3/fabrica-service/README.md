# fabrica-service (Ejercicio 3 - Productor-Consumidor)

Puerto 8083. Correr con `mvn spring-boot:run`.
Pruebas: `mvn test`

Endpoints:
- `POST /api/fabrica/simular?modo=concurrente` (o `secuencial`)
- `GET /api/fabrica/comparar?repeticiones=5`
- `GET /api/fabrica/analisis` (espacios=0 y elementos=5)
- `GET /api/fabrica/stream` (en vivo para el frontend)

Clases principales:
- `Almacen`: la cola con los semáforos `espacios`, `elementos` y el mutex
- `EstrategiaConcurrente` / `EstrategiaSecuencial`: patrón Strategy
- `PublicadorEventos` + observadores: patrón Observer
