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
    private Integer max_User;
    private static Pool pool;
    //Stack para almacenar las distintas conexiones
    private static Stack<Connection> pilaStack = new Stack<>();
    private static final Logger LOG = Logger.getLogger(Pool.class.getName());

    //Constructor del pool con los parámetros necesarios para realizar la conexión a la base de datos
    public Pool(ResourceBundle config, String driverBD, String urlDB, String passDB) {
        //Asignacion de los datos del archivo de configuración para realizar la conexión a la base de datos
        this.config = ResourceBundle.getBundle("Utilidades.Config");
        this.driverBD = config.getString("DRIVER");
        this.urlDB = config.getString("CONEXION");
        this.userDB = config.getString("BDUSER");
        this.passDB = config.getString("BDPASS");
        this.max_User = Integer.parseInt(config.getString("MAX_USERS"));
    }

    //Constructor vacio
    public Pool() {
    }

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
    public synchronized Connection createConnection() throws NotOperativeDataBaseException, ServerConnectionException {
        this.config = ResourceBundle.getBundle("Utilidades.Config");
        this.driverBD = config.getString("DRIVER");
        this.urlDB = config.getString("CONEXION");
        this.userDB = config.getString("BDUSER");
        this.passDB = config.getString("BDPASS");
        this.max_User = Integer.parseInt(config.getString("MAX_USERS"));
        //Comprobamos si hace la conexión a la base de datos
        try {
            LOG.info("Creando conexión");
            //Establece una conexión con nuestro driver de odoo, nos devuelve la conexión
            Connection conn = DriverManager.getConnection(this.urlDB, this.userDB, this.passDB);
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
     * @return conn
     */
    public synchronized Connection getConnection() {

        Connection conn = null;
        LOG.info("Capturando conexión");
        //Comprobamos el tamaño del stack, si es mayor que 0 sustituye la última conexión y la reemplaza por la nueva
        if (pilaStack.size() > 0) {
            conn = pilaStack.pop();
        } else {
            //Si falla nos devuelve la conexión normal

            try {
                conn = createConnection();
            } catch (ServerConnectionException e) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, e);
            } catch (NotOperativeDataBaseException e) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return conn;
    }

    /**
     * Cierra la conexión del pool
     *
     * @author Adrían
     * @param con
     * @throws excepciones.TimeOutException
     */
    public synchronized void returnConection(Connection con) throws TimeOutException {
        LOG.info("Devolviendo conexión");
        pilaStack.push(con);
    }

    /**
     * Método que cierra las conexiones a cada cliente asignado
     *
     * @author Diego,Ander
     * @param con
     *
     */
    public synchronized void closePool(Connection con) {
        LOG.info("Cerrando conexión");
        for (int i = 0; !pilaStack.isEmpty(); i++) {
            try {
                pilaStack.pop().close();
            } catch (SQLException ex) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
