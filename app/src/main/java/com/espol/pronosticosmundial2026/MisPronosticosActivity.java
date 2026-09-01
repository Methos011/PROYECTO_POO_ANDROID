package com.espol.pronosticosmundial2026;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.espol.pronosticosmundial2026.modelo.EstadoPartido;
import com.espol.pronosticosmundial2026.modelo.Fase;
import com.espol.pronosticosmundial2026.modelo.Partido;
import com.espol.pronosticosmundial2026.modelo.Pronostico;
import com.espol.pronosticosmundial2026.modelo.Resultado;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Pantalla que muestra únicamente los pronósticos registrados por el
 * participante que inició sesión, recorriendo todas las fases posibles
 * y leyendo el archivo .dat correspondiente a cada una. Por cada
 * pronóstico encontrado, cruza los datos con Partido (para mostrar
 * selecciones y banderas) y con Resultado (para mostrar el marcador
 * oficial y los puntos obtenidos, si el partido ya finalizó), o un
 * mensaje de "pendiente" en caso contrario. A diferencia de las demás
 * Activities de esta app, esta pantalla es de solo lectura: no escribe
 * ni modifica ningún archivo.
 *
 * @author David Delgado
 */
public class MisPronosticosActivity extends AppCompatActivity {

    /** Id del participante que inició sesión, recibido por Intent desde MenuParticipanteActivity. */
    private int idUsuario;

    /**
     * Inicializa la pantalla: infla el layout, recupera el id del
     * participante, y llama a mostrarMisPronosticos() para construir la
     * lista completa de tarjetas con sus pronósticos.
     *
     * @param savedInstanceState estado previamente guardado de la actividad, si existe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mis_pronosticos);

        idUsuario = getIntent().getIntExtra("idUsuario", -1);

        mostrarMisPronosticos();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Recorre las 7 fases del torneo mediante Fase.values(), y por cada
     * una intenta abrir el archivo .dat correspondiente al participante.
     * Por cada pronóstico encontrado, busca su partido y su resultado
     * oficial (si existe), y construye la tarjeta correspondiente. Si el
     * participante no tiene ningún pronóstico registrado en ninguna
     * fase, muestra un mensaje informativo en vez de dejar la pantalla
     * vacía.
     */
    private void mostrarMisPronosticos() {
        LinearLayout contenedor = findViewById(R.id.contenedorPronosticos);
        contenedor.removeAllViews();

        ArrayList<Partido> partidos = cargarPartidos();
        ArrayList<Resultado> resultados = cargarResultados();
        boolean hayPronosticos = false;

        for (Fase fase : Fase.values()) {
            String nombreArchivo = "pronostico_" + idUsuario + "_" + fase.name() + ".dat";
            ArrayList<Pronostico> pronosticos = cargarPronosticosDeArchivo(nombreArchivo);

            for (Pronostico pr : pronosticos) {
                Partido partidoEncontrado = buscarPartido(partidos, pr.getIdPartidoP());
                if (partidoEncontrado == null) continue;

                hayPronosticos = true;
                Resultado resultado = buscarResultado(resultados, pr.getIdPartidoP());

                crearTarjetaPronostico(fase, partidoEncontrado, pr, resultado, contenedor);
            }
        }

        if (!hayPronosticos) {
            TextView mensaje = new TextView(this);
            mensaje.setText("Todavía no tienes pronósticos registrados.");
            mensaje.setTextSize(15);
            mensaje.setTextColor(getColor(R.color.text_secondary));
            mensaje.setGravity(Gravity.CENTER);
            mensaje.setPadding(dp(20), dp(35), dp(20), dp(35));
            contenedor.addView(mensaje);
        }
    }

    /**
     * Construye dinámicamente la tarjeta de un pronóstico: la fase, las
     * banderas y nombres de ambas selecciones, el marcador pronosticado
     * en grande, y según si ya hay resultado oficial disponible, muestra
     * el marcador real junto con los puntos obtenidos, o un mensaje de
     * "pendiente" mientras el partido no haya finalizado.
     *
     * @param fase la fase a la que pertenece el pronóstico
     * @param partido el partido sobre el que se pronosticó
     * @param pronostico el pronóstico registrado por el participante
     * @param resultado el resultado oficial del partido, o null si aún no está disponible
     * @param contenedor el LinearLayout donde se agrega la tarjeta construida
     */
    private void crearTarjetaPronostico(Fase fase, Partido partido, Pronostico pronostico, Resultado resultado, LinearLayout contenedor) {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(dp(16), dp(16), dp(16), dp(16));
        tarjeta.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        parametros.setMargins(0, 0, 0, dp(16));
        tarjeta.setLayoutParams(parametros);

        // FASE
        TextView tvFase = crearTexto(nombreFase(fase), 13, true);
        tvFase.setTextColor(getColor(R.color.primary));
        tarjeta.addView(tvFase);

        // EQUIPOS
        LinearLayout equipos = new LinearLayout(this);
        equipos.setOrientation(LinearLayout.HORIZONTAL);
        equipos.setGravity(Gravity.CENTER);
        equipos.setPadding(0, dp(18), 0, dp(12));

        equipos.addView(crearEquipo(partido.getSeleccion1()), peso());

        TextView vs = crearTexto("VS", 13, true);
        vs.setGravity(Gravity.CENTER);
        vs.setTextColor(getColor(R.color.text_secondary));
        equipos.addView(vs, new LinearLayout.LayoutParams(dp(45), LinearLayout.LayoutParams.WRAP_CONTENT));

        equipos.addView(crearEquipo(partido.getSeleccion2()), peso());
        tarjeta.addView(equipos);

        // PRONÓSTICO
        TextView titulo = crearTexto("Tu pronóstico", 13, true);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTextColor(getColor(R.color.text_secondary));
        tarjeta.addView(titulo);

        TextView marcador = crearTexto(pronostico.getGolesSeleccion1P() + "  -  " + pronostico.getGolesSeleccion2P(), 28, true);
        marcador.setGravity(Gravity.CENTER);
        marcador.setTextColor(getColor(R.color.text_primary));
        marcador.setPadding(0, dp(8), 0, dp(14));
        tarjeta.addView(marcador);

        // RESULTADO
        if (resultado != null) {
            TextView resultadoTitulo = crearTexto("Resultado oficial", 13, true);
            resultadoTitulo.setGravity(Gravity.CENTER);
            resultadoTitulo.setTextColor(getColor(R.color.text_secondary));
            tarjeta.addView(resultadoTitulo);

            TextView resultadoTexto = crearTexto(resultado.getGolesSeleccion1R() + "  -  " + resultado.getGolesSeleccion2R(), 22, true);
            resultadoTexto.setGravity(Gravity.CENTER);
            resultadoTexto.setTextColor(getColor(R.color.primary));
            tarjeta.addView(resultadoTexto);

            TextView puntos = crearTexto("Puntos obtenidos: " + pronostico.getPuntosObtenidosP(), 15, true);
            puntos.setGravity(Gravity.CENTER);
            puntos.setTextColor(getColor(R.color.success));
            puntos.setPadding(0, dp(12), 0, 0);
            tarjeta.addView(puntos);
        } else {
            TextView pendiente = crearTexto("Resultado y puntos pendientes.", 14, false);
            pendiente.setGravity(Gravity.CENTER);
            pendiente.setTextColor(getColor(R.color.text_secondary));
            pendiente.setPadding(0, dp(8), 0, 0);
            tarjeta.addView(pendiente);
        }

        contenedor.addView(tarjeta);
    }

    /**
     * Construye el bloque visual de una selección: su bandera (buscada
     * dinámicamente por nombre) y su nombre debajo.
     *
     * @param pais nombre de la selección a representar
     * @return el LinearLayout con la bandera y el nombre de la selección
     */
    private LinearLayout crearEquipo(String pais) {
        LinearLayout equipo = new LinearLayout(this);
        equipo.setOrientation(LinearLayout.VERTICAL);
        equipo.setGravity(Gravity.CENTER);

        ImageView bandera = new ImageView(this);
        bandera.setImageResource(obtenerBandera(pais));
        bandera.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        equipo.addView(bandera, new LinearLayout.LayoutParams(dp(55), dp(38)));

        TextView nombre = crearTexto(pais, 14, true);
        nombre.setGravity(Gravity.CENTER);
        nombre.setPadding(dp(3), dp(7), dp(3), 0);
        equipo.addView(nombre);

        return equipo;
    }

    /**
     * Busca en drawable un recurso de imagen cuyo nombre siga el
     * patrón flag_nombrepais (normalizado). Si no existe ninguna
     * bandera con ese nombre, retorna un ícono genérico de balón como
     * respaldo.
     *
     * @param nombrePais nombre de la selección cuya bandera se busca
     * @return el id del recurso drawable de la bandera, o el ícono de respaldo
     */
    private int obtenerBandera(String nombrePais) {
        String nombre = "flag_" + normalizarNombrePais(nombrePais);
        int id = getResources().getIdentifier(nombre, "drawable", getPackageName());

        // Usamos el icono de fútbol si no se encuentra la imagen de la bandera
        return id == 0 ? R.drawable.ic_sports_soccer : id;
    }

    /**
     * Convierte el nombre de un país a un formato válido para nombre de
     * recurso Android: minúsculas, sin tildes ni caracteres especiales,
     * y espacios reemplazados por guiones bajos.
     *
     * @param nombrePais nombre original de la selección
     * @return el nombre normalizado, apto para buscarlo como recurso drawable
     */
    private String normalizarNombrePais(String nombrePais) {
        String texto = Normalizer.normalize(nombrePais, Normalizer.Form.NFD);
        texto = texto.replaceAll("\\p{M}", "");
        texto = texto.toLowerCase(Locale.ROOT);
        texto = texto.replaceAll("[^a-z0-9]+", "_");
        return texto.replaceAll("^_+|_+$", "");
    }

    /**
     * Busca dentro de una lista de partidos el que coincide con el id
     * indicado.
     *
     * @param partidos la lista completa de partidos donde buscar
     * @param idPartido id del partido a buscar
     * @return el Partido encontrado, o null si no existe ninguno con ese id
     */
    private Partido buscarPartido(ArrayList<Partido> partidos, int idPartido) {
        for (Partido p : partidos) {
            if (p.getPartidoId() == idPartido) return p;
        }
        return null;
    }

    /**
     * Busca dentro de una lista de resultados el que coincide con el
     * id de partido indicado.
     *
     * @param resultados la lista completa de resultados donde buscar
     * @param idPartido id del partido cuyo resultado se busca
     * @return el Resultado encontrado, o null si el partido aún no tiene resultado oficial
     */
    private Resultado buscarResultado(ArrayList<Resultado> resultados, int idPartido) {
        for (Resultado r : resultados) {
            if (r.getIdPartido() == idPartido) return r;
        }
        return null;
    }

    /** @return el nombre de la fase en español, listo para mostrar en pantalla */
    private String nombreFase(Fase fase) {
        switch (fase) {
            case FASE_DE_GRUPOS: return "Fase de grupos";
            case DIECISEISAVOS_DE_FINAL: return "Dieciseisavos";
            case OCTAVOS_DE_FINAL: return "Octavos";
            case CUARTOS_DE_FINAL: return "Cuartos de final";
            case SEMIFINALES: return "Semifinales";
            case TERCER_LUGAR: return "Tercer lugar";
            case FINAL: return "Final";
            default: return fase.name();
        }
    }

    /** @return un TextView configurado con el texto, tamaño y negrita indicados */
    private TextView crearTexto(String texto, int tamano, boolean negrita) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(tamano);
        if (negrita) {
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        tv.setTextColor(getColor(R.color.text_primary));
        return tv;
    }

    /** @return parámetros de layout que reparten el espacio disponible en partes iguales */
    private LinearLayout.LayoutParams peso() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    }

    /** @return el valor en píxeles equivalente al valor en dp, según la densidad de pantalla */
    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    /**
     * Lee partidos.txt línea por línea (salteando el encabezado) y
     * construye la lista completa de partidos del torneo, necesaria
     * para saber contra qué selecciones jugó cada pronóstico guardado.
     *
     * @return la lista completa de partidos del torneo
     */
    private ArrayList<Partido> cargarPartidos() {
        ArrayList<Partido> lista = new ArrayList<>();
        try (InputStream is = abrirPartidos();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine(); // Saltar encabezado
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.strip().split(";");
                Partido p = new Partido(
                        Integer.parseInt(datos[0]),
                        Fase.valueOf(datos[1]),
                        datos[2],
                        datos[3],
                        datos[4],
                        datos[5],
                        datos[6],
                        EstadoPartido.valueOf(datos[7])
                );
                lista.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Decide desde dónde leer partidos.txt: si ya existe una copia en
     * el almacenamiento interno, la usa; de lo contrario, lee la
     * versión original de assets.
     *
     * @return el InputStream del archivo partidos.txt correspondiente
     * @throws IOException si ocurre un error al abrir el archivo
     */
    private InputStream abrirPartidos() throws IOException {
        java.io.File archivo = new java.io.File(getFilesDir(), "partidos.txt");
        if (archivo.exists()) {
            return openFileInput("partidos.txt");
        } else {
            return getAssets().open("partidos.txt");
        }
    }

    /**
     * Lee y deserializa el archivo .dat indicado, reconstruyendo la
     * lista de objetos Pronostico guardados para una fase específica.
     *
     * @param nombreArchivo nombre del archivo .dat a leer
     * @return la lista de pronósticos guardados en ese archivo, o una lista vacía si no existe
     */
    private ArrayList<Pronostico> cargarPronosticosDeArchivo(String nombreArchivo) {
        java.io.File archivo = new java.io.File(getFilesDir(), nombreArchivo);
        if (!archivo.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(openFileInput(nombreArchivo))) {
            return (ArrayList<Pronostico>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lee resultados.txt desde el almacenamiento interno y construye la
     * lista de resultados oficiales ya registrados, necesaria para saber
     * cuáles de los pronósticos del participante ya pueden mostrar su
     * marcador real y puntos obtenidos.
     *
     * @return la lista de resultados oficiales registrados hasta el momento
     */
    private ArrayList<Resultado> cargarResultados() {
        ArrayList<Resultado> lista = new ArrayList<>();
        java.io.File archivo = new java.io.File(getFilesDir(), "resultados.txt");
        if (!archivo.exists()) return lista;

        try (InputStream is = openFileInput("resultados.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.strip().split(";");
                lista.add(new Resultado(
                        Integer.parseInt(datos[0]),
                        Integer.parseInt(datos[1]),
                        Integer.parseInt(datos[2]),
                        Integer.parseInt(datos[3])
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Controlador de evento del botón "Volver". Cierra esta pantalla y
     * regresa al menú principal del participante.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void volver(View view) {
        finish();
    }
}