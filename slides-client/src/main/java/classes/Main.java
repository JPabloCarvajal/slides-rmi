package classes;

import java.rmi.Naming;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    private static final int PUERTO = 1082;

    public static void main(String[] args) {
        try {
            String host = args.length > 0 ? args[0]
                    : JOptionPane.showInputDialog(null, "IP del servidor:", "192.168.1.9");
            if (host == null || host.isBlank()) return;

            String nombre = args.length > 1 ? args[1]
                    : JOptionPane.showInputDialog(null, "Nombre de este control:", "guayaba");
            if (nombre == null || nombre.isBlank()) return;

            String n = nombre.trim();
            iRMI servicio = (iRMI) Naming.lookup("rmi://" + host.trim() + ":" + PUERTO + "/control");

            String token = servicio.conectar(n);
            if (token == null) {
                JOptionPane.showMessageDialog(null, "El servidor rechazo la conexion.");
                return;
            }

            SwingUtilities.invokeLater(() -> new Control(servicio, token, n).setVisible(true));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se pudo conectar:\n\n" + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}