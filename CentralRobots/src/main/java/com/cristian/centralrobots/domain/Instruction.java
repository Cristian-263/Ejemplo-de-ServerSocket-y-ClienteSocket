package com.cristian.centralrobots.domain;

/**
 * Clase inmutable que representa una instrucción validada y lista para ser procesada.
 * <p>
 * Contiene la información desglosada del mensaje enviado por el cliente:
 * ID del robot destino, tipo de comando y parámetros adicionales.
 */
public class Instruction {

    private final int robotId;
    private final CommandType command;
    private final String params;

    /**
     * Constructor principal de la instrucción.
     * @param robotId Identificador numérico del robot al que va dirigida.
     * @param command Tipo de comando (enum).
     * @param params Parámetros adicionales (ej: "10", "LEFT"). Puede ser cadena vacía.
     */
    public Instruction(int robotId, CommandType command, String params) {
        this.robotId = robotId;
        this.command = command;
        this.params = params;
    }

    /**
     * Obtiene el ID del robot objetivo.
     * @return int con el ID.
     */
    public int getRobotId() {
        return robotId;
    }

    /**
     * Obtiene el tipo de comando.
     * @return CommandType comando.
     */
    public CommandType getCommand() {
        return command;
    }

    /**
     * Obtiene los parámetros de la instrucción.
     * @return String con los parámetros o cadena vacía si no hay.
     */
    public String getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Instruction{robot=" + robotId + ", cmd=" + command + ", params='" + params + "'}";
    }
}