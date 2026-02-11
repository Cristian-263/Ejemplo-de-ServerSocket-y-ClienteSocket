package com.cristian.centralrobots.server;

import com.cristian.centralrobots.core.InstructionBox;
import com.cristian.centralrobots.core.InstructionParser;
import com.cristian.centralrobots.domain.CommandType;
import com.cristian.centralrobots.domain.Instruction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hilo encargado de gestionar la comunicación con un único cliente conectado.
 * <p>
 * Implementa el protocolo de comunicación sobre TCP/IP. Lee mensajes del socket, 
 * los valida usando {@link InstructionParser} y los deposita en el 
 * {@link InstructionBox} para que sean consumidos por los hilos de los robots.
 * </p>
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final Socket clientSocket;
    private final InstructionBox instructionBox;

    /**
     * Constructor del manejador de cliente.
     * @param socket El socket de conexión con el cliente activo.
     * @param instructionBox El buzón compartido (Monitor) donde depositar las órdenes.
     */
    public ClientHandler(Socket socket, InstructionBox instructionBox) {
        this.clientSocket = socket;
        this.instructionBox = instructionBox;
    }

    /**
     * Lógica de ejecución del hilo. 
     * <p>
     * Mantiene una escucha activa sobre el flujo de entrada del socket. 
     * Procesa comandos línea a línea, gestiona el comando especial de 
     * SHUTDOWN y asegura el cierre de recursos al finalizar la conexión.
     * </p>
     */
    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                
                if (!instructionBox.isActive()) {
                    out.println("ERROR|El servidor se esta apagando");
                    break;
                }

                try {
                    Instruction instr = InstructionParser.parse(inputLine);

                    if (instr.getCommand() == CommandType.SHUTDOWN) {
                        out.println("OK|Apagando servidor...");
                        instructionBox.shutdown();
                        break;
                    }

                    instructionBox.put(instr);
                    out.println("OK|Instruccion aceptada para Robot " + instr.getRobotId());

                } catch (IllegalArgumentException e) {
                    out.println("ERROR|" + e.getMessage());
                    logger.log(Level.WARNING, "Mensaje invalido recibido: {0}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Cliente desconectado: {0}", clientSocket.getInetAddress());
        } finally {
            closeSocket();
        }
    }

    /**
     * Método auxiliar público para garantizar el cierre del socket del cliente.
     */
    public void closeSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.warning("Error cerrando socket de cliente");
        }
    }
}