"""
Configuración central del sistema de asignación de pedidos.
Cambia aquí las IPs de los workers cuando corran en otras computadoras.
"""
import os

# ---------- Servidor de modelos de la carrera (Open WebUI) ----------
IA_URL = os.environ.get("IA_URL", "http://10.20.202.199:3000")
IA_KEY = os.environ.get("OPENWEBUI_KEY", "")      # export OPENWEBUI_KEY="sk-..."
IA_MODELO = os.environ.get("IA_MODELO", "qwen3-coder:30b")
IA_TIMEOUT = 60                                   # segundos

# ---------- Workers (nodos de procesamiento) ----------
# ip: IP de la PC donde corre el worker (127.0.0.1 si es en tu misma laptop)
# ubicacion: posición simulada del repartidor en un mapa de 10x10 km
WORKERS = [
    {"nombre": "W1", "ip": "127.0.0.1", "puerto": 7001, "ubicacion": (2, 2)},
    {"nombre": "W2", "ip": "127.0.0.1", "puerto": 7002, "ubicacion": (8, 7)},
]
