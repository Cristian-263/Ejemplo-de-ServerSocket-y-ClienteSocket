package com.cristian.centralrobots.server;

import com.cristian.centralrobots.core.InstructionBox;
import com.cristian.centralrobots.robots.Robot;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal del Servidor de la Central de Robots.
 * <p>
 * Esta clase actúa como el punto de entrada (Entry Point) del sistema.
 * Responsabilidades:
 * </p>
 * <ol>
 * <li>Inicializar el recurso compartido ({@link InstructionBox}).</li>
 * <li>Crear y arrancar los hilos de los {@link Robot} de forma independiente.</li>
 * <li>Abrir el {@link ServerSocket} y aceptar conexiones de clientes concurrentemente,
 * delegando cada una en un {@link ClientHandler}.</li>
 * </ol>
 */
public class RobotServer {

    private static final Logger logger = Logger.getLogger(RobotServer.class.getName());
    
    // Configuración básica
    private static final int PORT = 9000;
    private static final int NUM_ROBOTS = 3;

    public static void main(String[] args) {
        logger.info("Iniciando Central de Control de Robots...");

        // 1. Crear el Monitor (Buzón compartido)
        InstructionBox instructionBox = new InstructionBox();

        // 2. Crear y arrancar los Robots (Consumidores)
        for (int i = 1; i <= NUM_ROBOTS; i++) {
            Robot robot = new Robot(i, instructionBox);
            new Thread(robot).start(); // Lanzamos el hilo del robot
        }

        // 3. Bucle principal del servidor (Aceptar clientes)
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.log(Level.INFO, "Servidor escuchando en el puerto {0}", PORT);
            
            while (instructionBox.isActive()) {
                try {
                    // Esperar nueva conexión (bloqueante)
                    Socket clientSocket = serverSocket.accept();
                    
                    // Si el sistema se ha apagado justo mientras esperábamos, cerramos
                    if (!instructionBox.isActive()) {
                        clientSocket.close();
                        break;
                    }

                    logger.info("Nuevo cliente conectado: " + clientSocket.getInetAddress());

                    // Crear un hilo para atender al cliente (Productor)
                    ClientHandler handler = new ClientHandler(clientSocket, instructionBox);
                    new Thread(handler).start();
                    
                } catch (IOException e) {
                    if (instructionBox.isActive()) {
                        logger.log(Level.SEVERE, "Error aceptando conexión", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "No se pudo iniciar el servidor en el puerto " + PORT, e);
        }
        
        logger.info("Servidor detenido. Bye!");
    }
}