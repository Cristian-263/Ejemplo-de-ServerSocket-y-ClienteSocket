package com.cristian.centralrobots.domain;

/**
 * Define los tipos de comandos permitidos que los robots pueden ejecutar.
 * <p>
 * Cada constante representa una acción específica que el sistema es capaz
 * de procesar y asignar a un hilo de robot.
 * </p>
 */
public enum CommandType {
    
    /** * Ordena al robot desplazarse una distancia determinada. 
     */
    MOVE,

    /** * Ordena al robot recoger un objeto en su posición actual. 
     */
    PICK,

    /** * Comando especial para iniciar el protocolo de apagado del servidor y los hilos. 
     */
    SHUTDOWN,

    /** * Solicita al robot que informe sobre su estado actual (batería, posición, etc.). 
     */
    STATUS,

    /** * Ordena al robot realizar un giro (ej: LEFT, RIGHT). 
     */
    TURN,

    /** * Valor por defecto para gestionar comandos no reconocidos o erróneos. 
     */
    UNKNOWN
}