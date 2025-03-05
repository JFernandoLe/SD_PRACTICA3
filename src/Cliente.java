import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente {
    public static void main(String[] args) {
        String direccion = "127.0.0.1";
        int puerto = 1234;

        try (Socket cl = new Socket(direccion, puerto);
             BufferedReader brTeclado = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter pw = new PrintWriter(cl.getOutputStream(), true);
             BufferedReader brServidor = new BufferedReader(new InputStreamReader(cl.getInputStream()))) {

            System.out.println("Conexión establecida con el servidor...");

            System.out.println("Escribe tu nombre y presiona la tecla <enter>");
            String nombre = brTeclado.readLine();

            System.out.println("Coloca la cantidad de dinero a retirar");
            int cantidad = Integer.parseInt(brTeclado.readLine());

            pw.println(nombre);
            pw.println(cantidad);

            System.out.println("Recibiendo mensaje desde el servidor: ");
            String respuesta = brServidor.readLine();
            System.out.println(respuesta);

        } catch (Exception e) {
            System.err.println("Error en la conexión con el servidor: " + e.getMessage());
        }
    }
}

