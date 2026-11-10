"""
Generador de pedidos (antes: productor de datos de sensores).
Crea pedidos y los coloca en una cola IPC (multiprocessing.Queue).
"""
import random
import time

ZONAS = ["Centro", "Norte", "Sur", "Este", "Oeste"]


def productor_main(cola_entrada, total_pedidos, intervalo):
    for i in range(1, total_pedidos + 1):
        pedido = {
            "id": i,
            "cliente": f"Cliente-{i:03d}",
            "zona": random.choice(ZONAS),
            "ubicacion": (round(random.uniform(0, 10), 2),
                          round(random.uniform(0, 10), 2)),
            "prioridad": random.choices(["ALTA", "NORMAL"], weights=[3, 7])[0],
            "creado": time.time(),
        }
        print(f"[PRODUCTOR] Pedido {pedido['id']} generado "
              f"({pedido['zona']}, {pedido['prioridad']})")
        cola_entrada.put(pedido)
        time.sleep(intervalo)

    cola_entrada.put(None)  # señal de fin
