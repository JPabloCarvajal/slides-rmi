package classes;

import java.awt.*;
import javax.swing.*;

// El mando: cuatro botones que invocan metodos remotos en el servidor.
// No pinta diapositivas, solo envia ordenes y muestra en cual quedo.
public class Control extends JFrame {

    private final iRMI servicio;
    private final String token;
    private final JLabel estado = new JLabel("...", SwingConstants.CENTER);
    private boolean completa = false;

    public Control(iRMI servicio, String token, String nombre) {
        super("Control - " + nombre);
        this.servicio = servicio;
        this.token = token;

        JButton bAtras = new JButton("<");
        JButton bSiguiente = new JButton(">");
        JButton bIr = new JButton("Ir a...");
        JButton bCompleta = new JButton("Pantalla completa");

        bAtras.addActionListener(e -> enviar(() -> servicio.atras(token)));
        bSiguiente.addActionListener(e -> enviar(() -> servicio.siguiente(token)));
        bIr.addActionListener(e -> {
            String txt = JOptionPane.showInputDialog(this, "Numero de diapositiva:");
            if (txt == null) return;
            try {
                int n = Integer.parseInt(txt.trim());
                enviar(() -> servicio.irA(token, n));
            } catch (NumberFormatException ex) {
                estado.setText("numero invalido");
            }
        });
        bCompleta.addActionListener(e -> {
            completa = !completa;
            enviar(() -> servicio.pantallaCompleta(token, completa));
        });

        JPanel botones = new JPanel(new GridLayout(2, 2, 6, 6));
        botones.add(bAtras);
        botones.add(bSiguiente);
        botones.add(bIr);
        botones.add(bCompleta);

        setLayout(new BorderLayout(6, 6));
        add(botones, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(280, 160);
        setLocationRelativeTo(null);

        enviar(() -> servicio.siguiente(token) == -1 ? -1 : servicio.irA(token, 1));
    }

    // Envia la orden en otro hilo para no congelar la ventana,
    // y actualiza la etiqueta con la diapositiva que devuelve el servidor.
    private void enviar(Llamada llamada) {
        new Thread(() -> {
            String texto;
            try {
                int actual = llamada.ejecutar();
                texto = actual == -1 ? "sin permiso" : "diapositiva " + actual + " / " + servicio.total(token);
            } catch (Exception ex) {
                texto = "sin conexion";
            }
            String t = texto;
            SwingUtilities.invokeLater(() -> estado.setText(t));
        }).start();
    }

    // Una llamada remota cualquiera que devuelve un int.
    private interface Llamada {
        int ejecutar() throws Exception;
    }
}