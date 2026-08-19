package classes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

// Acceso al material de la presentacion.
// Recibe un PDF o una carpeta de imagenes y entrega las paginas ya cargadas.
// No guarda cual se muestra: solo sirve la que le pidan.
public class Diapositivas {

    private final BufferedImage[] paginas;

    public Diapositivas(String ruta) throws Exception {
        File carpeta = esPdf(ruta) ? convertirPdf(ruta) : new File(ruta);
        File[] archivos = listarImagenes(carpeta);
        paginas = cargar(archivos);
    }

    // Cuantas diapositivas hay.
    public int total() {
        return paginas.length;
    }

    // La diapositiva n, en base 1. Devuelve null si n esta fuera de rango.
    public BufferedImage imagen(int n) {
        if (n < 1 || n > paginas.length) return null;
        return paginas[n - 1];
    }

    // ---------- privados ----------

    private boolean esPdf(String ruta) {
        return ruta.toLowerCase().endsWith(".pdf");
    }

    // Convierte el PDF a PNG en una carpeta temporal y devuelve esa carpeta.
    private File convertirPdf(String ruta) throws Exception {
        File destino = new File(System.getProperty("java.io.tmpdir"), "slides-" + System.currentTimeMillis());
        destino.mkdirs();
        String prefijo = new File(destino, "pag").getAbsolutePath();

        Process p = new ProcessBuilder("pdftoppm", "-png", "-r", "150",
                                       new File(ruta).getAbsolutePath(), prefijo).start();
        if (p.waitFor() != 0) throw new Exception("pdftoppm fallo al convertir " + ruta);

        System.out.println("PDF convertido en: " + destino);
        return destino;
    }

    // Imagenes de la carpeta, ordenadas por nombre.
    private File[] listarImagenes(File carpeta) throws Exception {
        File[] archivos = carpeta.listFiles(f -> {
            String n = f.getName().toLowerCase();
            return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
        });
        if (archivos == null || archivos.length == 0) throw new Exception("Sin imagenes en " + carpeta);
        Arrays.sort(archivos);
        return archivos;
    }

    private BufferedImage[] cargar(File[] archivos) throws Exception {
        BufferedImage[] imgs = new BufferedImage[archivos.length];
        for (int i = 0; i < archivos.length; i++) imgs[i] = ImageIO.read(archivos[i]);
        return imgs;
    }
}