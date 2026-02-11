# Central de Control de Robots (Socket + Hilos)

Este proyecto implementa un sistema multihilo para la gestión de instrucciones de robots industriales mediante una arquitectura de servidor TCP/IP y un buzón compartido siguiendo el patrón de diseño **Monitor**.

## 🚀 Instrucciones de Ejecución

### 1. Iniciar el Servidor
* Localiza la clase `com.cristian.centralrobots.server.RobotServer`.
* Haz clic derecho y selecciona **Run As > Java Application**.
* El servidor se iniciará en el puerto **9000**. Verás en consola cómo los hilos de los Robots se activan y quedan en estado "ONLINE".

### 2. Probar con el Cliente de Prueba
* Ejecuta la clase `com.cristian.centralrobots.server.TestClient`.
* Esta clase enviará automáticamente ráfagas de comandos para validar el procesamiento secuencial y la respuesta del servidor.

### 3. Prueba Manual vía Telnet
Es posible validar el protocolo en tiempo real desde una terminal:
* Comando: `telnet localhost 9000`
* Formato: `ID|COMANDO|PARAMETROS` (Ejemplo: `1|MOVE|10`).
* *Nota: Se han omitido tildes en las respuestas del servidor para garantizar la compatibilidad de visualización en consolas externas.*

## 🧪 Ejecución de Tests (JUnit 5)
El proyecto incluye una suite de pruebas automatizadas que cubren los requisitos críticos:
* **Ubicación:** `src/test/java/com/cristian.centralrobots/RobotSystemTest.java`.
* **Ejecución:** Clic derecho > **Run As > JUnit Test**.
* **Cobertura:** Validación de protocolo (Parsing), persistencia en buffer, consumo selectivo por ID y test de integración de red.

## 🛠️ Decisiones de Diseño
* **Monitor (`InstructionBox`):** Implementación de un recurso compartido sincronizado mediante `wait()` y `notifyAll()`. Los hilos robot permanecen en espera pasiva, optimizando el uso de CPU.
* **Consumo Selectivo:** El monitor garantiza que cada hilo de robot extraiga únicamente las instrucciones destinadas a su identificador único.
* **Protocolo de Apagado:** El sistema soporta un cierre ordenado mediante el comando `0|SHUTDOWN|`, liberando sockets y finalizando los hilos de forma segura.
* **Documentación:** El proyecto incluye Javadoc técnico completo en la carpeta `/doc`.

## 📚 Estructura de Paquetes
- `domain`: Modelos de datos e inmutabilidad.
- `core`: Lógica de procesamiento y monitor de sincronización.
- `server`: Gestión de red y hilos de cliente (Sockets).
- `robots`: Lógica de ejecución de los hilos consumidores.