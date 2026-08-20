package classes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.swing.JOptionPane;

public class ImpRMI extends UnicastRemoteObject implements iRMI {

    private static final long serialVersionUID = 1L;
    private static final long VENTANA_MS = 500;

    private final Visor visor;
    private final Map<String, String> tokens = new HashMap<>();
    private long ultimaAccion = 0;

    public ImpRMI(Visor visor) throws RemoteException {
        this.visor = visor;
    }

    @Override
    public synchronized String conectar(String nombre) throws RemoteException {
        int r = JOptionPane.showConfirmDialog(visor,
                "El control \"" + nombre + "\" quiere conectarse.\n\u00bfLe das permiso?",
                "Solicitud de conexion", JOptionPane.YES_NO_OPTION);

        if (r != JOptionPane.YES_OPTION) {
            visor.log(nombre + " intento conectarse (rechazado)");
            return null;
        }
        String token = UUID.randomUUID().toString();
        tokens.put(token, nombre);
        visor.log(nombre + " se conecto");
        return token;
    }

    @Override
    public synchronized int siguiente(String token) throws RemoteException {
        String quien = autor(token);
        if (quien == null) return -1;
        if (!aceptaAccion()) return visor.actual();

        visor.siguiente();
        visor.log(quien + " avanzo a la diapositiva " + visor.actual());
        return visor.actual();
    }

    @Override
    public synchronized int atras(String token) throws RemoteException {
        String quien = autor(token);
        if (quien == null) return -1;
        if (!aceptaAccion()) return visor.actual();

        visor.atras();
        visor.log(quien + " retrocedio a la diapositiva " + visor.actual());
        return visor.actual();
    }

    @Override
    public synchronized int irA(String token, int n) throws RemoteException {
        String quien = autor(token);
        if (quien == null) return -1;
        if (!aceptaAccion()) return visor.actual();

        visor.irA(n);
        visor.log(quien + " salto a la diapositiva " + visor.actual());
        return visor.actual();
    }

    @Override
    public synchronized int pantallaCompleta(String token, boolean on) throws RemoteException {
        String quien = autor(token);
        if (quien == null) return -1;
        if (!aceptaAccion()) return visor.actual();

        visor.pantallaCompleta(on);
        visor.log(quien + (on ? " activo" : " desactivo") + " pantalla completa");
        return visor.actual();
    }

    @Override
    public synchronized int total(String token) throws RemoteException {
        if (autor(token) == null) return -1;
        return visor.total();
    }

    // ---------- privados ----------

    // Quien es el duenno del token, o null si no tiene permiso.
    private String autor(String token) {
        return token == null ? null : tokens.get(token);
    }

    // Deja pasar una sola accion por ventana. Las que lleguen dentro se descartan.
    private boolean aceptaAccion() {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimaAccion < VENTANA_MS) return false;
        ultimaAccion = ahora;
        return true;
    }
}