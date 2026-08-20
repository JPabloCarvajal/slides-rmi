package classes;

import java.rmi.Naming;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

// Arranque del mando: solo busca el objeto remoto y abre la ventana.
// El permiso se pide desde el boton Conectar.
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

            SwingUtilities.invokeLater(() -> new Control(servicio, n).setVisible(true));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se pudo conectar:\n\n" + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}