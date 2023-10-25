/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import excepciones.NotOperativeDataBaseException;
import excepciones.ServerConnectionException;
import excepciones.TimeOutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pool de conexiones, es un stack de conexiones, que se encarga de abri y
 * cerrar conexiones a la base de datos a distintos usuarios
 *
 * @author Diego,Ander
 */
public class Pool {

    private ResourceBundle config;
    private String driverBD;
    private String urlDB;
    private String userDB;
    private String passDB;
    private static Pool pool;
    //Stack para almacenar las distintas conexiones
    private static Stack<Connection> pilaStack = new Stack<>();
    private static final Logger LOG = Logger.getLogger(Pool.class.getName());

    /**
     * Método openconnection del pool, abre las conexiones con la base de datos,
     * asigna una apertura de conexión a los distintos clientes que trata la
     * app;
     *
     * @author Diego,Ander
     * @return conn
     * @throws excepciones.NotOperativeDataBaseException
     * @throws excepciones.ServerConnectionException
     */
    public Connection openConnection() throws NotOperativeDataBaseException, ServerConnectionException {
        //Asignacion de los datos del archivo de configuración para realizar la conexión a la base de datos
        this.config = ResourceBundle.getBundle("/Utilidades/config.properties");
        this.driverBD = config.getString("DRIVER");
        this.urlDB = config.getString("DB");
        this.userDB = config.getString("DBUSER");
        this.passDB = config.getString("DBPASS");
        //Comprobamos si hace la conexión a la base de datos
        try {
            //Establece una conexión con nuestro driver de odoo, nos devuelve la conexión
            Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
            return conn;
        } catch (SQLException e) {
            throw new NotOperativeDataBaseException("Base de datos no operativa");
        }
    }

    /**
     * Método que crea un único pool, no pueden crearse más
     *
     * @author Diego,Ander
     * @return pool
     */
    public static Pool getPool() {
        if (pool == null) {
            pool = new Pool();
        }
        return pool;
    }

    /**
     * Método que nos devuelve la conexión que hemos creado
     *
     * @author Diego,Ander
     * @return pool
     */
    public Connection getConnection() {
        Connection conn = null;
        //Comprobamos el tamaño del stack, si es mayor que 0 sustituye la última conexión y la reemplaza por la nueva
        if (pilaStack.size() > 0) {
            pilaStack.pop();
        } else {
            //Si falla nos devuelve la conexión normal
            try {
                conn = openConnection();
            } catch (ServerConnectionException e) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, e);
            } catch (NotOperativeDataBaseException e) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return conn;
    }

    /**
     * Devuelve la conexión y pone la última conexión al tope de la pila
     *
     * @param con
     * @throws TimeOutException
     */
    public void returnConection(Connection con) throws TimeOutException {
        LOG.info("Returnea la conexión");
        pilaStack.push(con);
    }
}
