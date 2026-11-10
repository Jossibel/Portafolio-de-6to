"""
Worker (antes: server.py). Se ejecuta en OTRA computadora.
Recibe pedidos por TCP, simula la entrega y responde al gateway.

Uso:  python3 worker.py 7001
"""
import json
import random
import socket
import sys
import threading
import time

carga = 0
lock = threading.Lock()


def atender(conn, addr, puerto):
    global carga
    try:
        partes = []
        while True:
            bloque = conn.recv(4096)
            if not bloque:
                break
            partes.append(bloque)
        pedido = json.loads(b"".join(partes).decode())

        with lock:
            carga += 1
            carga_actual = carga
        print(f"[WORKER {puerto}] Pedido {pedido['id']} recibido de {addr[0]} "
              f"| carga = {carga_actual}")

        # Simular la entrega: más distancia = más tiempo
        km = pedido.get("distancia_km", 3)
        demora = 0.5 + km * 0.3 + random.uniform(0, 0.5)
        time.sleep(demora)

        with lock:
            carga -= 1

        respuesta = {"worker": puerto, "pedido": pedido["id"],
                     "estado": "ENTREGADO", "demora": round(demora, 2)}
        conn.sendall(json.dumps(respuesta).encode())
        print(f"[WORKER {puerto}] Pedido {pedido['id']} entregado en {demora:.2f}s")
    except Exception as e:
        print(f"[WORKER {puerto}] Error: {e}")
    finally:
        conn.close()


def main(puerto):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(("0.0.0.0", puerto))
    s.listen()
    print(f"[WORKER {puerto}] activo, esperando pedidos...")
    while True:
        conn, addr = s.accept()
        threading.Thread(target=atender, args=(conn, addr, puerto), daemon=True).start()


if __name__ == "__main__":
    main(int(sys.argv[1]))
