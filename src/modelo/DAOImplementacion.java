/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import excepciones.ConnectException;
import excepciones.IncorrectCredentialsException;
import excepciones.NotOperativeDataBaseException;
import excepciones.ServerConnectionException;
import excepciones.TimeOutException;
import excepciones.UserAlreadyExistsException;
import excepciones.UserNotFoundException;
import java.sql.Connection;
import java.sql.Date;
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

    //Sentencias SQL
    //Introducir un nuevo usuario a la bda
    final String INSERTAR_USUARIO = "insert into res_users (login, password) values (?,?)";
    final String INSERTAR_DATOS_USUARIO = "insert into res_partner (company_id, create_date, name, zip, city, phone, active) values (?,?,?,?,?,?,?)";
    final String INSERTAR_USUARIO_GRUPO = "insert into res_groups_users_rel (gid, uid) values (16,?), (26,?), (28,?), (31,?)";
    final String ID_USUARIO = "select MAX(id) as id from res_users";
    final String INSERTAR_USUARIO_COMPAÑIA = "insert into res_company_users_rel (cid, user_id) values (1,?)";
    final String ID_PARTNER = "select MAX(id) as id from res_partner";
    //Buscar los datos del usuario en la BDA
    final String BUSCAR_USUARIO = "select login, password from res_users where login = ? and password = ?";
    //BUscar si un usuario ya existe
    final String USUARIO_EXISTE = "select login from res_users where login=?";

    public DAOImplementacion() {
        //Configuración para conectarse a la base de datos
        this.config = ResourceBundle.getBundle("/Utilidades/Config.properties");
        this.url = config.getString("CONEXION");
        this.user = config.getString("DBUSER");
        this.pswd = config.getString("DBPASS");

        this.pool = pool.getPool();
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
        try {
            con = pool.createConnection();

            if (usuarioYaExiste(user.getEmail())) {
                throw new UserAlreadyExistsException("El usuario ya existe.");
            }

            try {
                stmt = con.prepareStatement(INSERTAR_USUARIO);

                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getContraseña());

                if (stmt.executeUpdate() == 1) {
                    stmt = con.prepareStatement(INSERTAR_DATOS_USUARIO);

                    stmt.setInt(1, user.getCompañia());
                    stmt.setDate(2, Date.valueOf(user.getFecha_ini()));
                    stmt.setString(3, user.getNombre());
                    stmt.setInt(4, user.getZip_code());
                    stmt.setString(5, user.getDireccion());
                    stmt.setInt(6, user.getTelefono());
                    stmt.setBoolean(7, user.isActivo());

                    if (stmt.executeUpdate() == 1) {
                        String id_Usuario = null;
                        stmt = con.prepareStatement(ID_USUARIO);

                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            id_Usuario = rs.getString("id");
                        } else if (id_Usuario == null) {
                            throw new SQLException("Ha ocurrido un error en la insercion de los datos.");
                        }

                        stmt = con.prepareStatement(INSERTAR_USUARIO_GRUPO);

                        stmt.setString(1, id_Usuario);

                        if (stmt.executeUpdate() == 1) {
                            String id_Partner = null;
                            stmt = con.prepareStatement(ID_PARTNER);

                            rs = stmt.executeQuery();
                            if (rs.next()) {
                                id_Partner = rs.getString("id");
                            } else if (id_Partner == null) {
                                throw new SQLException("Ha ocurrido un error en la insercion de los datos.");
                            }

                            stmt = con.prepareStatement(INSERTAR_USUARIO_COMPAÑIA);

                            stmt.setString(1, id_Partner);

                            stmt.executeUpdate();
                        }
                    }
                }

            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (NotOperativeDataBaseException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectException("Conexion a la base de datos no operativa.");
        } catch (ServerConnectionException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectException("Error de conexión al servidor.");
        } finally {
            pool.closePool(con);
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return user;
    }

    /**
     * Este método inserta en la base de datos al usuario que se registró.
     *
     * @param user
     * @return usuario
     * @throws ServerErrorException
     * @throws UserAlreadyExitsException
     */
    @Override
    public User executeSignIn(User user) throws UserNotFoundException, ConnectException, IncorrectCredentialsException {
        User usuario = null;
        try {
            con = pool.createConnection();

            try {
                stmt = con.prepareStatement(BUSCAR_USUARIO);

                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getContraseña());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    usuario = new User();
                    usuario.setEmail(rs.getString("login"));
                    usuario.setContraseña(rs.getString("password"));
                } else {
                    throw new IncorrectCredentialsException("Correo o contraseña incorrectos");
                }
                if ((usuario == null)) {
                    throw new UserNotFoundException("El usuario no existe");

                }

            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (NotOperativeDataBaseException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectException("Conexion a la base de datos no operativa.");
        } catch (ServerConnectionException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectException("Error de conexión con el servidor.");
        } finally {
            pool.closePool(con);
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return usuario;
    }

    /**
     * Metodo para saber si ese email ya esta introducido en la base de datos
     *
     * @param email
     * @return existe
     * @throws ConnectException
     */
    private boolean usuarioYaExiste(String email) throws ConnectException {
        boolean existe = false;
        try {
            con = pool.createConnection();

            try {
                stmt = con.prepareStatement(USUARIO_EXISTE);

                stmt.setString(1, email);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Establecer existe en true si hay al menos un registro
                    existe = true;
                }
            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NotOperativeDataBaseException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectException("Conexion a la base de datos no operativa.");
        } catch (ServerConnectionException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectException("Error de conexión con el servidor.");
        } finally {
            pool.closePool(con);
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return existe;
    }

}
