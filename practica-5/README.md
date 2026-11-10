# Práctica 5: Mensajería distribuida con MQTT y RabbitMQ

Práctica APE Nro. 005 de Sistemas Distribuidos (docente José Guamán). Hicimos la capa IoT de un sistema de monitoreo de flota. Tres vehículos simulados publican GPS, temperatura y combustible en Mosquitto, un suscriptor guarda todo en SQLite y avisa cuando hay alertas, y un puente pasa a RabbitMQ el GPS y solo las alertas.

El informe completo está en `Informe_APE5_MQTT_RabbitMQ.pdf`.

## Archivos

```text
practica-5/
├── docker-compose.yml          # Mosquitto y RabbitMQ
├── mosquitto/
│   └── config/mosquitto.conf
├── mqtt/
│   ├── simulador_sensores.py   # publica en flota/{vehiculo}/{sensor}
│   ├── suscriptor_sqlite.py    # guarda en telemetria.db y muestra alertas
│   ├── mqtt_rabbitmq_bridge.py # reenvía a las tres colas de RabbitMQ
│   ├── prueba_alertas.py       # manda 3 mensajes con valores conocidos
│   ├── verificar_resultados.py # cuenta registros en SQLite y mensajes en colas
│   └── requirements.txt
└── Informe_APE5_MQTT_RabbitMQ.pdf
```

## Cómo ejecutarlo

Se necesita Docker con Docker Compose y Python 3. En Debian no deja instalar con `pip` directo, así que usamos un entorno virtual:

```bash
python3 -m venv venv
source venv/bin/activate
pip install -r mqtt/requirements.txt
docker compose up -d
```

Si el puerto 1883 sale ocupado es porque hay un Mosquitto instalado en el sistema. Se detiene con `sudo systemctl stop mosquitto`.

Después abrimos una terminal para cada proceso, con el venv activado, y los arrancamos en este orden. Si el simulador va primero, los mensajes que publica antes de que exista la suscripción se pierden.

```bash
python mqtt/suscriptor_sqlite.py
python mqtt/mqtt_rabbitmq_bridge.py
python mqtt/simulador_sensores.py --rondas 2
```

Cada ronda son 9 mensajes (3 vehículos por 3 sensores). Sin `--rondas` el simulador sigue hasta que se presiona Ctrl+C.

Para comprobar las reglas de alerta y ver los resultados:

```bash
python mqtt/prueba_alertas.py
python mqtt/verificar_resultados.py
```

La prueba manda un GPS con velocidad 42, una temperatura de 7 °C y un combustible de 15 %. La alerta de temperatura salta cuando pasa de 4 °C y la de combustible cuando baja de 20 %.

Las colas también se ven en el panel de RabbitMQ, en `http://localhost:15672` con usuario `guest` y clave `guest`, pestaña Queues and Streams. Tienen que aparecer `cola.gps.telemetria`, `cola.alertas.temperatura` y `cola.combustible.nivel`.
