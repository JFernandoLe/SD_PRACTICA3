import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private static final int MAX_CLIENTES = 3;  // Máximo de clientes permitidos
    private static int clientesConectados = 0;  // Contador de clientes activos
    private static final Object lock = new Object();  // Para sincronización
    private static CuentaBancaria cuenta = new CuentaBancaria(1000);  // Saldo inicial

    public static void main(String[] args) {
        int puerto = 1234;

        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado en el puerto " + puerto);

            while (true) {
                synchronized (lock) {
                    if (clientesConectados < MAX_CLIENTES) {
                        clientesConectados++;
                    } else {
                        System.out.println("Máximo de clientes alcanzado, rechazando conexión...");
                        continue;  // No aceptar más clientes si ya hay 3 activos
                    }
                }

                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado desde " + cliente.getInetAddress() + ":" + cliente.getPort());

                new Thread(new ManejadorCliente(cliente)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clase que maneja la conexión de un cliente
    static class ManejadorCliente implements Runnable {
        private Socket cliente;

        public ManejadorCliente(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                 PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true)) {

                String nombre = br.readLine();
                int cantidad = Integer.parseInt(br.readLine());

                synchronized (cuenta) {  // Bloque sincronizado para evitar accesos simultáneos al saldo
                    if (cuenta.retirar(cantidad)) {
                        System.out.println("Cliente [" + nombre + "] retiró: " + cantidad + " pesos. Saldo restante: " + cuenta.getSaldo());
                        pw.println("Hola " + nombre + ", retiro exitoso de " + cantidad + " pesos. Saldo restante: " + cuenta.getSaldo());
                    } else {
                        System.out.println("Cliente [" + nombre + "] intentó retirar " + cantidad + " pesos. Saldo insuficiente.");
                        pw.println("Saldo insuficiente. Solo quedan " + cuenta.getSaldo() + " pesos.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
            } finally {
                try {
                    cliente.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    clientesConectados--;  // Reducir el contador cuando un cliente termina su sesión
                }
            }
        }
    }
}

// Clase para manejar el saldo de la cuenta bancaria
class CuentaBancaria {
    private int saldo;

    public CuentaBancaria(int saldoInicial) {
        this.saldo = saldoInicial;
    }

    public synchronized boolean retirar(int cantidad) {
        if (cantidad > saldo) {
            return false;  // Saldo insuficiente
        }
        saldo -= cantidad;
        return true;
    }

    public synchronized int getSaldo() {
        return saldo;
    }
}

