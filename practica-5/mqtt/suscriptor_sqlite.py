"""Actividad 3 - Suscriptor MQTT que guarda la telemetria en SQLite y muestra alertas.

Arrancarlo ANTES del simulador:  python suscriptor_sqlite.py
"""
import json
import os
import sqlite3
from datetime import datetime

import paho.mqtt.client as mqtt

BROKER = "localhost"
PUERTO = 1883
DB = os.path.join(os.path.dirname(os.path.abspath(__file__)), "telemetria.db")

UMBRAL_TEMPERATURA = 4    # alerta si temperature > 4 °C
UMBRAL_COMBUSTIBLE = 20   # alerta si fuel_level < 20 %

TOPICS = ["flota/+/gps", "flota/+/temperatura", "flota/+/combustible"]


def inicializar_base_datos():
    conn = sqlite3.connect(DB)
    cur = conn.cursor()
    cur.execute("""CREATE TABLE IF NOT EXISTS gps_data (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        vehicle_id TEXT, lat REAL, lng REAL, speed REAL, timestamp TEXT)""")
    cur.execute("""CREATE TABLE IF NOT EXISTS temp_data (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        vehicle_id TEXT, temperature REAL, unit TEXT, timestamp TEXT)""")
    cur.execute("""CREATE TABLE IF NOT EXISTS fuel_data (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        vehicle_id TEXT, fuel_level REAL, unit TEXT, timestamp TEXT)""")
    conn.commit()
    print(f"Base de datos lista: {DB}")
    return conn


def on_connect(client, userdata, flags, reason_code, properties):
    print(f"[{datetime.now().isoformat()}] Conectado al broker MQTT (rc={reason_code})")
    for topic in TOPICS:
        client.subscribe(topic, qos=1)
        print(f"  Suscrito a {topic}")


def on_message(client, userdata, mensaje):
    conn = userdata["db"]
    topic = mensaje.topic                                   # 1. leer topic
    try:
        datos = json.loads(mensaje.payload.decode())        # 2. decodificar JSON
    except (json.JSONDecodeError, UnicodeDecodeError):
        print(f"Mensaje no valido en {topic}")
        return

    sensor = topic.split("/")[-1]                           # 3. tipo de sensor
    vehiculo = datos.get("vehicle_id")
    cur = conn.cursor()

    try:
        if sensor == "gps":                                 # 4. insertar en SQLite
            cur.execute(
                "INSERT INTO gps_data (vehicle_id, lat, lng, speed, timestamp) VALUES (?, ?, ?, ?, ?)",
                (vehiculo, datos["lat"], datos["lng"], datos["speed"], datos["timestamp"]))
            print(f"[GPS] {vehiculo} lat={datos['lat']} lng={datos['lng']} speed={datos['speed']}")

        elif sensor == "temperatura":
            cur.execute(
                "INSERT INTO temp_data (vehicle_id, temperature, unit, timestamp) VALUES (?, ?, ?, ?)",
                (vehiculo, datos["temperature"], datos.get("unit", "celsius"), datos["timestamp"]))
            print(f"[TEMP] {vehiculo} {datos['temperature']} °C")
            if datos["temperature"] > UMBRAL_TEMPERATURA:   # 5. evaluar alerta
                print(f"  ALERTA: {vehiculo} temperatura alta ({datos['temperature']} °C)")

        elif sensor == "combustible":
            cur.execute(
                "INSERT INTO fuel_data (vehicle_id, fuel_level, unit, timestamp) VALUES (?, ?, ?, ?)",
                (vehiculo, datos["fuel_level"], datos.get("unit", "percent"), datos["timestamp"]))
            print(f"[FUEL] {vehiculo} {datos['fuel_level']} %")
            if datos["fuel_level"] < UMBRAL_COMBUSTIBLE:
                print(f"  ALERTA: {vehiculo} combustible bajo ({datos['fuel_level']} %)")
        conn.commit()
    except KeyError as e:
        print(f"Falta el campo {e} en el mensaje de {topic}")


def principal():
    conn = inicializar_base_datos()
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, userdata={"db": conn})
    cliente.on_connect = on_connect
    cliente.on_message = on_message
    cliente.connect(BROKER, PUERTO, 60)
    print("Suscriptor activo. Ctrl+C para detener.")
    try:
        cliente.loop_forever()
    except KeyboardInterrupt:
        print("\nDeteniendo suscriptor...")
    finally:
        cliente.disconnect()
        conn.close()


if __name__ == "__main__":
    principal()
