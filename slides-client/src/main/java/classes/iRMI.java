package classes;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Contrato del control remoto de diapositivas.
public interface iRMI extends Remote {

    // Pide permiso al servidor. El operador aprueba o rechaza.
    // Devuelve el token que el cliente reenvia en las demas llamadas, o null si lo rechazan.
    String conectar(String nombre) throws RemoteException;

    // Avanza una diapositiva.
    // Devuelve la diapositiva actual (base 1), o -1 si el token no vale.
    int siguiente(String token) throws RemoteException;

    // Retrocede una diapositiva.
    // Devuelve la diapositiva actual (base 1), o -1 si el token no vale.
    int atras(String token) throws RemoteException;

    // Salta a la diapositiva n (base 1). Si n esta fuera de rango, no hace nada.
    // Devuelve la diapositiva actual, o -1 si el token no vale.
    int irA(String token, int n) throws RemoteException;

    // Activa (true) o desactiva (false) la pantalla completa en el servidor.
    // Devuelve la diapositiva actual, o -1 si el token no vale.
    int pantallaCompleta(String token, boolean on) throws RemoteException;

    // Cuantas diapositivas tiene la presentacion, para mostrar "3/12" en el mando.
    // Devuelve el total, o -1 si el token no vale.
    int total(String token) throws RemoteException;
}