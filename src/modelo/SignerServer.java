/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase es la clase principal del Servidor, arranca el servidor e inicia
 * el hilo para realizar las peticiones del cliente
 *
 * @author Adrían
 */
public class SignerServer {

    private static final ResourceBundle archivo = ResourceBundle.getBundle("Utilidades.Config");
    private static final int MAX_USERS = Integer.parseInt(archivo.getString("MAX_USERS"));
    private static final int PORT = Integer.parseInt(archivo.getString("PORT"));

    private static final Logger LOGGER = Logger.getLogger(SignerServer.class.getName());

    private static boolean serverOn = true;
    private ServerSocket svSocket;
    private Message mensaje;
    private Socket skCliente;
    private static Integer i = 0;
    private ServerThread st;

    /**
     * Metodo para iniciar y cerrar el servidor.
     */
    public SignerServer() {
        // Esta llamada arranca el hilo del servidor
        this.arrancarHilo();
    }

    /**
     * Metodo que arranca el hilo worker del servidor, se encarga de gestionar
     * la capacidad máxima de usuarios permitidos en la aplicación.
     */
    private void arrancarHilo() {
        try {
            LOGGER.info("Servidor en marcha");
            // Instanciamos el serversocket
            svSocket = new ServerSocket(PORT);

            // Iniciamos un hilo para escuchar la entrada del teclado y cerrar el servidor al presionar 'q'
            Thread tecladoThread = new Thread(() -> {
                LOGGER.info("Presiona 'q' para cerrar el servidor.");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                try {
                    while (true) {
                        String input = reader.readLine();
                        if (input != null && input.equalsIgnoreCase("q")) {
                            System.exit(0);
                            break;
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error al leer la entrada del teclado", e);
                }
            });

            tecladoThread.start();

            while (serverOn) {
                LOGGER.info("Escuchando");
                // Saber si se ha sobrepasado el límite de usuarios conectados a la vez
                if (i < MAX_USERS) {
                    // Acepta la petición del socket cliente
                    skCliente = svSocket.accept();

                    // Creamos el hilo pasándole el Socket skCliente
                    st = new ServerThread(skCliente);
                    st.start();
                    // Cada hilo que se crea se llama la función añadir cliente, que añade un contador para hacer
                    // la gestión de clientes
                    añadirCliente(st);
                } else {
                    // Si ocurre una caravana de usuarios se lanzará una excepción
                    ObjectOutputStream oos = new ObjectOutputStream(skCliente.getOutputStream());
                    mensaje = new Message();
                    mensaje.setMsg(MessageType.MAX_THREAD_USER);
                    oos.writeObject(mensaje);
                }
            }

            // Cerrar servidor
            svSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cerrar el servidor", e);
        }
    }

    /**
     * Metodo para añadir una conexión.
     *
     * @param signerT
     */
    public static synchronized void añadirCliente(ServerThread signerT) {
        i++;
    }

    /**
     * Metodo para borrar una conexión.
     *
     * @param signerT
     */
    public static synchronized void borrarCliente(ServerThread signerT) {
        i--;
    }

    public static void main(String[] args) {
        new SignerServer();
    }
}
