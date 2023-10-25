/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import excepciones.ConnectException;
import excepciones.TimeOutException;
import excepciones.UserAlreadyExistsException;
import excepciones.UserNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase se utiliza para gestionar la base de datos.
 *
 * @author Adrian
 */
public class DAOImplementacion implements Sign {

    private Connection con;
    private PreparedStatement stmt;
    private ResourceBundle config;
    private static Pool pool;
    private String url;
    private String user;
    private String pswd;
    private static final Logger LOG = Logger.getLogger(DAOImplementacion.class.getName());

    public DAOImplementacion() {
        //Configuración para conectarse a la base de datos
        this.config = ResourceBundle.getBundle("");
        this.url = config.getString("");
        this.user = config.getString("");
        this.pswd = config.getString("");

        //this.pool = pool.getPool();
    }

    /**
     * Metodo para insertar usuarios a la base de datos.
     *
     * @param user
     * @return user
     * @throws ServerConnectionException
     * @throws UserAlreadyExistsException
     */
    @Override
    public User excecuteLogin(User user) throws ConnectException, UserAlreadyExistsException {
        /*try {
            ResultSet rs = null;
            String nombre;
            String email;
            String contraseña;
            Integer telefono;
            Integer zip_code;
            String direccion;
            con = pool.getConnection();
            
            try{
                
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                pool.devolverConexion(con);
                if(rs != null){
                    try {
                        rs.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (TimeOutException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        }
*/
        return user;
    }

    @Override
    public User executeSignIn(User user) throws UserNotFoundException, ConnectException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
