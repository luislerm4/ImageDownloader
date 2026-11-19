import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class ImageDownloader {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java ImageDownloader <URL>");
            return;
        }
        try {
            URI uri = URI.create(args[0]);
            URL url = uri.toURL();

            String protocol = url.getProtocol();
            String host = url.getHost();

            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                System.err.println("Protocolo no admitido (solo HTTP o HTTPS): " + protocol);
                return;
            }
            if (host == null || host.isBlank()) {
                System.err.println("URL inválida: no se especificó un servidor válido.");
                return;
            }
            System.out.println("Protocolo: " + protocol);
            System.out.println("Servidor: " + host);
            System.out.println("Puerto: " + url.getPort());
            System.out.println("Ruta: " + url.getPath());
        } catch (IllegalArgumentException e) {
            System.err.println("URL inválida: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("URL no admitida: " + e.getMessage());
        }
    }
}