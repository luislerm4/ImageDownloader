import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageDownloader {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java ImageDownloader <URL>");
            return;
        }
        URL url = null;
        try {
            URI uri = URI.create(args[0]);
            url = uri.toURL();

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
            return;
        } catch (MalformedURLException e) {
            System.err.println("URL no admitida: " + e.getMessage());
            return;
        }
        try {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);

            if (connection instanceof HttpURLConnection httpConn) {
                System.out.println("Código HTTP: " + httpConn.getResponseCode());
            }

            String contentType = connection.getContentType();
            System.out.println("Tipo de contenido: " + contentType);

            Charset charset = detectarCharset(contentType);
            System.out.println("Codificación utilizada: " + charset.name());

            String html = leerHtml(connection.getInputStream(), charset);

            if (html != null && !html.isEmpty()) {
                int limite = Math.min(500, html.length());
                System.out.println("\nPrimeros " + limite + " caracteres del HTML:\n");
                System.out.println(html.substring(0, limite));
            } else {
                System.out.println("No hay contenido disponible para mostrar.");
            }
            Set<String> imagenes = extraerImagenes(html);
            System.out.println("\nReferencias a imágenes encontradas (" + imagenes.size() + "):");
            for (String img : imagenes) {
                System.out.println(" - " + img);
            }
            Set<URL> urlsImagenes = new LinkedHashSet<>();
            System.out.println("\nURLs de imágenes resueltas:");
            for (String ref : imagenes) {
                try {
                    URL imgUrl = resolverImagen(url, ref);
                    urlsImagenes.add(imgUrl);
                    System.out.println(" - " + imgUrl);
                } catch (Exception e) {
                    System.err.println(" - Omitida (" + ref + "): " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al conectar o leer el recurso: " + e.getMessage());
        }
    }
    private static Charset detectarCharset(String contentType) {
        if (contentType != null) {
            for (String parte : contentType.split(";")) {
                String limpia = parte.trim();
                if (limpia.toLowerCase().startsWith("charset=")) {
                    String nombreCharset = limpia.substring("charset=".length()).trim();
                    if (nombreCharset.startsWith("\"") && nombreCharset.endsWith("\"") && nombreCharset.length() > 1) {
                        nombreCharset = nombreCharset.substring(1, nombreCharset.length() - 1);
                    }
                    try {
                        return Charset.forName(nombreCharset);
                    } catch (IllegalCharsetNameException | UnsupportedCharsetException ignored) {
                        return StandardCharsets.UTF_8;
                    }
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static String leerHtml(InputStream input, Charset charset) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
    public static Set<String> extraerImagenes(String html) {
        Set<String> referencias = new LinkedHashSet<>();
        if (html == null || html.isBlank()) {
            return referencias;
        }

        Pattern imgPattern = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
        Pattern srcPattern = Pattern.compile("\\bsrc\\s*=\\s*([\"'])(.*?)\\1", Pattern.CASE_INSENSITIVE);

        Matcher imgMatcher = imgPattern.matcher(html);
        while (imgMatcher.find()) {
            String etiqueta = imgMatcher.group();
            Matcher srcMatcher = srcPattern.matcher(etiqueta);
            if (srcMatcher.find()) {
                String rutaSrc = srcMatcher.group(2).trim();
                if (!rutaSrc.isEmpty()) {
                    referencias.add(rutaSrc);
                }
            }
        }

        return referencias;
    }
    public static URL resolverImagen(URL urlPagina, String referencia) throws Exception {
        if (urlPagina == null) {
            throw new IllegalArgumentException("La URL base de la página no puede ser nula.");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia de imagen está vacía.");
        }

        String limpia = referencia.trim();
        String minuscula = limpia.toLowerCase();

        if (minuscula.startsWith("data:") || minuscula.startsWith("javascript:")) {
            throw new IllegalArgumentException("Esquema no admitido: " + limpia);
        }

        URL resuelta = new URL(urlPagina, limpia);
        String protocolo = resuelta.getProtocol();

        if (!"http".equalsIgnoreCase(protocolo) && !"https".equalsIgnoreCase(protocolo)) {
            throw new IllegalArgumentException("Protocolo no admitido en imagen: " + protocolo);
        }

        if (resuelta.getHost() == null || resuelta.getHost().isBlank()) {
            throw new IllegalArgumentException("La URL resultante no tiene servidor válido: " + resuelta);
        }

        return resuelta;
    }
}