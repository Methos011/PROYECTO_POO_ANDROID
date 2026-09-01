package com.espol.pronosticosmundial2026;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.espol.pronosticosmundial2026.excepciones.DatosIncompletosException;
import com.espol.pronosticosmundial2026.excepciones.PronosticoFueraDeTiempoException;
import com.espol.pronosticosmundial2026.modelo.EstadoPartido;
import com.espol.pronosticosmundial2026.modelo.Fase;
import com.espol.pronosticosmundial2026.modelo.Partido;
import com.espol.pronosticosmundial2026.modelo.Pronostico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Pantalla donde el participante registra sus pronósticos para los
 * partidos del Mundial, filtrados por fase mediante un Spinner. Cada
 * partido se dibuja dinámicamente como una tarjeta con las banderas de
 * ambas selecciones y dos campos de goles, que quedan habilitados o
 * deshabilitados según si el partido está ABIERTO. Los pronósticos se
 * guardan mediante serialización de objetos, un archivo por participante
 * y fase, reemplazando el pronóstico anterior si ya existía uno para el
 * mismo partido.
 *
 * @author Jair Cárdenas
 */
public class PronosticosActivity extends AppCompatActivity {

    /** Id del participante que inició sesión, recibido por Intent desde MenuParticipanteActivity. */
    private int idUsuario;

    /**
     * Inicializa la pantalla: infla el layout, recupera el id del
     * participante, y configura el Spinner de fases con su
     * OnItemSelectedListener, que vuelve a dibujar los partidos de la
     * fase elegida cada vez que cambia la selección.
     *
     * @param savedInstanceState estado previamente guardado de la actividad, si existe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pronosticos);

        idUsuario = getIntent().getIntExtra("idUsuario", -1);

        Spinner spFase = findViewById(R.id.spFase);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.fases_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFase.setAdapter(adapter);

        spFase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mostrarPartidosDeFase(Fase.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Recarga los partidos y reconstruye el contenedor dinámico,
     * mostrando únicamente los que pertenecen a la fase indicada. Si no
     * hay partidos registrados para esa fase, muestra un mensaje
     * informativo en vez de dejar la pantalla vacía sin explicación.
     *
     * @param fase la fase seleccionada en el Spinner
     */
    private void mostrarPartidosDeFase(Fase fase) {
        ArrayList<Partido> partidos = cargarPartidos();
        LinearLayout contenedor = findViewById(R.id.contenedorPartidos);
        contenedor.removeAllViews();
        boolean hayPartidos = false;

        for (Partido p : partidos) {
            if (p.getFase() != fase) continue;
            hayPartidos = true;
            crearTarjetaPartido(p, fase, contenedor);
        }

        if (!hayPartidos) {
            TextView mensaje = new TextView(this);
            mensaje.setText("No hay partidos registrados para esta fase.");
            mensaje.setTextSize(15);
            mensaje.setTextColor(getColor(R.color.text_secondary));
            mensaje.setGravity(Gravity.CENTER);
            mensaje.setPadding(dp(20), dp(30), dp(20), dp(30));
            contenedor.addView(mensaje);
        }
    }

    /**
     * Construye dinámicamente la tarjeta de un partido: encabezado con
     * la fase y un badge de estado con color, fecha y estadio, las
     * banderas de ambas selecciones, los campos de goles (precargados
     * con el pronóstico existente si ya se había guardado uno) y el
     * botón "Guardar pronóstico". Tanto los campos como el botón se
     * deshabilitan si el partido ya no está en estado ABIERTO. Es el
     * método central de programación dinámica de GUI de esta pantalla.
     *
     * @param p el partido a representar en la tarjeta
     * @param fase la fase a la que pertenece el partido, usada para el nombre del archivo de pronósticos
     * @param contenedor el LinearLayout donde se agrega la tarjeta construida
     */
    private void crearTarjetaPartido(Partido p, Fase fase, LinearLayout contenedor) {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(dp(16), dp(16), dp(16), dp(16));
        tarjeta.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams parametrosTarjeta = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        parametrosTarjeta.setMargins(0, 0, 0, dp(16));
        tarjeta.setLayoutParams(parametrosTarjeta);

        // Encabezado
        LinearLayout encabezado = new LinearLayout(this);
        encabezado.setOrientation(LinearLayout.HORIZONTAL);
        encabezado.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvFase = crearTextView(nombreFase(fase), 13, true);
        tvFase.setTextColor(getColor(R.color.primary));
        encabezado.addView(tvFase, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvEstado = crearTextView(nombreEstado(p.getEstado()), 12, true);
        tvEstado.setGravity(Gravity.CENTER);
        tvEstado.setPadding(dp(12), dp(6), dp(12), dp(6));

        if (p.getEstado() == EstadoPartido.ABIERTO) {
            tvEstado.setTextColor(getColor(R.color.success));
            tvEstado.setBackgroundResource(R.drawable.bg_status_open);
        } else if (p.getEstado() == EstadoPartido.CERRADO) {
            tvEstado.setTextColor(getColor(R.color.warning));
            tvEstado.setBackgroundResource(R.drawable.bg_status_closed);
        } else {
            tvEstado.setTextColor(getColor(R.color.primary));
            tvEstado.setBackgroundResource(R.drawable.bg_status_finished);
        }
        encabezado.addView(tvEstado);
        tarjeta.addView(encabezado);

        // Información del Partido
        TextView tvFecha = crearTextView(p.getFecha() + "  •  " + p.getHora() + " UTC", 13, false);
        tvFecha.setTextColor(getColor(R.color.text_secondary));
        tvFecha.setGravity(Gravity.CENTER);
        tvFecha.setLayoutParams(margenSuperior(0, 12, 0, 0));
        tarjeta.addView(tvFecha);

        TextView tvEstadio = crearTextView(p.getEstadio(), 12, false);
        tvEstadio.setTextColor(getColor(R.color.text_secondary));
        tvEstadio.setGravity(Gravity.CENTER);
        tarjeta.addView(tvEstadio);

        // Equipos
        LinearLayout equipos = new LinearLayout(this);
        equipos.setOrientation(LinearLayout.HORIZONTAL);
        equipos.setGravity(Gravity.CENTER);
        equipos.setPadding(0, dp(18), 0, dp(12));

        equipos.addView(crearEquipo(p.getSeleccion1()), pesoCompleto());

        TextView tvVs = crearTextView("VS", 13, true);
        tvVs.setTextColor(getColor(R.color.text_secondary));
        tvVs.setGravity(Gravity.CENTER);
        equipos.addView(tvVs, new LinearLayout.LayoutParams(dp(50), LinearLayout.LayoutParams.WRAP_CONTENT));

        equipos.addView(crearEquipo(p.getSeleccion2()), pesoCompleto());
        tarjeta.addView(equipos);

        // Campos de Goles
        LinearLayout marcador = new LinearLayout(this);
        marcador.setOrientation(LinearLayout.HORIZONTAL);
        marcador.setGravity(Gravity.CENTER);

        EditText editGoles1 = crearCampoGoles(p.getSeleccion1());
        EditText editGoles2 = crearCampoGoles(p.getSeleccion2());
        cargarPronosticoExistente(p, fase, editGoles1, editGoles2);

        marcador.addView(editGoles1, pesoCompleto());

        TextView separador = crearTextView("-", 22, true);
        separador.setGravity(Gravity.CENTER);
        separador.setTextColor(getColor(R.color.text_secondary));
        marcador.addView(separador, new LinearLayout.LayoutParams(dp(45), dp(50)));

        marcador.addView(editGoles2, pesoCompleto());
        tarjeta.addView(marcador);

        // Botón Guardar
        Button btnGuardar = new Button(this);
        btnGuardar.setText("Guardar pronóstico");
        btnGuardar.setTextSize(15);
        btnGuardar.setTextColor(getColor(R.color.text_primary));
        btnGuardar.setAllCaps(false); // <- ERROR CORREGIDO AQUÍ
        btnGuardar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        btnGuardar.setBackgroundResource(R.drawable.bg_button_primary);
        btnGuardar.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams parametrosBoton = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(50));
        parametrosBoton.setMargins(0, dp(14), 0, 0);
        btnGuardar.setLayoutParams(parametrosBoton);

        boolean abierto = p.getEstado() == EstadoPartido.ABIERTO;
        editGoles1.setEnabled(abierto);
        editGoles2.setEnabled(abierto);
        btnGuardar.setEnabled(abierto);
        btnGuardar.setAlpha(abierto ? 1f : 0.45f);

        btnGuardar.setOnClickListener(v -> guardarPronostico(p, fase, editGoles1, editGoles2));

        tarjeta.addView(btnGuardar);
        contenedor.addView(tarjeta);
    }

    /**
     * Construye el bloque visual de una selección: su bandera (buscada
     * dinámicamente por nombre) y su nombre debajo.
     *
     * @param nombrePais nombre de la selección a representar
     * @return el LinearLayout con la bandera y el nombre de la selección
     */
    private LinearLayout crearEquipo(String nombrePais) {
        LinearLayout equipo = new LinearLayout(this);
        equipo.setOrientation(LinearLayout.VERTICAL);
        equipo.setGravity(Gravity.CENTER);

        ImageView bandera = new ImageView(this);
        bandera.setImageResource(obtenerBandera(nombrePais));
        bandera.setContentDescription("Bandera de " + nombrePais);
        bandera.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        equipo.addView(bandera, new LinearLayout.LayoutParams(dp(56), dp(40)));

        TextView nombre = crearTextView(nombrePais, 14, true);
        nombre.setGravity(Gravity.CENTER);
        nombre.setTextColor(getColor(R.color.text_primary));
        nombre.setPadding(dp(4), dp(8), dp(4), 0);
        equipo.addView(nombre);

        return equipo;
    }

    /**
     * Crea un campo de texto numérico para ingresar los goles pronosticados
     * de una selección, con teclado numérico forzado.
     *
     * @param pais selección a la que pertenece el campo, usada solo para accesibilidad
     * @return el EditText configurado para ingresar goles
     */
    private EditText crearCampoGoles(String pais) {
        EditText campo = new EditText(this);
        campo.setHint("0");
        campo.setTextSize(18);
        campo.setGravity(Gravity.CENTER);
        campo.setInputType(InputType.TYPE_CLASS_NUMBER);
        campo.setSingleLine(true);
        campo.setTextColor(getColor(R.color.text_primary));
        campo.setHintTextColor(getColor(R.color.text_secondary));
        campo.setBackgroundResource(R.drawable.bg_score);
        campo.setPadding(dp(8), 0, dp(8), 0);
        campo.setContentDescription("Goles de " + pais);
        return campo;
    }

    /**
     * Busca si el participante ya tiene un pronóstico guardado para este
     * partido dentro del archivo .dat de la fase correspondiente, y de
     * ser así, precarga los campos de goles con esos valores, para que
     * el participante vea su pronóstico anterior en vez de campos vacíos.
     *
     * @param partido el partido cuyo pronóstico existente se busca
     * @param fase la fase del partido, usada para ubicar el archivo .dat correcto
     * @param editGoles1 campo de goles de la primera selección, a precargar si corresponde
     * @param editGoles2 campo de goles de la segunda selección, a precargar si corresponde
     */
    private void cargarPronosticoExistente(Partido partido, Fase fase, EditText editGoles1, EditText editGoles2) {
        String nombreArchivo = "pronostico_" + idUsuario + "_" + fase.name() + ".dat";
        ArrayList<Pronostico> pronosticos = cargarPronosticosDeArchivo(nombreArchivo);

        for (Pronostico pr : pronosticos) {
            if (pr.getIdPartidoP() == partido.getPartidoId()) {
                editGoles1.setText(String.valueOf(pr.getGolesSeleccion1P()));
                editGoles2.setText(String.valueOf(pr.getGolesSeleccion2P()));
                break;
            }
        }
    }

    /**
     * Controlador de evento del botón "Guardar pronóstico". Valida que
     * el partido siga ABIERTO (lanzando PronosticoFueraDeTiempoException
     * si no lo está), que los goles ingresados no estén vacíos ni sean
     * negativos (lanzando DatosIncompletosException en caso contrario),
     * y luego construye el objeto Pronostico y lo guarda por
     * serialización: carga la lista existente del archivo .dat
     * correspondiente, elimina cualquier pronóstico previo para el
     * mismo partido con removeIf() (para reemplazarlo en vez de
     * duplicarlo), agrega el nuevo, y reescribe la lista completa con
     * ObjectOutputStream.
     *
     * @param p el partido sobre el que se registra el pronóstico
     * @param fase la fase del partido, usada para el nombre del archivo .dat
     * @param editGoles1 campo con los goles pronosticados para la primera selección
     * @param editGoles2 campo con los goles pronosticados para la segunda selección
     */
    private void guardarPronostico(Partido p, Fase fase, EditText editGoles1, EditText editGoles2) {
        try {
            if (p.getEstado() != EstadoPartido.ABIERTO) {
                throw new PronosticoFueraDeTiempoException("El período para registrar pronósticos de este partido ya ha finalizado.");
            }

            String texto1 = editGoles1.getText().toString().trim();
            String texto2 = editGoles2.getText().toString().trim();

            if (texto1.isEmpty() || texto2.isEmpty()) {
                throw new DatosIncompletosException("Debe ingresar los goles de ambas selecciones.");
            }

            int goles1 = Integer.parseInt(texto1);
            int goles2 = Integer.parseInt(texto2);

            if (goles1 < 0 || goles2 < 0) {
                throw new DatosIncompletosException("Los goles deben ser mayores o iguales a cero.");
            }

            Pronostico pronostico = new Pronostico(p.getPartidoId(), idUsuario, p.getPartidoId(), goles1, goles2, 0);
            String nombreArchivo = "pronostico_" + idUsuario + "_" + fase.name() + ".dat";

            ArrayList<Pronostico> pronosticos = cargarPronosticosDeArchivo(nombreArchivo);
            pronosticos.removeIf(pr -> pr.getIdPartidoP() == p.getPartidoId());
            pronosticos.add(pronostico);

            try (ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(nombreArchivo, MODE_PRIVATE))) {
                oos.writeObject(pronosticos);
            }

            Toast.makeText(this, "Pronóstico guardado correctamente.", Toast.LENGTH_SHORT).show();

        } catch (PronosticoFueraDeTiempoException | DatosIncompletosException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Los goles deben ser números válidos.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ocurrió un error al guardar el pronóstico.", Toast.LENGTH_SHORT).show();
        }
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
        String nombreRecurso = "flag_" + normalizarNombrePais(nombrePais);
        int id = getResources().getIdentifier(nombreRecurso, "drawable", getPackageName());


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

    /** @return el nombre del estado del partido en español, listo para mostrar en pantalla */
    private String nombreEstado(EstadoPartido estado) {
        switch (estado) {
            case ABIERTO: return "Abierto";
            case CERRADO: return "Cerrado";
            case FINALIZADO: return "Finalizado";
            default: return estado.name();
        }
    }

    /** @return un TextView configurado con el texto, tamaño y negrita indicados */
    private TextView crearTextView(String texto, int tamano, boolean negrita) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(tamano);
        if (negrita) {
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return tv;
    }

    /** @return parámetros de layout que reparten el espacio disponible en partes iguales */
    private LinearLayout.LayoutParams pesoCompleto() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    }

    /** @return parámetros de layout con los márgenes indicados (en dp), usados para separar elementos verticalmente */
    private LinearLayout.LayoutParams margenSuperior(int izquierda, int arriba, int derecha, int abajo) {
        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        parametros.setMargins(dp(izquierda), dp(arriba), dp(derecha), dp(abajo));
        return parametros;
    }

    /** @return el valor en píxeles equivalente al valor en dp, según la densidad de pantalla */
    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    /**
     * Lee partidos.txt línea por línea (salteando el encabezado) y
     * construye la lista completa de partidos del torneo.
     *
     * @return la lista completa de partidos con su estado actual
     */
    private ArrayList<Partido> cargarPartidos() {
        ArrayList<Partido> listaPartidos = new ArrayList<>();
        try (InputStream is = abrirPartidos();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine(); // salta el encabezado
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datosParticionados = linea.strip().split(";");
                Partido p = new Partido(
                        Integer.parseInt(datosParticionados[0]),
                        Fase.valueOf(datosParticionados[1]),
                        datosParticionados[2],
                        datosParticionados[3],
                        datosParticionados[4],
                        datosParticionados[5],
                        datosParticionados[6],
                        EstadoPartido.valueOf(datosParticionados[7])
                );
                listaPartidos.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listaPartidos;
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
        java.io.File archivoInterno = new java.io.File(getFilesDir(), "partidos.txt");
        if (archivoInterno.exists()) {
            return openFileInput("partidos.txt");
        } else {
            return getAssets().open("partidos.txt");
        }
    }

    /**
     * Lee y deserializa el archivo .dat indicado, reconstruyendo la
     * lista de objetos Pronostico guardados por el participante para
     * una fase específica. Si el archivo aún no existe (porque el
     * participante no ha guardado ningún pronóstico en esa fase),
     * retorna una lista vacía en vez de fallar.
     *
     * @param nombreArchivo nombre del archivo .dat a leer (formato pronostico_idusuario_fase.dat)
     * @return la lista de pronósticos guardados en ese archivo, o una lista vacía si no existe
     */
    private ArrayList<Pronostico> cargarPronosticosDeArchivo(String nombreArchivo) {
        java.io.File archivo = new java.io.File(getFilesDir(), nombreArchivo);
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(openFileInput(nombreArchivo))) {
            return (ArrayList<Pronostico>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
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