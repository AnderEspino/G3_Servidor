/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excepciones;

/**
 * Excepcion que trata si la base de datos está operativa al realizar cualquier acción con el servidor
 * @author Diego
 */
public class NotOperativeDataBaseException extends Exception{
    
    public NotOperativeDataBaseException(String msg) {
        super(msg);
    }
}
