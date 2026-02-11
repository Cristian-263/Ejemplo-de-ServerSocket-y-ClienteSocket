package com.cristian.centralrobots.server;

import java.io.*;
import java.net.Socket;

/**
 * Cliente de prueba (Dummy Client) para validar la comunicación por Sockets.
 * <p>
 * Esta clase simula el comportamiento de un panel de control externo que se conecta
 * al servidor de robots para enviar instrucciones secuenciales y verificar las respuestas.
 * </p>
 * <strong>Funcionalidad:</strong>
 * <ul>
 * <li>Establece conexión TCP con el puerto 9000.</li>
 * <li>Envía órdenes con formato ID|COMANDO|PARAMS.</li>
 * <li>Muestra por consola la respuesta (ACK/NACK) del servidor.</li>
 * </ul>
 */
public class TestClient {

    /**
     * Punto de entrada para ejecutar la simulación del cliente.
     * Envía una serie de comandos predefinidos para testear la robustez del servidor.
     * * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        // Conexión al puerto 9000 usando Try-with-resources para asegurar el cierre
        try (Socket socket = new Socket("localhost", 9000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            System.out.println("Conectado al servidor. Enviando órdenes...");

            // Orden 1: Mover Robot 1
            out.println("1|MOVE|10");
            System.out.println("Servidor dice: " + in.readLine());

            // Orden 2: Girar Robot 2
            out.println("2|TURN|LEFT");
            System.out.println("Servidor dice: " + in.readLine());

            // Orden 3: Error a propósito (Comando inexistente)
            out.println("99|FLY|ALTO");
            System.out.println("Servidor dice: " + in.readLine());

            // Orden 4: Petición de apagado global
            out.println("0|SHUTDOWN|"); 
            System.out.println("Servidor dice: " + in.readLine());
            
        } catch (IOException e) {
            System.err.println("Error de comunicación: " + e.getMessage());
        }
    }
}