# puente-service (Ejercicio 4 - Puente en una dirección)

Puerto 8084. Correr con `mvn spring-boot:run`.
Pruebas: `mvn test`

Endpoints:
- `POST /api/puente/simular?politica=base` (o `justa`)
- `GET /api/puente/inanicion`
- `GET /api/puente/comparar?repeticiones=5`
- `GET /api/puente/stream` (en vivo para el frontend)

Clases principales:
- `Puente`: monitor con `ReentrantLock` + `Condition`
- `PoliticaBase` (la de la guía) y `PoliticaJusta` (el reto): patrón Strategy

vehiculo_sur(id) completado:
```
PROCEDIMIENTO vehiculo_sur(id)
    lock(mutex)
    MIENTRAS direccion == NORTE HACER
        esperar(condicion)        // suelta el mutex mientras espera
    FIN MIENTRAS
    direccion = SUR
    vehiculos_sur = vehiculos_sur + 1
    unlock(mutex)

    escribir("Vehículo ", id, " cruza hacia el SUR")
    cruzar_puente(id)

    lock(mutex)
    vehiculos_sur = vehiculos_sur - 1
    SI vehiculos_sur == 0 ENTONCES
        direccion = NINGUNA
    FIN SI
    despertar_a_todos(condicion)
    unlock(mutex)
FIN PROCEDIMIENTO
```
