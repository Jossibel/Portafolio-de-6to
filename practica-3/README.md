# Práctica 3: Sincronización de procesos

Práctica APE Nro. 003 de Sistemas Distribuidos (docente José Guamán). Resolvimos en grupo los cinco ejercicios de la guía: estacionamiento con semáforo contador, cajero con exclusión mutua, productor-consumidor, cruce de vehículos en un puente y centro de impresión. Cada ejercicio es un microservicio en Java 17 con Spring Boot, y un frontend común en HTML, CSS y JavaScript muestra lo que hace cada hilo.

El proyecto completo del grupo está en https://github.com/Doomblas/SyncLab. El informe está en `Informe_APE3_Sincronizacion.pdf`.

## Archivos

```text
practica-3/
├── estacionamiento-service/   # Ejercicio 1, puerto 8080
├── cajero-service/            # Ejercicio 2, puerto 8082
├── fabrica-service/           # Ejercicio 3, puerto 8083
├── puente-service/            # Ejercicio 4, puerto 8084
├── impresion-service/         # Ejercicio 5, puerto 8085
├── frontend/                  # páginas de los cinco ejercicios
│   ├── index.html             # inicio y ejercicio 1
│   ├── ejercicio2.html ... ejercicio5.html
│   ├── css/
│   └── js/config.js           # puerto de cada servicio
└── Informe_APE3_Sincronizacion.pdf
```

## Cómo ejecutarlo

Se necesita Java 17 y Maven. Cada servicio se levanta en su propia terminal, entrando a su carpeta:

```bash
cd fabrica-service
mvn spring-boot:run
```

Lo mismo con `estacionamiento-service`, `cajero-service`, `puente-service` e `impresion-service`. Solo hace falta levantar los servicios de los ejercicios que se quieran probar.

El frontend se abre con la extensión Live Server de Visual Studio Code sobre `frontend/index.html`, que lo sirve en el puerto 5500. Desde la página de inicio se entra a cada ejercicio.

Si el puerto 8080 está ocupado (en Debian puede estar usándolo Tomcat), se libera con `sudo systemctl stop tomcat10`.

## Pruebas

Los ejercicios 3 y 4 tienen pruebas con JUnit que revisan el criterio de verificación de la guía:

```bash
cd fabrica-service
mvn test
```
