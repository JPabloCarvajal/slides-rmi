package classes;

import java.awt.*;
import javax.swing.*;

// El mando: se conecta bajo demanda y luego invoca metodos remotos.
public class Control extends JFrame {

    private final iRMI servicio;
    private final String nombre;
    private volatile String token = null;

    private final JButton bConectar = new JButton("Conectar");
    private final JPanel botones = new JPanel(new GridLayout(2, 2, 6, 6));
    private final JLabel estado = new JLabel("sin conectar", SwingConstants.CENTER);
    private boolean completa = false;

    public Control(iRMI servicio, String nombre) {
        super("Control - " + nombre);
        this.servicio = servicio;
        this.nombre = nombre;

        JButton bAtras = new JButton("<");
        JButton bSiguiente = new JButton(">");
        JButton bIr = new JButton("Ir a...");
        JButton bCompleta = new JButton("Pantalla completa");

        bConectar.addActionListener(e -> conectar());
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

        botones.add(bAtras);
        botones.add(bSiguiente);
        botones.add(bIr);
        botones.add(bCompleta);
        habilitar(false);

        setLayout(new BorderLayout(6, 6));
        add(bConectar, BorderLayout.NORTH);
        add(botones, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(280, 200);
        setLocationRelativeTo(null);
    }

    // Pide permiso al servidor. Bloquea hasta que el operador responda,
    // por eso corre en otro hilo.
    private void conectar() {
        bConectar.setEnabled(false);
        estado.setText("esperando permiso...");

        new Thread(() -> {
            String t = null;
            String texto;
            try {
                t = servicio.conectar(nombre);
                texto = t == null ? "conexion rechazada"
                                  : "conectado - " + servicio.total(t) + " diapositivas";
            } catch (Exception ex) {
                texto = "sin conexion";
            }
            String tk = t;
            String msg = texto;
            SwingUtilities.invokeLater(() -> {
                token = tk;
                habilitar(tk != null);
                bConectar.setEnabled(tk == null);
                estado.setText(msg);
            });
        }).start();
    }

    private void enviar(Llamada llamada) {
        if (token == null) {
            estado.setText("conecta primero");
            return;
        }
        new Thread(() -> {
            String texto;
            try {
                int actual = llamada.ejecutar();
                texto = actual == -1 ? "sin permiso"
                                     : "diapositiva " + actual + " / " + servicio.total(token);
            } catch (Exception ex) {
                texto = "sin conexion";
            }
            String t = texto;
            SwingUtilities.invokeLater(() -> estado.setText(t));
        }).start();
    }

    private void habilitar(boolean on) {
        for (Component c : botones.getComponents()) c.setEnabled(on);
    }

    private interface Llamada {
        int ejecutar() throws Exception;
    }
}