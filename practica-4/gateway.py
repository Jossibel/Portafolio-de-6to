"""
Gateway: cliente TCP que envía el pedido al worker elegido
y espera su respuesta (fusiona el antiguo cliente.py y el envío del gateway).
"""
import json
import socket


def _recibir_todo(sock):
    partes = []
    while True:
        bloque = sock.recv(4096)
        if not bloque:
            break
        partes.append(bloque)
    return b"".join(partes)


def enviar(worker, pedido, timeout=30):
    """Envía el pedido y devuelve la respuesta del worker (dict).
    Lanza excepción si el worker no responde."""
    with socket.create_connection((worker["ip"], worker["puerto"]), timeout=timeout) as s:
        s.sendall(json.dumps(pedido).encode())
        s.shutdown(socket.SHUT_WR)          # indica fin del mensaje
        respuesta = _recibir_todo(s)
    return json.loads(respuesta.decode())
