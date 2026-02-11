package com.cristian.centralrobots.core;

import com.cristian.centralrobots.domain.Instruction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Recurso compartido (Monitor) que gestiona la cola de instrucciones.
 * Implementa el patrón Productor-Consumidor de forma Thread-Safe.
 * <p>
 * <strong>Decisión de Diseño (Sincronización):</strong>
 * Se utilizan los mecanismos nativos de Java {@code wait()} y {@code notifyAll()} 
 * para gestionar la concurrencia:
 * <ul>
 * <li>Los robots esperan pasivamente (wait) si no hay instrucciones para ellos,
 * evitando así el consumo innecesario de CPU ("busy wait").</li>
 * <li>Cuando entra una instrucción, se notifica a todos (notifyAll) para que 
 * comprueben si es para ellos.</li>
 * </ul>
 */
public class InstructionBox {

    private static final Logger logger = Logger.getLogger(InstructionBox.class.getName());
    
    // Lista compartida donde se guardan las instrucciones pendientes
    private final List<Instruction> buffer;
    
    // Flag para controlar el apagado ordenado del sistema
    private boolean isSystemActive; 

    /**
     * Constructor del buzón. Inicializa la lista y activa el sistema.
     */
    public InstructionBox() {
        this.buffer = new ArrayList<>();
        this.isSystemActive = true;
    }

    /**
     * Método PRODUCTOR: Añade una instrucción al buzón.
     * <p>
     * Es {@code synchronized} para evitar condiciones de carrera al escribir en la lista.
     * Despierta a los hilos consumidores con {@code notifyAll()}.
     * @param instr La instrucción validada a encolar.
     */
    public synchronized void put(Instruction instr) {
        buffer.add(instr);
        logger.info("Buzón: Instrucción encolada para Robot " + instr.getRobotId());
        
        // Usamos notifyAll() en lugar de notify() porque podría haber varios robots esperando
        // y queremos asegurar que el destinatario correcto se entere.
        notifyAll();
    }

    /**
     * Método CONSUMIDOR: Busca y extrae una instrucción para un robot específico.
     * <p>
     * Si no encuentra instrucción, el hilo se bloquea (wait) hasta que llegue nueva información
     * o hasta que el sistema se apague.
     * @param robotId ID del robot que solicita trabajo.
     * @return La instrucción encontrada, o {@code null} si el sistema se está apagando.
     * @throws InterruptedException Si el hilo es interrumpido mientras espera.
     */
    public synchronized Instruction takeFor(int robotId) throws InterruptedException {
        // Bucle de espera (Guarded Block)
        while (isSystemActive) {
            // 1. Buscamos si hay algo para este robot en la lista
            for (int i = 0; i < buffer.size(); i++) {
                if (buffer.get(i).getRobotId() == robotId) {
                    // La sacamos de la lista y la devolvemos
                    return buffer.remove(i);
                }
            }

            // 2. Si no hay nada para mí, ME DUERMO (wait).
            // Esto libera el cerrojo (lock) para que otros puedan entrar a 'put' o 'takeFor'.
            wait();
        }
        
        // Si el bucle termina, es porque isSystemActive es false (Apagado ordenado)
        return null; 
    }

    /**
     * Inicia el protocolo de apagado del sistema.
     * Cambia el estado a inactivo y despierta a todos los robots para que finalicen.
     */
    public synchronized void shutdown() {
        logger.warning("Buzón: ¡SHUTDOWN recibido! Iniciando apagado ordenado...");
        this.isSystemActive = false;
        buffer.clear(); // Limpiamos tareas pendientes
        notifyAll();    // Despertamos a todos para que salgan del wait() y terminen
    }
    /**
     * Verifica si el sistema sigue activo o si se ha ordenado el apagado.
     * @return true si el sistema está operativo.
     */
    public synchronized boolean isActive() {
        return isSystemActive;
    }
}