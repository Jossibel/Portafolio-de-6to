"""Prueba controlada: 3 mensajes con valores conocidos.

  GPS   speed=42       -> cola.gps.telemetria
  TEMP  temperature=7  -> ALERTA -> cola.alertas.temperatura
  FUEL  fuel_level=15  -> ALERTA -> cola.combustible.nivel

Iniciar ANTES el suscriptor y el puente.
"""
import json
import time
from datetime import datetime

import paho.mqtt.client as mqtt

VEHICULO = "VH-001"


def principal():
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    cliente.connect("localhost", 1883, 60)
    cliente.loop_start()
    time.sleep(1)

    ts = datetime.now().isoformat()
    mensajes = [
        (f"flota/{VEHICULO}/gps",
         {"vehicle_id": VEHICULO, "timestamp": ts, "lat": -2.1709, "lng": -79.9224, "speed": 42.0}),
        (f"flota/{VEHICULO}/temperatura",
         {"vehicle_id": VEHICULO, "timestamp": ts, "temperature": 7.0, "unit": "celsius"}),
        (f"flota/{VEHICULO}/combustible",
         {"vehicle_id": VEHICULO, "timestamp": ts, "fuel_level": 15.0, "unit": "percent"}),
    ]
    for topic, datos in mensajes:
        info = cliente.publish(topic, json.dumps(datos), qos=1)
        info.wait_for_publish(timeout=5)
        print(f"Publicado {topic}: {datos}")

    time.sleep(1)
    cliente.loop_stop()
    cliente.disconnect()
    print("Prueba enviada. Revisa la consola del suscriptor y las colas en RabbitMQ.")


if __name__ == "__main__":
    principal()
