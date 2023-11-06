/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import excepciones.ConnectException;
import excepciones.IncorrectCredentialsException;
import excepciones.UserAlreadyExistsException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase es el hilo del servidor, realiza las operaciones necesarias para
 * garantizar el funcionamiento del servidor, escucha al cliente y lee sus
 * peticiones, envia respúestas al cliente
 *
 * @author Diego
 */
public class ServerThread extends Thread {

    //Llamada de los distintos objetos
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;
    private Socket sk = null;
    private Sign sign;
    private MessageType messT;
    private Message msg = null;
    private User user;
    private static final Logger LOGGER = Logger.getLogger(ServerThread.class.getName());

    //Constructor vacio
    public ServerThread() {
    }

    //Constructor con el socket
    public ServerThread(Socket sk) {
        this.sk = sk;

    }

    /**
     * Método run del socket, depende de lo que reciva del cliente realiza
     * diferentes operaciones, como registrar ususarios, iniciar sesiones,
     * enviar respuestas correctas y erroneas etc...
     *
     * @author Diego
     */
    @Override
    public void run() {

        try {

            ois = new ObjectInputStream(sk.getInputStream());

            //LLamaos al método de la factoria por cada registro que hace el cliente
            //Métodos comentados por falta de la clase DaoFactory
            DAOFactory daofact = new DAOFactory();
            sign = daofact.getDao();

            //Leemos los datos del encapsulador
            msg = (Message) ois.readObject();

            //Dependiendo de que respuesta lee este hará varias cosas con una estructura switch-case
            switch (msg.getMsg()) {

                //Este caso se ejecutará si el cliente hace un Sign In
                case SIGNIN_REQUEST:
                    LOGGER.info("Realizando Registro");
                    //Llama al método de la interfaz para hacer el signIn
                    user = sign.executeSignIn(msg.getUser());
                    //Setea el usuario al mensaje
                    msg.setUser(user);
                    //Comprueba si el user tiene datos
                    if (user == null) {
                        //Indica en el mensaje un ok si tiene datos
                        msg.setMsg(MessageType.ERROR_RESPONSE);
                    } else {
                        //Indica un error si no hay datos user
                        msg.setMsg(MessageType.OK_RESPONSE);
                    }
                    break;
                //Este caso se ejecutará si el cliente hace un Sign uP
                case SIGNUP_REQUEST:
                    LOGGER.info("Realizando Inicio de sesión");
                    //Llama al método de la interfaz para hacer el SignUp
                    user = sign.excecuteLogin(msg.getUser());
                    //Setea el usuario al mensaje
                    msg.setUser(user);
                    //Comprueba si el user tiene datos
                    if (user == null) {
                        //Indica en el mensaje un ok si tiene datos
                        msg.setMsg(MessageType.ERROR_RESPONSE);
                    } else {
                        //Indica un error si no hay datos user
                        msg.setMsg(MessageType.OK_RESPONSE);
                    }
                    break;
            }
            //Distintos controles de errores que se pueden interceptar
        } catch (UserAlreadyExistsException e) {
            msg.setMsg(MessageType.USER_ALREADY_EXISTS_RESPONSE);
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (IncorrectCredentialsException e) {
            msg.setMsg(MessageType.INCORRECT_CREDENTIALS_RESPONSE);
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (ConnectException e) {
            msg.setMsg(MessageType.CONNECTION_ERROR_RESPONSE);
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (ClassNotFoundException e) {
            msg.setMsg(MessageType.ERROR_RESPONSE);
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            msg.setMsg(MessageType.ERROR_RESPONSE);
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                LOGGER.info("Cerrando conexiónes");
                //Cerramos los distintos imputs y outputs más el propio socket
                oos = new ObjectOutputStream(sk.getOutputStream());
                oos.writeObject(msg);
                //Llamamos a esta funcion del main para borrar el cliente una vez que cierre su conexión
                SignerServer.borrarCliente(this);
                ois.close();
                oos.close();
                sk.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
