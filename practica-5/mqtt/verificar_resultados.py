"""Verifica los resultados: registros en SQLite, alertas guardadas y mensajes en RabbitMQ."""
import os
import sqlite3

DB = os.path.join(os.path.dirname(os.path.abspath(__file__)), "telemetria.db")
COLAS = ["cola.gps.telemetria", "cola.alertas.temperatura", "cola.combustible.nivel"]


def verificar_sqlite():
    print("=" * 55)
    print(" SQLite - telemetria.db")
    print("=" * 55)
    if not os.path.exists(DB):
        print("No existe telemetria.db. Ejecuta primero suscriptor_sqlite.py")
        return
    conn = sqlite3.connect(DB)
    cur = conn.cursor()
    for tabla in ("gps_data", "temp_data", "fuel_data"):
        total = cur.execute(f"SELECT COUNT(*) FROM {tabla}").fetchone()[0]
        print(f"  {tabla:<10} {total:>5} registros")

    print("\n Alertas de temperatura (> 4 °C) - ultimas 5")
    for fila in cur.execute(
            "SELECT vehicle_id, temperature, timestamp FROM temp_data "
            "WHERE temperature > 4 ORDER BY id DESC LIMIT 5"):
        print(f"  {fila[0]}  {fila[1]:>5} °C  {fila[2]}")

    print("\n Alertas de combustible (< 20 %) - ultimas 5")
    for fila in cur.execute(
            "SELECT vehicle_id, fuel_level, timestamp FROM fuel_data "
            "WHERE fuel_level < 20 ORDER BY id DESC LIMIT 5"):
        print(f"  {fila[0]}  {fila[1]:>5} %   {fila[2]}")
    conn.close()


def verificar_rabbitmq():
    print("\n" + "=" * 55)
    print(" RabbitMQ - mensajes en cola")
    print("=" * 55)
    try:
        import pika
        conexion = pika.BlockingConnection(pika.ConnectionParameters("localhost"))
        canal = conexion.channel()
        for cola in COLAS:
            q = canal.queue_declare(queue=cola, durable=True, passive=True)
            print(f"  {cola:<26} {q.method.message_count:>5} mensajes")
        conexion.close()
    except Exception as e:
        print(f"  No se pudo consultar RabbitMQ: {e}")


if __name__ == "__main__":
    verificar_sqlite()
    verificar_rabbitmq()
