package classes;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import javax.swing.SwingUtilities;

public class Main {

    private static final int PUERTO = 1082;
    private static final String NOMBRE = "control";

    public static void main(String[] args) throws Exception {
        String ruta = args.length > 0 ? args[0] : "presentacion.pdf";

        // El cliente necesita la IP real del servidor, no 127.0.0.1
        String ip = java.net.InetAddress.getLocalHost().getHostAddress();
        System.setProperty("java.rmi.server.hostname", ip);

        Diapositivas diapositivas = new Diapositivas(ruta);
        Visor visor = new Visor(diapositivas);
        SwingUtilities.invokeAndWait(() -> visor.setVisible(true));

        iRMI servicio = new ImpRMI(visor);
        LocateRegistry.createRegistry(PUERTO);
        Naming.rebind("//" + ip + ":" + PUERTO + "/" + NOMBRE, servicio);

        System.out.println("Servidor listo en  rmi://" + ip + ":" + PUERTO + "/" + NOMBRE);
        System.out.println("Diapositivas: " + diapositivas.total());
        visor.log("servidor listo, esperando controles");
    }
}