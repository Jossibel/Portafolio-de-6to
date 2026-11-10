"""Actividad 4 - Puente MQTT -> RabbitMQ.

Se suscribe a flota/# y reenvia a RabbitMQ:
  - Todo GPS                      -> cola.gps.telemetria
  - Temperatura > 4 °C (alerta)   -> cola.alertas.temperatura
  - Combustible < 20 % (alerta)   -> cola.combustible.nivel
Usa el exchange predeterminado (""), donde routing_key = nombre de la cola.
"""
import json
from datetime import datetime

import paho.mqtt.client as mqtt
import pika

MQTT_BROKER = "localhost"
MQTT_PUERTO = 1883
RABBIT_HOST = "localhost"

COLA_GPS = "cola.gps.telemetria"
COLA_TEMP = "cola.alertas.temperatura"
COLA_FUEL = "cola.combustible.nivel"
COLAS = [COLA_GPS, COLA_TEMP, COLA_FUEL]


def configurar_rabbitmq():
    parametros = pika.ConnectionParameters(host=RABBIT_HOST, heartbeat=600)
    conexion = pika.BlockingConnection(parametros)
    canal = conexion.channel()
    for cola in COLAS:
        canal.queue_declare(queue=cola, durable=True)
        print(f"  Cola declarada (durable): {cola}")
    return conexion, canal


def publicar_en(canal, cola, datos):
    canal.basic_publish(
        exchange="",                 # exchange predeterminado
        routing_key=cola,            # = nombre de la cola destino
        body=json.dumps(datos),
        properties=pika.BasicProperties(
            delivery_mode=2,         # mensaje persistente
            content_type="application/json",
        ),
    )
    print(f"  -> RabbitMQ [{cola}] {datos.get('vehicle_id')}")


def on_connect(client, userdata, flags, reason_code, properties):
    print(f"[{datetime.now().isoformat()}] Puente conectado a MQTT (rc={reason_code})")
    client.subscribe("flota/#", qos=1)
    print("  Suscrito a flota/#")


def on_message(client, userdata, mensaje):
    canal = userdata["rabbitmq_channel"]
    topic = mensaje.topic
    try:
        datos = json.loads(mensaje.payload.decode())
    except (json.JSONDecodeError, UnicodeDecodeError):
        print(f"Mensaje no valido en {topic}")
        return

    try:
        if topic.endswith("/gps"):
            publicar_en(canal, COLA_GPS, datos)

        elif topic.endswith("/temperatura") and datos["temperature"] > 4:
            alerta = {**datos, "type": "TEMP_ALERT", "message": "Temperatura excedida"}
            publicar_en(canal, COLA_TEMP, alerta)

        elif topic.endswith("/combustible") and datos["fuel_level"] < 20:
            alerta = {**datos, "type": "FUEL_LOW", "message": "Combustible bajo"}
            publicar_en(canal, COLA_FUEL, alerta)
    except KeyError as e:
        print(f"Falta el campo {e} en el mensaje de {topic}")


def principal():
    print("Conectando a RabbitMQ...")
    conexion, canal = configurar_rabbitmq()

    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2,
                          userdata={"rabbitmq_channel": canal})
    cliente.on_connect = on_connect
    cliente.on_message = on_message
    cliente.connect(MQTT_BROKER, MQTT_PUERTO, 60)

    print("Puente MQTT -> RabbitMQ activo. Ctrl+C para detener.")
    try:
        cliente.loop_forever()
    except KeyboardInterrupt:
        print("\nDeteniendo puente...")
    finally:
        cliente.disconnect()
        if conexion.is_open:
            conexion.close()


if __name__ == "__main__":
    principal()
