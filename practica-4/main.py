"""
Servidor central: arranca todo el sistema.

  productor (proceso) --Queue--> procesador (proceso) --Queue--> despachadores (hilos)
  despachador: agente_ia.decidir()  ->  gateway.enviar()  ->  worker remoto

Uso:
  python3 main.py --modo ia    --pedidos 20 --hilos 5
  python3 main.py --modo rr    --pedidos 20 --hilos 5
  python3 main.py --modo carga --pedidos 20 --hilos 5
"""
import argparse
import csv
import multiprocessing
import threading
import time
from collections import Counter

import agente_ia
import gateway
import procesador
import productor
from config import WORKERS

# Estado compartido de los workers (lo ve el agente para decidir)
estado = {w["nombre"]: {"carga": 0, "activo": True} for w in WORKERS}
estado_lock = threading.Lock()
resultados = []
resultados_lock = threading.Lock()


def despachador(cola_lista, modo):
    while True:
        pedido = cola_lista.get()
        if pedido is None:
            break

        inicio = time.time()
        intentos = 0
        while intentos < len(WORKERS):
            intentos += 1
            with estado_lock:
                foto = {k: dict(v) for k, v in estado.items()}
            try:
                t0 = time.time()
                idx, metodo = agente_ia.decidir(pedido, foto, modo)
                t_decision = time.time() - t0
            except RuntimeError as e:
                print(f"[MAIN] Pedido {pedido['id']} PERDIDO: {e}")
                break

            w = WORKERS[idx]
            pedido["distancia_km"] = pedido["distancias"][w["nombre"]]
            with estado_lock:
                estado[w["nombre"]]["carga"] += 1
                carga_al_enviar = estado[w["nombre"]]["carga"]
            print(f"[GATEWAY] Pedido {pedido['id']} -> {w['nombre']} ({w['ip']}:{w['puerto']}) "
                  f"[{metodo}] carga={carga_al_enviar}")
            try:
                resp = gateway.enviar(w, pedido)
                with estado_lock:
                    estado[w["nombre"]]["carga"] -= 1
                total = time.time() - inicio
                with resultados_lock:
                    resultados.append({
                        "pedido": pedido["id"], "prioridad": pedido["prioridad"],
                        "worker": w["nombre"], "metodo": metodo,
                        "distancia_km": pedido["distancia_km"],
                        "carga_al_enviar": carga_al_enviar,
                        "t_decision": round(t_decision, 3),
                        "t_total": round(total, 3),
                    })
                print(f"[MAIN] Pedido {pedido['id']} {resp['estado']} por {w['nombre']} "
                      f"en {total:.2f}s")
                break
            except OSError as e:
                # Tolerancia a fallos: marcar worker caído y reintentar con otro
                print(f"[GATEWAY] {w['nombre']} no responde ({e}). Reasignando pedido {pedido['id']}...")
                with estado_lock:
                    estado[w["nombre"]]["carga"] -= 1
                    estado[w["nombre"]]["activo"] = False


def resumen(modo, duracion):
    print("\n" + "=" * 55)
    print(f" RESUMEN  modo={modo}")
    print("=" * 55)
    if not resultados:
        print(" Sin resultados.")
        return
    n = len(resultados)
    prom = sum(r["t_total"] for r in resultados) / n
    prom_dec = sum(r["t_decision"] for r in resultados) / n
    dist = sum(r["distancia_km"] for r in resultados) / n
    print(f" Pedidos entregados      : {n}")
    print(f" Tiempo total de prueba  : {duracion:.2f} s")
    print(f" Tiempo medio respuesta  : {prom:.2f} s")
    print(f" Tiempo medio de decisión: {prom_dec:.3f} s")
    print(f" Distancia media         : {dist:.2f} km")
    print(" Distribución de carga   :")
    for nombre, c in sorted(Counter(r["worker"] for r in resultados).items()):
        print(f"   {nombre}: {c} pedidos ({100 * c / n:.0f}%)")
    metodos = Counter(r["metodo"] for r in resultados)
    print(f" Métodos usados          : {dict(metodos)}")

    archivo = f"resultados_{modo}.csv"
    with open(archivo, "w", newline="") as f:
        wr = csv.DictWriter(f, fieldnames=resultados[0].keys())
        wr.writeheader()
        wr.writerows(sorted(resultados, key=lambda r: r["pedido"]))
    print(f" Detalle guardado en     : {archivo}")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--modo", choices=["ia", "rr", "carga"], default="ia")
    ap.add_argument("--pedidos", type=int, default=10)
    ap.add_argument("--hilos", type=int, default=3, help="despachadores concurrentes")
    ap.add_argument("--intervalo", type=float, default=0.2, help="segundos entre pedidos")
    args = ap.parse_args()

    cola_entrada = multiprocessing.Queue()
    cola_lista = multiprocessing.Queue()

    p1 = multiprocessing.Process(target=productor.productor_main,
                                 args=(cola_entrada, args.pedidos, args.intervalo))
    p2 = multiprocessing.Process(target=procesador.procesador_main,
                                 args=(cola_entrada, cola_lista, args.hilos))

    print(f"[MAIN] Iniciando: modo={args.modo}, pedidos={args.pedidos}, hilos={args.hilos}")
    inicio = time.time()
    p1.start()
    p2.start()

    hilos = [threading.Thread(target=despachador, args=(cola_lista, args.modo))
             for _ in range(args.hilos)]
    for h in hilos:
        h.start()
    for h in hilos:
        h.join()
    p1.join()
    p2.join()

    resumen(args.modo, time.time() - inicio)


if __name__ == "__main__":
    main()
