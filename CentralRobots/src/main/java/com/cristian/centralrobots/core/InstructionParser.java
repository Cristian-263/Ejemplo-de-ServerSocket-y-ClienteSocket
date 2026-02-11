package com.cristian.centralrobots.core;

import com.cristian.centralrobots.domain.CommandType;
import com.cristian.centralrobots.domain.Instruction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase utilitaria encargada de analizar (parsear) las cadenas de texto
 * provenientes de los sockets y convertirlas en objetos Instruction.
 */
public class InstructionParser {

    private static final Logger logger = Logger.getLogger(InstructionParser.class.getName());

    /**
     * Convierte una línea de texto en un objeto Instruction validado.
     * <p>
     * Formato esperado: ROBOT_ID | COMANDO | PARAMS
     * Ejemplo válido: "1|MOVE|10"
     * @param rawLine La línea de texto recibida por el socket.
     * @return Objeto Instruction si el formato es válido.
     * @throws IllegalArgumentException Si el formato es incorrecto, el ID no es numérico o el comando no existe.
     */
    public static Instruction parse(String rawLine) throws IllegalArgumentException {
        // 1. Validaciones básicas de nulidad
        if (rawLine == null || rawLine.trim().isEmpty()) {
            throw new IllegalArgumentException("Mensaje vacío");
        }

        // 2. Separamos por la barra vertical "|"
        // (Usamos \\| porque en expresiones regulares | es un carácter especial)
        String[] parts = rawLine.split("\\|");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Formato incorrecto. Se espera: ID|COMANDO|PARAMS");
        }

        try {
            // 3. Parseamos el ID del Robot (debe ser un numero)
            int id = Integer.parseInt(parts[0].trim());

            // 4. Parseamos el Comando (debe coincidir con el Enum)
            String cmdStr = parts[1].trim().toUpperCase();
            CommandType command = CommandType.valueOf(cmdStr); 

            // 5. Obtenemos los Parámetros (si los hay)
            String params = (parts.length > 2) ? parts[2].trim() : "";

            // Todo correcto: devolvemos el objeto
            return new Instruction(id, command, params);

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Error parseando ID: {0}", parts[0]);
            throw new IllegalArgumentException("El ID del robot debe ser un numero entero");
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Comando desconocido: {0}", parts[1]);
            throw new IllegalArgumentException("Comando desconocido o invalido");
        }
    }
}