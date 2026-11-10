"""Actividad 2 - Simulador de sensores IoT de la flota.

Publica GPS, temperatura y combustible de 3 vehiculos en Mosquitto.
Uso:
    python simulador_sensores.py            # infinito (Ctrl+C para detener)
    python simulador_sensores.py --rondas 2 # 2 rondas = 18 mensajes
"""
import argparse
import json
import random
import threading
import time
from datetime import datetime

import paho.mqtt.client as mqtt

BROKER = "localhost"
PUERTO = 1883
VEHICULOS = ["VH-001", "VH-002", "VH-003"]
SENSORES = ("gps", "temperatura", "combustible")
PAUSA_SEGUNDOS = 5

conectado = threading.Event()


def on_connect(client, userdata, flags, reason_code, properties):
    print(f"[{datetime.now().isoformat()}] Conectado al broker MQTT (rc={reason_code})")
    conectado.set()


def simular_gps(id_vehiculo):
    base_lat = -2.1709
    base_lng = -79.9224
    return {
        "lat": round(base_lat + random.uniform(-0.01, 0.01), 6),
        "lng": round(base_lng + random.uniform(-0.01, 0.01), 6),
        "speed": round(random.uniform(20, 80), 1),
    }


def simular_temperatura(id_vehiculo):
    return {"temperature": round(random.uniform(-5, 8), 1), "unit": "celsius"}


def simular_combustible(id_vehiculo):
    return {"fuel_level": round(random.uniform(10, 100), 1), "unit": "percent"}


GENERADORES = {
    "gps": simular_gps,
    "temperatura": simular_temperatura,
    "combustible": simular_combustible,
}


def publicar_ronda(cliente, numero_ronda):
    for vehiculo in VEHICULOS:
        timestamp = datetime.now().isoformat()
        for sensor in SENSORES:
            topic = f"flota/{vehiculo}/{sensor}"
            payload = {"vehicle_id": vehiculo, "timestamp": timestamp,
                       **GENERADORES[sensor](vehiculo)}
            info = cliente.publish(topic, json.dumps(payload), qos=1)
            info.wait_for_publish(timeout=5)
            print(f"  -> {topic} {payload}")
        print(f"[{timestamp}] Ronda {numero_ronda}: datos publicados para {vehiculo}")
        time.sleep(PAUSA_SEGUNDOS)


def principal():
    parser = argparse.ArgumentParser(description="Simulador de sensores de la flota")
    parser.add_argument("--rondas", type=int, default=0,
                        help="Numero de rondas (0 = infinito). Cada ronda = 9 mensajes")
    args = parser.parse_args()

    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    cliente.on_connect = on_connect
    cliente.connect(BROKER, PUERTO, 60)
    cliente.loop_start()

    if not conectado.wait(timeout=10):
        print("No se pudo conectar al broker. Revisa que Mosquitto este activo.")
        cliente.loop_stop()
        return

    print("Iniciando simulador de sensores IoT...")
    ronda = 0
    try:
        while args.rondas == 0 or ronda < args.rondas:
            ronda += 1
            publicar_ronda(cliente, ronda)
        print(f"Finalizado: {ronda} ronda(s), {ronda * 9} mensajes publicados.")
    except KeyboardInterrupt:
        print("\nDeteniendo simulador...")
    finally:
        cliente.loop_stop()
        cliente.disconnect()


if __name__ == "__main__":
    principal()
