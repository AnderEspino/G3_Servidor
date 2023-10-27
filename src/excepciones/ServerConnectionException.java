
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excepciones;

/**
 * Excepcion que captura si ha ocurrido algún error con la base de datos
 * @author Diego
 */
public class ServerConnectionException extends Exception {

    public ServerConnectionException(String msg) {
        super(msg);
    }
}

