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
import excepciones.UserAlreadyExistsException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataSource;

/**
 * Esta clase se utiliza para gestionar la base de datos y sus casos de lógica.
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
    final String INSERTAR_USUARIO = "insert into res_users (login, password, company_id, partner_id,notification_type) values (?,?,?,?,'email')";
    final String INSERTAR_DATOS_USUARIO = "insert into res_partner (company_id, create_date, name, zip, city, phone, active, email) values (?,?,?,?,?,?,?,?)";
    final String INSERTAR_USUARIO_GRUPO = "insert into res_groups_users_rel (gid, uid) values (16,?), (26,?), (28,?), (31,?)";
    final String ID_USUARIO = "select MAX(id) as id from res_users";
    final String INSERTAR_USUARIO_COMPAÑIA = "insert into res_company_users_rel (cid, user_id) values (1,?)";
    final String ID_PARTNER = "select MAX(id) as id from res_partner";
    final String BUSCAR_USUARIO = "select login, password from res_users where login = ? and password = ?";
    final String NOMBRE_USUARIO = "select name from res_partner where email=?";
    final String USUARIO_EXISTE = "select login from res_users where login=?";

    //Constructor para conectarse a la base de datos
    public DAOImplementacion() {
        this.config = ResourceBundle.getBundle("Utilidades.Config");
        this.url = config.getString("CONEXION");
        this.user = config.getString("BDUSER");
        this.pswd = config.getString("BDPASS");
        this.pool = pool.getPool();
    }

    /**
     * Metodo para insertar usuarios a la base de datos.
     *
     * @param user
     * @return user
     * @throws UserAlreadyExistsException
     */
    @Override
    public User excecuteLogin(User user) throws ConnectException, UserAlreadyExistsException {
        LOG.info("Relaizando la transaccion de Registro");
        //Llamamos al pool para recuperar una conexión
        con = pool.getConnection();

        //Hacemos una comprobación para ver si el usuario ya existe en la base de datos o no
        if (usuarioYaExiste(user.getEmail())) {
            throw new UserAlreadyExistsException("El usuario ya existe.");
        } else {
            try {
                //Insertamos los datos del usuario en la tabla res.partners
                stmt = con.prepareStatement(INSERTAR_DATOS_USUARIO);

                stmt.setInt(1, user.getCompañia());
                stmt.setDate(2, Date.valueOf(user.getFecha_ini()));
                stmt.setString(3, user.getNombre());
                stmt.setInt(4, user.getZip_code());
                stmt.setString(5, user.getDireccion());
                stmt.setInt(6, user.getTelefono());
                stmt.setBoolean(7, user.isActivo());
                stmt.setString(8, user.getEmail());
                //Si lo inserta bien pasa a la siguiente sql
                if (stmt.executeUpdate() == 1) {
                    //Para realizar la siguiente insercción necesitamos recoger un parámetro
                    int partnerId = recogerPartnerId();
                    //Una vez recuperado ese parámetro insertamos los usuarios en la tabla res_users
                    stmt = con.prepareStatement(INSERTAR_USUARIO);

                    stmt.setString(1, user.getEmail());
                    stmt.setString(2, user.getContraseña());
                    stmt.setInt(3, user.getCompañia());
                    stmt.setInt(4, partnerId);
                    //Si lo inserta bien pasa a la siguiente sql
                    if (stmt.executeUpdate() == 1) {
                        //Para realizar la siguiente insercción necesitamos recoger un parámetro
                        int id_Usuario = recogerUsuarioId();
                        //Una vez recuperado ese parámetro insertamos los usuarios en la tabla res_groups_users_rel
                        stmt = con.prepareStatement(INSERTAR_USUARIO_GRUPO);

                        stmt.setInt(1, id_Usuario);
                        stmt.setInt(2, id_Usuario);
                        stmt.setInt(3, id_Usuario);
                        stmt.setInt(4, id_Usuario);
                        //Si lo inserta bien pasa a la siguiente sql
                        if (stmt.executeUpdate() == 4) {
                            //Insertamos los datos del usuario en la tabla res_company_users_rel
                            stmt = con.prepareStatement(INSERTAR_USUARIO_COMPAÑIA);

                            stmt.setInt(1, id_Usuario);

                            stmt.executeUpdate();
                            //Mostramos errores si ha ocurrido algo mal
                        } else {
                            throw new SQLException("Ha ocurrido un error en la insercion de datos.");
                        }
                    } else {
                        throw new SQLException("Ha ocurrido un error en la insercion de datos.");
                    }
                } else {
                    throw new SQLException("Ha ocurrido un error en la insercion de datos.");
                }

            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Cerramos las conexiónes
        pool.closePool(con);
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Devolvemos un objeto user
        return user;
    }

    /**
     * Este método inserta en la base de datos al usuario que se registró.
     *
     * @param user
     * @return usuario
     */
    @Override
    public User executeSignIn(User user) throws ConnectException, IncorrectCredentialsException {
        LOG.info("Relaizando la transaccion de Inicio de sesión");
        //Instanciamos un nuevo User
        User usuario = new User();
        //Llamamos al pool para recuperar una conexión
        con = pool.getConnection();
        try {
            //Hacemos una comprobación para ver si el usuario ya existe en la base de datos o no
            if (!usuarioYaExiste(user.getEmail())) {
                throw new IncorrectCredentialsException("Correo o contraseña incorrectos");
            } else {
                //Buscamos que el usuario introducido existe en nuestra base de datos
                stmt = con.prepareStatement(BUSCAR_USUARIO);

                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getContraseña());
                //Si existe devuelve los datos especificados
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    usuario.setEmail(rs.getString("login"));
                    usuario.setContraseña(rs.getString("password"));
                    //Esta consulta especifica devuelve el nombre del usuario para mostrarlo por ventana
                    stmt = con.prepareStatement(NOMBRE_USUARIO);
                    stmt.setString(1, user.getEmail());
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        usuario.setNombre(rs.getString("name"));
                    }
                    //Si no existe ese usuario lanza una excepcion
                } else if (usuario.getNombre() == null) {
                    throw new IncorrectCredentialsException("Correo o contraseña incorrectos");

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Cerramos las conexiónes
        pool.closePool(con);
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Devuelve un usuario existente
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
        //El método devuelve un objeto boolean para comprobar si el usuario existe o no
        boolean existe = false;
        try {
            //Devuelve una conexión con el pool
            con = pool.createConnection();
            //Preparamos la sentencia para hacer la comprobación
            stmt = con.prepareStatement(USUARIO_EXISTE);

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            //Si existe un usuario la variable booleana se pondra con valor true
            if (rs.next()) {
                // Establecer existe en true si hay al menos un registro
                existe = true;
            }
            //Control de excepciones
        } catch (SQLException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotOperativeDataBaseException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectException("Conexion a la base de datos no operativa.");
        } catch (ServerConnectionException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectException("Error de conexión con el servidor.");
        } finally {
            //Cerramos las conexiones
            pool.closePool(con);
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //Devuelve el valor de la variable existe
        return existe;
    }

    /**
     * Metodo que recoge el id del partner de la base de datos de odoo,
     * necesaria para realizar insercciones en distintos campos
     *
     * @return partnerId
     */
    private int recogerPartnerId() {
        int partnerId = 0;
        //Recogemos la conexión del pool
        con = pool.getConnection();
        try {
            //Preparamos la consulta de sql
            stmt = con.prepareStatement(ID_PARTNER);
            //Si sale bien nos devuelve  el id del partner
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                partnerId = rs.getInt("id");
                //Devuelve un error si no existe un id para ese usuario
            } else if (partnerId == 0) {
                throw new SQLException("Ha ocurrido un error en la insercion de los datos.");
            }
            //Control de errores
        } catch (SQLException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Cerramos las conexiones
        pool.closePool(con);
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Devuelve el id del partner
        return partnerId;
    }

    /**
     * Metodo que recoge el id del usuario de la base de datos de odoo,
     * necesaria para realizar insercciones en distintos campos
     *
     * @return id_Usuario
     */
    private int recogerUsuarioId() {
        int id_Usuario = 0;
        //Recogemos la conexión del pool
        con = pool.getConnection();
        try {
            //Preparamos la consulta de sql
            stmt = con.prepareStatement(ID_USUARIO);
            //Si sale bien nos devuelve  el id del usuario
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                id_Usuario = rs.getInt("id");
                //Devuelve un error si no existe un id para ese usuario
            } else if (id_Usuario == 0) {
                throw new SQLException("Ha ocurrido un error en la insercion de los datos.");
            }
            //Control de errores
        } catch (SQLException ex) {
            Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Cerramos las conexiones
        pool.closePool(con);
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Devuelve el id del usuario
        return id_Usuario;
    }

}
