"""
Procesamiento de pedidos (antes: procesador de alertas).
Valida cada pedido, calcula su distancia a cada worker y lo pasa
a la cola de pedidos listos para asignar.
"""
import math

from config import WORKERS


def distancia(a, b):
    return round(math.dist(a, b), 2)


def procesador_main(cola_entrada, cola_lista, num_despachadores):
    while True:
        pedido = cola_entrada.get()
        if pedido is None:
            break

        # Validación básica
        x, y = pedido["ubicacion"]
        if not (0 <= x <= 10 and 0 <= y <= 10):
            print(f"[PROCESADOR] Pedido {pedido['id']} descartado: ubicación inválida")
            continue

        # Enriquecer: distancia (km) del pedido a cada worker
        pedido["distancias"] = {
            w["nombre"]: distancia(pedido["ubicacion"], w["ubicacion"])
            for w in WORKERS
        }
        print(f"[PROCESADOR] Pedido {pedido['id']} listo -> distancias {pedido['distancias']}")
        cola_lista.put(pedido)

    # Una señal de fin por cada hilo despachador
    for _ in range(num_despachadores):
        cola_lista.put(None)
