/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excepciones;

/**
 * Excepcion que captura si la conexión con la base de datos tarda más de lo habitual
 * @author Diego
 */
public class TimeOutException extends Exception {

    public TimeOutException(String msg) {
        super(msg);
    }

}
