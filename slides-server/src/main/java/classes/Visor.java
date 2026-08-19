package classes;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

// Ventana de proyeccion: la diapositiva ocupa toda la ventana.
// Encima flotan el aviso de eventos (abajo izquierda) y los controles
// locales del presentador (abajo derecha).
// Es el unico que sabe cual diapositiva se esta mostrando.
public class Visor extends JFrame {

    private static final int AVISO_MS = 3000;

    private final Diapositivas diapositivas;
    private int actual = 1;
    private boolean completa = false;

    private final JLabel lienzo = new JLabel("", SwingConstants.CENTER);
    private final JLabel aviso = new JLabel();
    private final JPanel controles;
    private final JLabel contador = new JLabel();
    private final Timer temporizador;

    public Visor(Diapositivas diapositivas) {
        super("Presentacion");
        this.diapositivas = diapositivas;
        this.controles = crearControles();

        temporizador = new Timer(AVISO_MS, e -> aviso.setVisible(false));
        temporizador.setRepeats(false);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(crearLienzoConCapas());
        setSize(1100, 700);
        setLocationRelativeTo(null);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) { recolocar(); }
        });
    }

    // ---------- acciones (las usan los botones locales y, luego, ImpRMI) ----------

    public synchronized void siguiente() {
        if (actual < diapositivas.total()) actual++;
        pintar();
    }

    public synchronized void atras() {
        if (actual > 1) actual--;
        pintar();
    }

    public synchronized void irA(int n) {
        if (n >= 1 && n <= diapositivas.total()) actual = n;
        pintar();
    }

    public synchronized void pantallaCompleta(boolean on) {
        if (on == completa) return;
        completa = on;
        GraphicsDevice pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        dispose();
        setUndecorated(on);
        pantalla.setFullScreenWindow(on ? this : null);
        setVisible(true);
        recolocar();
    }

    public synchronized int actual() {
        return actual;
    }

    public int total() {
        return diapositivas.total();
    }

    // Muestra un aviso flotante. Si llega otro antes, lo reemplaza.
    public void log(String texto) {
        SwingUtilities.invokeLater(() -> {
            aviso.setText(" " + texto + " ");
            aviso.setVisible(true);
            recolocar();
            temporizador.restart();
        });
    }

    // ---------- privados ----------

    private JLayeredPane crearLienzoConCapas() {
        JLayeredPane capas = new JLayeredPane();
        lienzo.setOpaque(true);
        lienzo.setBackground(Color.BLACK);

        aviso.setOpaque(true);
        aviso.setBackground(new Color(0, 0, 0, 190));
        aviso.setForeground(Color.WHITE);
        aviso.setFont(aviso.getFont().deriveFont(14f));
        aviso.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        aviso.setVisible(false);

        capas.add(lienzo, JLayeredPane.DEFAULT_LAYER);
        capas.add(controles, JLayeredPane.PALETTE_LAYER);
        capas.add(aviso, JLayeredPane.PALETTE_LAYER);
        return capas;
    }

    // Reajusta las tres capas al tamano actual de la ventana.
    private void recolocar() {
        Container capas = getContentPane();
        int w = capas.getWidth();
        int h = capas.getHeight();
        if (w <= 0 || h <= 0) return;

        lienzo.setBounds(0, 0, w, h);

        Dimension dc = controles.getPreferredSize();
        controles.setBounds(w - dc.width - 20, h - dc.height - 20, dc.width, dc.height);

        Dimension da = aviso.getPreferredSize();
        aviso.setBounds(20, h - da.height - 20, da.width, da.height);

        pintar();
    }

    // Dibuja la diapositiva actual escalada, manteniendo la proporcion.
    private void pintar() {
        BufferedImage img = diapositivas.imagen(actual);
        if (img == null) return;
        int w = Math.max(lienzo.getWidth(), 100);
        int h = Math.max(lienzo.getHeight(), 100);
        double escala = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
        Image escalada = img.getScaledInstance((int) (img.getWidth() * escala),
                                               (int) (img.getHeight() * escala),
                                               Image.SCALE_SMOOTH);
        lienzo.setIcon(new ImageIcon(escalada));
        contador.setText(actual + " / " + diapositivas.total() + "  ");
    }

    private JPanel crearControles() {
        JButton bAtras = new JButton("<");
        JButton bSiguiente = new JButton(">");
        JButton bIr = new JButton("Ir a...");
        JButton bCompleta = new JButton("Pantalla completa");

        bAtras.addActionListener(e -> { atras(); log("local retrocedio a la diapositiva " + actual()); });
        bSiguiente.addActionListener(e -> { siguiente(); log("local avanzo a la diapositiva " + actual()); });
        bIr.addActionListener(e -> {
            String txt = JOptionPane.showInputDialog(this, "Numero de diapositiva:");
            if (txt == null) return;
            try {
                irA(Integer.parseInt(txt.trim()));
                log("local salto a la diapositiva " + actual());
            } catch (NumberFormatException ex) {
                log("local escribio un numero invalido");
            }
        });
        bCompleta.addActionListener(e -> {
            pantallaCompleta(!completa);
            log("local " + (completa ? "activo" : "desactivo") + " pantalla completa");
        });

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        p.setOpaque(true);
        p.setBackground(new Color(0, 0, 0, 190));
        p.add(contador);
        p.add(bAtras);
        p.add(bSiguiente);
        p.add(bIr);
        p.add(bCompleta);
        contador.setForeground(Color.WHITE);
        return p;
    }
}