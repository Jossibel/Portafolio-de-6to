"""
Agente de decisión (extraído del antiguo gateway.py).
Decide a qué worker enviar cada pedido.

Modos:
  ia     -> consulta al modelo del servidor de la carrera
  rr     -> Round Robin (regla fija)
  carga  -> el worker con menos pedidos activos (regla fija)
"""
import itertools
import re
import threading

import requests

from config import IA_URL, IA_KEY, IA_MODELO, IA_TIMEOUT, WORKERS

_rr = itertools.cycle(range(len(WORKERS)))
_rr_lock = threading.Lock()


def _round_robin(disponibles):
    with _rr_lock:
        for _ in range(len(WORKERS)):
            i = next(_rr)
            if i in disponibles:
                return i
    return disponibles[0]


def _menor_carga(estado, disponibles):
    return min(disponibles, key=lambda i: estado[WORKERS[i]["nombre"]]["carga"])


def _construir_prompt(pedido, estado, disponibles):
    lineas = []
    for i in disponibles:
        w = WORKERS[i]
        lineas.append(
            f"- Worker {w['puerto']}: carga actual = {estado[w['nombre']]['carga']} pedidos, "
            f"distancia al cliente = {pedido['distancias'][w['nombre']]} km"
        )
    return (
        "Eres un sistema de asignación de pedidos de delivery.\n"
        f"Pedido #{pedido['id']} con prioridad {pedido['prioridad']}.\n"
        "Workers disponibles:\n" + "\n".join(lineas) + "\n\n"
        "Reglas: equilibra la carga entre workers y prefiere el más cercano. "
        "Si la prioridad es ALTA, da más peso a la distancia.\n"
        "Responde ÚNICAMENTE con el número de puerto del worker elegido, sin texto adicional."
    )


def _consultar_ia(pedido, estado, disponibles):
    prompt = _construir_prompt(pedido, estado, disponibles)
    r = requests.post(
        f"{IA_URL}/api/chat/completions",
        headers={"Authorization": f"Bearer {IA_KEY}"},
        json={"model": IA_MODELO,
              "messages": [{"role": "user", "content": prompt}],
              "temperature": 0},
        timeout=IA_TIMEOUT,
    )
    r.raise_for_status()
    texto = r.json()["choices"][0]["message"]["content"]
    # Quitar bloques <think> de modelos con razonamiento (qwen3)
    texto = re.sub(r"<think>.*?</think>", "", texto, flags=re.S)

    for i in disponibles:
        if str(WORKERS[i]["puerto"]) in texto:
            return i, texto.strip()
    raise ValueError(f"Respuesta no interpretable: {texto!r}")


def decidir(pedido, estado, modo):
    """Devuelve (indice_worker, metodo_usado)."""
    disponibles = [i for i, w in enumerate(WORKERS) if estado[w["nombre"]]["activo"]]
    if not disponibles:
        raise RuntimeError("No hay workers disponibles")

    if modo == "rr":
        return _round_robin(disponibles), "rr"
    if modo == "carga":
        return _menor_carga(estado, disponibles), "carga"

    try:
        idx, respuesta = _consultar_ia(pedido, estado, disponibles)
        print(f"[IA] Pedido {pedido['id']}: modelo respondió '{respuesta}'")
        return idx, "ia"
    except Exception as e:
        print(f"[IA] Error ({e}). Fallback: menor carga")
        return _menor_carga(estado, disponibles), "fallback"
