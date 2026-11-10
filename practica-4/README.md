# Práctica 4: IPC y comunicación en red

Práctica APE Nro. 004 de Sistemas Distribuidos (docente José Guamán). Partimos del código de `u1 - copia/practica4` del repositorio del docente (https://github.com/joseguaman1/distribuidos) y lo cambiamos por un sistema de asignación de pedidos tipo delivery. El productor y el procesador se comunican con colas IPC, el gateway envía cada pedido por TCP a uno de los workers y un agente de IA decide a qué worker mandarlo según la carga y la distancia.

El informe completo está en `Informe_APE4_IPC_y_Red.pdf`.

## Archivos

```text
practica-4/
├── main.py          # arranca todo (modos ia, rr y carga)
├── productor.py     # genera los pedidos
├── procesador.py    # valida el pedido y calcula la distancia a cada worker
├── agente_ia.py     # decide el worker (IA, Round Robin o menor carga)
├── gateway.py       # cliente TCP que envía el pedido
├── worker.py        # servidor TCP que simula la entrega
├── config.py        # IP de los workers, URL y modelo de IA
└── Informe_APE4_IPC_y_Red.pdf
```

## Cómo ejecutarlo

Se necesita Python 3 con `requests` (en Debian viene con `python3-requests`).

Primero levantamos los dos workers, cada uno en su terminal:

```bash
python3 worker.py 7001
python3 worker.py 7002
```

Después, en otra terminal, corremos el sistema con el modo que queramos:

```bash
python3 main.py --modo rr    --pedidos 30 --hilos 6 --intervalo 0.05
python3 main.py --modo carga --pedidos 30 --hilos 6 --intervalo 0.05
```

El modo `ia` usa el servidor de modelos de la carrera (`http://10.20.202.199:3000`), así que solo funciona dentro de la red de la UNL y con una API key propia:

```bash
export OPENWEBUI_KEY="tu-api-key"
python3 main.py --modo ia --pedidos 10 --hilos 3
```

Al terminar, el programa muestra un resumen con el tiempo de respuesta y cuántos pedidos recibió cada worker, y guarda el detalle en `resultados_<modo>.csv`.

Para correr los workers en otras computadoras basta con copiar `worker.py` a esa PC y cambiar su IP en `config.py`.
