package classes;

import java.rmi.Naming;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

// Arranque del mando: busca el objeto remoto, pide permiso y abre la ventana.
public class Main {

    public static void main(String[] args) throws Exception {
        String host = "192.168.80.31";
        String nombre = "guayaba";

        iRMI servicio = (iRMI) Naming.lookup("rmi://" + host + ":1082/control");
        System.out.println("Conectado a " + host + ", pidiendo permiso...");

        String token = servicio.conectar(nombre);
        if (token == null) {
            JOptionPane.showMessageDialog(null, "El servidor rechazo la conexion.");
            return;
        }
        System.out.println("Permiso concedido. Diapositivas: " + servicio.total(token));

        SwingUtilities.invokeLater(() -> new Control(servicio, token, nombre).setVisible(true));
    }
}