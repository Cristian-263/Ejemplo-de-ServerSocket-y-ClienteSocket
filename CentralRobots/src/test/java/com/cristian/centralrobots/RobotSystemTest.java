package com.cristian.centralrobots;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.cristian.centralrobots.core.InstructionParser;
import com.cristian.centralrobots.core.InstructionBox;
import com.cristian.centralrobots.domain.Instruction;
import com.cristian.centralrobots.domain.CommandType;
import com.cristian.centralrobots.server.ClientHandler;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Suite de pruebas automatizadas para la validación del sistema de control de robots.
 * <p>
 * Esta clase realiza pruebas unitarias sobre el análisis de protocolos y la lógica 
 * del monitor sincronizado, así como pruebas de integración sobre la capa de red.
 * </p>
 */
public class RobotSystemTest {

    /**
     * Valida que el analizador de instrucciones procese correctamente las cadenas de texto
     * siguiendo el protocolo definido y gestione adecuadamente las excepciones ante formatos inválidos.
     */
    @Test
    @DisplayName("Validación de Procesamiento de Protocolo")
    public void testParserLogic() { 
        Instruction instr = InstructionParser.parse("1|MOVE|10");
        assertAll("Propiedades de la instrucción",
            () -> assertEquals(1, instr.getRobotId()),
            () -> assertEquals(CommandType.MOVE, instr.getCommand())
        );
        
        assertThrows(IllegalArgumentException.class, () -> InstructionParser.parse("INVALID_DATA"));
    }

    /**
     * Verifica la integridad de las operaciones de inserción y extracción en el buzón 
     * compartido, asegurando que los objetos Instruction mantienen su estado.
     * @throws InterruptedException si el hilo es interrumpido durante la espera.
     */
    @Test
    @DisplayName("Persistencia en Buffer Compartido")
    public void testBufferStorage() throws InterruptedException { 
        InstructionBox box = new InstructionBox();
        Instruction original = new Instruction(1, CommandType.MOVE, "10");
        
        box.put(original);
        Instruction recuperada = box.takeFor(1);
        
        assertNotNull(recuperada);
        assertEquals(original.getCommand(), recuperada.getCommand());
    }

    /**
     * Comprueba la capacidad del monitor para filtrar instrucciones de forma selectiva,
     * garantizando que un hilo consumidor solo obtenga tareas destinadas a su identificador único.
     * @throws InterruptedException si el hilo es interrumpido durante la espera.
     */
    @Test
    @DisplayName("Filtrado Selectivo por Identificador")
    public void testSelectiveConsumption() throws InterruptedException { 
        InstructionBox box = new InstructionBox();
        
        box.put(new Instruction(1, CommandType.MOVE, "10"));
        box.put(new Instruction(5, CommandType.TURN, "RIGHT"));

        Instruction result = box.takeFor(5);
        
        assertNotNull(result);
        assertEquals(5, result.getRobotId());
        assertEquals(CommandType.TURN, result.getCommand());
    }

    /**
     * Prueba de integración que simula un entorno de red real utilizando puertos efímeros.
     * Verifica el ciclo completo de vida de una petición: conexión TCP, envío de stream, 
     * procesamiento concurrente y recepción de respuesta ACK.
     * @throws IOException Si ocurre un error en la apertura de los sockets de prueba.
     */
    @Test
    @DisplayName("Integración End-to-End mediante Sockets")
    public void testNetworkIntegration() throws IOException { 
        InstructionBox box = new InstructionBox();
        
        try (ServerSocket tempServer = new ServerSocket(0)) {
            int port = tempServer.getLocalPort();
            
            Thread listener = new Thread(() -> {
                try {
                    Socket s = tempServer.accept();
                    new ClientHandler(s, box).run();
                } catch (Exception ignored) {}
            });
            listener.start();

            try (Socket client = new Socket("localhost", port);
                 PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                
                writer.println("1|MOVE|50");
                String response = reader.readLine();
                
                assertNotNull(response);
                assertTrue(response.contains("OK"), "El servidor debe confirmar la recepción");
            }
        }
    }

    /**
     * Valida el comportamiento del sistema ante una señal de terminación, asegurando 
     * que el monitor desbloquea los hilos en espera y permite un cierre limpio.
     * @throws InterruptedException si el hilo es interrumpido durante la espera.
     */
    @Test
    @DisplayName("Verificación de Protocolo de Apagado")
    public void testSystemShutdown() throws InterruptedException { 
        InstructionBox box = new InstructionBox();
        box.shutdown();
        assertNull(box.takeFor(1), "El retorno debe ser nulo tras el cese de actividad");
    }
}