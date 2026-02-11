package com.cristian.centralrobots.robots;

import com.cristian.centralrobots.core.InstructionBox;
import com.cristian.centralrobots.domain.Instruction;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representa un robot individual que se ejecuta en su propio hilo.
 * <p>
 * Responsabilidades:
 * </p>
 * <ul>
 * <li>Consultar continuamente el buzón en busca de instrucciones para su ID.</li>
 * <li>Simular la ejecución de la tarea (tiempo de espera).</li>
 * <li>Finalizar su ejecución de forma limpia cuando el sistema se apaga.</li>
 * </ul>
 */
public class Robot implements Runnable {

    private static final Logger logger = Logger.getLogger(Robot.class.getName());

    private final int id;
    private final InstructionBox instructionBox;
    private final Random random;

    /**
     * Constructor del Robot.
     * @param id Identificador único del robot.
     * @param instructionBox Referencia al buzón compartido para obtener tareas.
     */
    public Robot(int id, InstructionBox instructionBox) {
        this.id = id;
        this.instructionBox = instructionBox;
        this.random = new Random();
    }

    /**
     * Lógica principal del hilo del robot (Ciclo de vida).
     * Mantiene un bucle activo solicitando tareas hasta recibir señal de parada.
     */
    @Override
    public void run() {
        logger.log(Level.INFO, "Robot {0} ONLINE y esperando órdenes...", id);

        try {
            while (true) {
                Instruction instruction = instructionBox.takeFor(this.id);

                if (instruction == null) {
                    break; 
                }

                executeInstruction(instruction);
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Robot {0} interrumpido abruptamente.", id);
            Thread.currentThread().interrupt(); 
        }

        logger.log(Level.INFO, "Robot {0} OFFLINE (Apagado correcto).", id);
    }

    /**
     * Simula la ejecución física de una instrucción mediante un retardo aleatorio.
     * @param instruction La instrucción a ejecutar.
     * @throws InterruptedException Si se interrumpe la simulación del tiempo de trabajo.
     */
    public void executeInstruction(Instruction instruction) throws InterruptedException { // <-- AHORA ES PUBLIC
        logger.log(Level.INFO, "Robot {0} PROCESANDO: {1} [{2}]", 
                new Object[]{id, instruction.getCommand(), instruction.getParams()});

        int simulationTime = 500 + random.nextInt(1000);
        Thread.sleep(simulationTime);

        logger.log(Level.INFO, "Robot {0} FIN TAREA: {1}", 
                new Object[]{id, instruction.getCommand()});
    }
}