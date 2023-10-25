/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase es el server socket.
 *
 * @author Adrian
 */
public class SignerServer {

    private static final ResourceBundle RETO1 = ResourceBundle.getBundle("damc.grupo3.reto1.model.Config");
    private static final int MAX_USERS = Integer.parseInt(RETO1.getString("MaxUsers"));
    private static final int PORT = Integer.parseInt(RETO1.getString("PORT"));

    private static final Logger LOGGER = Logger.getLogger(SignerServer.class.getName());

    private static boolean serverOn = true;
    private ServerSocket svSocket;
    private Message mensaje = null;
    private Socket skCliente;
    private static Integer i = 0;
    private ServerThread st;

    /**
     * Metodo para iniciar el servidor.
     */
    public SignerServer() {
        try {
            svSocket = new ServerSocket(PORT);

            while (serverOn) {
                //Saber si se ha sobrepasado el limite de usuarios conectados a la vez
                if (i < MAX_USERS) {
                    skCliente = svSocket.accept();

                    //Crear hilo pasándole el Socket skCliente
                    st = new ServerThread(skCliente);
                    st.start();
                    añadirCliente(st);
                } else {
                    ObjectOutputStream oos = new ObjectOutputStream(skCliente.getOutputStream());
                    mensaje.setMsg(MessageType.MAX_THREAD_USER);
                    oos.writeObject(mensaje);
                }
            }

            //Cerrar servidor
            svSocket.close();
        } catch (IOException e) {
            Logger.getLogger(SignerServer.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Metodo para añadir una conexion
     *
     * @param signerT
     */
    public static synchronized void añadirCliente(ServerThread signerT) {
        i++;
    }

    /**
     * Metodo para borrar una conexion
     *
     * @param signerT
     */
    public static synchronized void borrarCliente(ServerThread signerT) {
        i--;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new SignerServer();
    }

}
