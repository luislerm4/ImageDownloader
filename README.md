# ImageDownloader
### Luis Angel Lerma Coss
### a225203072

Aplicación de consola en Java que recibe una URL (`http` o `https`), descarga su contenido HTML detectando el `charset`, extrae referencias de etiquetas `<img>`, resuelve URLs relativas frente a la URL base filtrando esquemas no admitidos (`data:`, `javascript:`, etc.) y descarga de forma segura las imágenes válidas como archivos binarios en un directorio local.

## Requisitos

* **JDK** instalado (compatible con la versión de Java configurada en tu entorno, por ejemplo Java 17/21/25).
* **Conexión a internet** para consultar las páginas y descargar las imágenes.
* **Git** para el control de versiones del proyecto.

## Estructura del proyecto

```text
ImageDownloader/
├── src/
│   └── main/
│       └── java/
│           └── ImageDownloader.java
├── .gitignore
└── README.md
```
## Preguntas de reflexión

### 1. ¿Qué responsabilidad desempeña la aplicación dentro de una arquitectura cliente-servidor?
Actúa como **cliente**: valida la dirección de entrada, abre conexiones salientes hacia el servidor, solicita recursos (primero el documento HTML y luego cada imagen referenciada), interpreta las cabeceras y códigos de respuesta, y procesa o almacena localmente los datos recibidos sin alojar ni servir contenido a otros clientes.

### 2. ¿Qué diferencias existen entre `URL`, `URLConnection` y `HttpURLConnection`?
* **`URL`**: Representa la identificación y estructura de un recurso en la red (protocolo, servidor, puerto, ruta, etc.), pero por sí sola no abre una conexión de red.
* **`URLConnection`**: Es la abstracción general para comunicar la aplicación con el recurso apuntado por una `URL` (permite configurar *timeouts*, leer cabeceras como `Content-Type` o `Content-Length` y obtener flujos de entrada/salida).
* **`HttpURLConnection`**: Es una subclase especializada de `URLConnection` para el protocolo HTTP/HTTPS, que expone funcionalidades propias de HTTP como el método de petición (`GET`, `POST`, etc.), códigos de estado (`200`, `404`, etc.) y desconexión explícita (`disconnect()`).

### 3. ¿Qué sucede al invocar `openConnection()`?
Crea y devuelve un objeto de conexión (`URLConnection` o `HttpURLConnection`) asociado a la `URL`, pero **no abre todavía la conexión de red ni envía la petición al servidor**; únicamente prepara el manejador para configurarlo (por ejemplo, *timeouts* o método HTTP) antes de iniciar la comunicación real.

### 4. ¿Qué operaciones provocan que la conexión se establezca realmente?
Operaciones que exigen comunicarse o leer datos del servidor, como obtener el flujo de entrada (`getInputStream()`), consultar la respuesta/código HTTP (`getResponseCode()`) o solicitar metadatos/cabeceras que requieren la respuesta del servidor (`getContentType()`, `getContentLengthLong()`, etc.).

### 5. ¿Por qué una página HTML puede requerir múltiples conexiones?
Porque el documento HTML inicial normalmente solo contiene el marcado y referencias (como `<img src="...">`, hojas de estilo o *scripts*). Cada recurso externo alojado en el servidor o en otro origen requiere su propia petición/conexión independiente para descargarse y completarse.

### 6. ¿Cuál es la diferencia entre `getInputStream()` y `getContentType()`?
* **`getInputStream()`**: Devuelve el flujo de bytes (`InputStream`) para leer el **cuerpo** de la respuesta (el contenido HTML o los bytes binarios de la imagen).
* **`getContentType()`**: Devuelve la cabecera `Content-Type` de la respuesta (por ejemplo `text/html; charset=utf-8` o `image/png`), que indica el **tipo de medio** y, si aplica, la codificación de caracteres antes o al interpretar el cuerpo.

### 7. ¿Por qué distinguir recursos textuales y binarios?
Porque los recursos textuales (como HTML) se interpretan como caracteres usando una codificación (`Charset`, por ejemplo UTF-8) y lectores de texto (`BufferedReader`), mientras que los recursos binarios (como PNG, JPG, GIF o WebP) son secuencias exactas de bytes (`InputStream`) que se corrompen si se decodifican o alteran como texto.

### 8. ¿Para qué sirven los tiempos máximos de conexión y lectura?
* **Tiempo máximo de conexión (`connectTimeout`)**: Evita que el programa quede colgado indefinidamente si el servidor no responde o la red no logra establecer la conexión.
* **Tiempo máximo de lectura (`readTimeout`)**: Evita bloqueos prolongados si la conexión se abrió pero el servidor se detiene o tarda demasiado en enviar los siguientes bytes.

### 9. ¿Qué limitaciones tiene analizar HTML mediante expresiones regulares?
Las expresiones regulares no entienden la estructura jerárquica ni las reglas completas de un parser HTML: pueden fallar ante comentarios, etiquetas mal cerradas o anidadas, atributos con comillas escapadas o sin comillas, saltos de línea inesperados, o contenido generado dinámicamente con JavaScript que no viene en el HTML inicial.

### 10. ¿Cómo podría implementarse la descarga concurrente usando únicamente la API estándar de Java?
Usando `java.util.concurrent` (como `ExecutorService` / `Executors.newFixedThreadPool(...)` o `Executors.newVirtualThreadPerTaskExecutor()`), enviando cada descarga de imagen como una tarea independiente (`Runnable` o `Callable`), cuidando que el contador o nombrado de archivos (`imagen_001...`) y la actualización de métricas/contadores sean seguros entre hilos (por ejemplo con `AtomicInteger` o sincronización) y cerrando el *executor* con `shutdown()` y `awaitTermination(...)`.

