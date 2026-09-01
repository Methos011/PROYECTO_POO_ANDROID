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
import com.espol.pronosticosmundial2026.modelo.EstadoPartido;
import com.espol.pronosticosmundial2026.modelo.Partido;
import com.espol.pronosticosmundial2026.modelo.Fase;
import com.espol.pronosticosmundial2026.modelo.Resultado;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Pantalla exclusiva del administrador para gestionar los partidos del
 * torneo: filtrar por fase mediante un Spinner, cerrar los pronósticos
 * de un partido, y registrar su resultado oficial. Cada partido se
 * dibuja dinámicamente como una tarjeta cuyo contenido y controles
 * cambian según su estado (ABIERTO, CERRADO o FINALIZADO), reutilizando
 * el mismo patrón visual (colores e íconos de estado, banderas por
 * selección) que PronosticosActivity.
 *
 * @author Jair Cárdenas
 */
public class AdministrarPartidosActivity extends AppCompatActivity {

    /** Id del partido cuyo formulario de resultado está actualmente desplegado, o -1 si ninguno. */
    private int partidoEnRegistro = -1;
    /** Fase actualmente seleccionada en el Spinner, usada para filtrar los partidos a mostrar. */
    private Fase faseSeleccionada;

    /**
     * Inicializa la pantalla: infla el layout, configura el Spinner de
     * fases con un ArrayAdapter cargado desde fases_array, y le agrega
     * un OnItemSelectedListener que actualiza faseSeleccionada y vuelve
     * a dibujar la lista de partidos cada vez que el administrador
     * cambia de fase.
     *
     * @param savedInstanceState estado previamente guardado de la actividad, si existe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_administrar_partidos);

        Spinner spFase = findViewById(R.id.spFase);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.fases_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFase.setAdapter(adapter);

        spFase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fase[] fases = Fase.values();
                faseSeleccionada = fases[position];
                mostrarPartidos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Recarga la lista de partidos y reconstruye el contenido del
     * contenedor dinámico, mostrando únicamente los partidos que
     * pertenecen a faseSeleccionada. Se invoca cada vez que cambia la
     * fase o el estado de algún partido.
     */
    private void mostrarPartidos() {
        ArrayList<Partido> partidos = cargarPartidos();
        LinearLayout contenedor = findViewById(R.id.contenedorPartidos);
        contenedor.removeAllViews();

        for (Partido p : partidos) {
            if (p.getFase() != faseSeleccionada) {
                continue;
            }
            crearTarjetaAdministracion(p, partidos, contenedor);
        }
    }

    /**
     * Construye dinámicamente la tarjeta de un partido, con su encabezado
     * (número y badge de estado con color según ABIERTO/CERRADO/FINALIZADO),
     * fecha, estadio, y las dos selecciones con su bandera. Según el estado
     * del partido, agrega distintos controles: el botón "Cerrar pronósticos"
     * si está ABIERTO, el formulario de resultado o el botón "Registrar
     * resultado" si está CERRADO, o el resultado oficial ya finalizado.
     * Es el método central de programación dinámica de GUI de esta pantalla.
     *
     * @param p el partido a representar en la tarjeta
     * @param partidos la lista completa de partidos, usada para reescribir el archivo al guardar cambios
     * @param contenedor el LinearLayout donde se agrega la tarjeta construida
     */
    private void crearTarjetaAdministracion(Partido p, ArrayList<Partido> partidos, LinearLayout contenedor) {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(dp(16), dp(16), dp(16), dp(16));
        tarjeta.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        parametros.setMargins(0, 0, 0, dp(16));
        tarjeta.setLayoutParams(parametros);

        // ENCABEZADO
        LinearLayout encabezado = new LinearLayout(this);
        encabezado.setOrientation(LinearLayout.HORIZONTAL);

        TextView titulo = crearTexto("Partido #" + p.getPartidoId(), 14, true);
        titulo.setTextColor(getColor(R.color.primary));
        encabezado.addView(titulo, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView estado = crearTexto(nombreEstado(p.getEstado()), 12, true);
        estado.setGravity(Gravity.CENTER);
        estado.setPadding(dp(12), dp(6), dp(12), dp(6));

        if (p.getEstado() == EstadoPartido.ABIERTO) {
            estado.setTextColor(getColor(R.color.success));
            estado.setBackgroundResource(R.drawable.bg_status_open);
        } else if (p.getEstado() == EstadoPartido.CERRADO) {
            estado.setTextColor(getColor(R.color.warning));
            estado.setBackgroundResource(R.drawable.bg_status_closed);
        } else {
            estado.setTextColor(getColor(R.color.primary));
            estado.setBackgroundResource(R.drawable.bg_status_finished);
        }

        encabezado.addView(estado);
        tarjeta.addView(encabezado);

        // FECHA
        TextView fecha = crearTexto(p.getFecha() + "  •  " + p.getHora() + " UTC", 13, false);
        fecha.setGravity(Gravity.CENTER);
        fecha.setTextColor(getColor(R.color.text_secondary));
        fecha.setPadding(0, dp(12), 0, 0);
        tarjeta.addView(fecha);

        // ESTADIO
        TextView estadio = crearTexto(p.getEstadio(), 12, false);
        estadio.setGravity(Gravity.CENTER);
        estadio.setTextColor(getColor(R.color.text_secondary));
        tarjeta.addView(estadio);

        // EQUIPOS
        LinearLayout equipos = new LinearLayout(this);
        equipos.setOrientation(LinearLayout.HORIZONTAL);
        equipos.setGravity(Gravity.CENTER);
        equipos.setPadding(0, dp(18), 0, dp(15));

        equipos.addView(crearEquipo(p.getSeleccion1()), peso());

        TextView vs = crearTexto("VS", 13, true);
        vs.setGravity(Gravity.CENTER);
        vs.setTextColor(getColor(R.color.text_secondary));
        equipos.addView(vs, new LinearLayout.LayoutParams(dp(45), LinearLayout.LayoutParams.WRAP_CONTENT));

        equipos.addView(crearEquipo(p.getSeleccion2()), peso());
        tarjeta.addView(equipos);

        // ESTADO ABIERTO
        if (p.getEstado() == EstadoPartido.ABIERTO) {
            Button cerrar = crearBoton("Cerrar pronósticos", R.drawable.bg_button_primary, R.color.text_primary);
            cerrar.setOnClickListener(v -> {
                p.setEstado(EstadoPartido.CERRADO);
                guardarPartidos(partidos);
                mostrarPartidos();
            });
            tarjeta.addView(cerrar);
        }
        // ESTADO CERRADO
        else if (p.getEstado() == EstadoPartido.CERRADO) {
            TextView mensaje = crearTexto("Los pronósticos ya no pueden modificarse.", 13, false);
            mensaje.setTextColor(getColor(R.color.text_secondary));
            mensaje.setGravity(Gravity.CENTER);
            mensaje.setPadding(0, 0, 0, dp(12));
            tarjeta.addView(mensaje);

            if (partidoEnRegistro == p.getPartidoId()) {
                crearFormularioResultado(p, partidos, tarjeta);
            } else {
                Button registrar = crearBoton("Registrar resultado", R.drawable.bg_button_secondary, R.color.white);
                registrar.setOnClickListener(v -> {
                    partidoEnRegistro = p.getPartidoId();
                    mostrarPartidos();
                });
                tarjeta.addView(registrar);
            }
        }
        // ESTADO FINALIZADO
        else if (p.getEstado() == EstadoPartido.FINALIZADO) {
            Resultado resultado = buscarResultado(p.getPartidoId());

            if (resultado != null) {
                TextView resultadoTexto = crearTexto("Resultado oficial: " + resultado.getGolesSeleccion1R() + " - " + resultado.getGolesSeleccion2R(), 16, true);
                resultadoTexto.setGravity(Gravity.CENTER);
                resultadoTexto.setTextColor(getColor(R.color.primary));
                resultadoTexto.setPadding(0, dp(5), 0, 0);
                tarjeta.addView(resultadoTexto);
            } else {
                TextView resultadoTexto = crearTexto("Resultado oficial no disponible.", 13, false);
                resultadoTexto.setGravity(Gravity.CENTER);
                resultadoTexto.setTextColor(getColor(R.color.text_secondary));
                tarjeta.addView(resultadoTexto);
            }
        }

        contenedor.addView(tarjeta);
    }

    /**
     * Construye y agrega a la tarjeta el formulario para registrar el
     * resultado oficial de un partido CERRADO: dos campos de goles y el
     * botón "Guardar resultado". Al presionarlo, valida los datos
     * ingresados (lanzando DatosIncompletosException si faltan o son
     * inválidos), crea el objeto Resultado, lo guarda en resultados.txt,
     * cambia el estado del partido a FINALIZADO y lo persiste en
     * partidos.txt.
     *
     * @param p el partido para el cual se registra el resultado
     * @param partidos la lista completa de partidos, reescrita al cambiar el estado
     * @param tarjeta el contenedor de la tarjeta donde se agrega el formulario
     */
    private void crearFormularioResultado(Partido p, ArrayList<Partido> partidos, LinearLayout tarjeta) {
        TextView titulo = crearTexto("Registrar resultado oficial", 14, true);
        titulo.setTextColor(getColor(R.color.text_primary));
        titulo.setPadding(0, 0, 0, dp(10));
        tarjeta.addView(titulo);

        LinearLayout marcador = new LinearLayout(this);
        marcador.setOrientation(LinearLayout.HORIZONTAL);
        marcador.setGravity(Gravity.CENTER);

        EditText goles1 = crearCampoGoles(p.getSeleccion1());
        EditText goles2 = crearCampoGoles(p.getSeleccion2());

        marcador.addView(goles1, peso());

        TextView separador = crearTexto("-", 22, true);
        separador.setGravity(Gravity.CENTER);
        marcador.addView(separador, new LinearLayout.LayoutParams(dp(45), dp(50)));

        marcador.addView(goles2, peso());
        tarjeta.addView(marcador);

        Button guardar = crearBoton("Guardar resultado", R.drawable.bg_button_primary, R.color.text_primary);
        guardar.setOnClickListener(v -> {
            try {
                String texto1 = goles1.getText().toString().trim();
                String texto2 = goles2.getText().toString().trim();

                if (texto1.isEmpty() || texto2.isEmpty()) {
                    throw new DatosIncompletosException("Debe ingresar los goles de ambas selecciones.");
                }

                int golesSeleccion1 = Integer.parseInt(texto1);
                int golesSeleccion2 = Integer.parseInt(texto2);

                if (golesSeleccion1 < 0 || golesSeleccion2 < 0) {
                    throw new DatosIncompletosException("Los goles deben ser mayores o iguales a cero.");
                }

                Resultado resultado = new Resultado(p.getPartidoId(), p.getPartidoId(), golesSeleccion1, golesSeleccion2);
                guardarResultado(resultado);

                p.setEstado(EstadoPartido.FINALIZADO);
                guardarPartidos(partidos);

                partidoEnRegistro = -1;
                mostrarPartidos();

            } catch (DatosIncompletosException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Los goles deben ser números válidos.", Toast.LENGTH_SHORT).show();
            }
        });

        tarjeta.addView(guardar);
    }

    /**
     * Construye el bloque visual de una selección: su bandera (buscada
     * dinámicamente por nombre mediante obtenerBandera()) y su nombre
     * debajo.
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
     * Crea un campo de texto numérico para ingresar los goles de una
     * selección, con teclado numérico forzado (InputType.TYPE_CLASS_NUMBER).
     *
     * @param pais selección a la que pertenece el campo, usada solo para accesibilidad
     * @return el EditText configurado para ingresar goles
     */
    private EditText crearCampoGoles(String pais) {
        EditText campo = new EditText(this);
        campo.setHint("0");
        campo.setGravity(Gravity.CENTER);
        campo.setTextSize(18);
        campo.setInputType(InputType.TYPE_CLASS_NUMBER);
        campo.setSingleLine(true);
        campo.setTextColor(getColor(R.color.text_primary));
        campo.setHintTextColor(getColor(R.color.text_secondary));
        campo.setBackgroundResource(R.drawable.bg_score);
        campo.setContentDescription("Goles de " + pais);
        return campo;
    }

    /**
     * Crea un botón con el texto, fondo y color de texto indicados,
     * reutilizado tanto para "Cerrar pronósticos" como para "Registrar
     * resultado" y "Guardar resultado".
     *
     * @param texto texto a mostrar en el botón
     * @param fondo recurso drawable de fondo del botón
     * @param colorTexto recurso de color para el texto del botón
     * @return el Button ya configurado
     */
    private Button crearBoton(String texto, int fondo, int colorTexto) {
        Button boton = new Button(this);
        boton.setText(texto);
        boton.setTextSize(15);
        boton.setTextColor(getColor(colorTexto));
        boton.setAllCaps(false); // CORREGIDO AQUÍ
        boton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        boton.setBackgroundResource(fondo);
        boton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));
        return boton;
    }

    /**
     * Busca en drawable un recurso de imagen cuyo nombre siga el
     * patrón flag_nombrepais (normalizado). Si no existe ninguna
     * bandera con ese nombre, retorna un ícono genérico de balón como
     * respaldo, para que la pantalla nunca falle por una imagen faltante.
     *
     * @param nombrePais nombre de la selección cuya bandera se busca
     * @return el id del recurso drawable de la bandera, o el ícono de respaldo
     */
    private int obtenerBandera(String nombrePais) {
        String nombre = "flag_" + normalizarNombrePais(nombrePais);
        int id = getResources().getIdentifier(nombre, "drawable", getPackageName());
        return id == 0 ? R.drawable.ic_sports_soccer : id;
    }

    /**
     * Convierte el nombre de un país a un formato válido para nombre de
     * recurso Android: minúsculas, sin tildes ni caracteres especiales,
     * y espacios reemplazados por guiones bajos (ej. "Corea del Sur" a
     * "corea_del_sur").
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
    private TextView crearTexto(String texto, int tamano, boolean negrita) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(tamano);
        tv.setTextColor(getColor(R.color.text_primary));
        if (negrita) {
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
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
     * Busca dentro de resultados.txt el resultado oficial correspondiente
     * a un partido, si ya fue registrado.
     *
     * @param idPartido id del partido cuyo resultado se busca
     * @return el Resultado encontrado, o null si el partido aún no tiene resultado registrado
     */
    private Resultado buscarResultado(int idPartido) {
        ArrayList<Resultado> resultados = cargarResultados();
        for (Resultado r : resultados) {
            if (r.getIdPartido() == idPartido) {
                return r;
            }
        }
        return null;
    }

    /**
     * Lee partidos.txt línea por línea (salteando el encabezado) y
     * construye la lista completa de partidos del torneo, convirtiendo
     * el texto de fase y estado a sus respectivos valores de enum con
     * valueOf().
     *
     * @return la lista completa de partidos, con su estado actual
     */
    private ArrayList<Partido> cargarPartidos() {
        ArrayList<Partido> lista = new ArrayList<>();
        try (InputStream is = abrirPartidos();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine(); // Saltar el encabezado
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
     * Reescribe por completo partidos.txt en el almacenamiento interno
     * con el estado actualizado de todos los partidos, incluyendo la
     * fila de encabezado. Se invoca cada vez que un partido cambia de
     * estado (al cerrar pronósticos o registrar un resultado), para que
     * el cambio se conserve en futuras ejecuciones de la aplicación.
     *
     * @param partidos la lista completa de partidos a persistir
     */
    private void guardarPartidos(ArrayList<Partido> partidos) {
        try (OutputStreamWriter writer = new OutputStreamWriter(openFileOutput("partidos.txt", MODE_PRIVATE))) {
            writer.write("idPartido;fase;fecha;horaUTC;estadio;seleccion1;seleccion2;estado\n");
            for (Partido p : partidos) {
                String linea = p.getPartidoId() + ";" + p.getFase() + ";" + p.getFecha() + ";" + p.getHora() + ";" +
                        p.getEstadio() + ";" + p.getSeleccion1() + ";" + p.getSeleccion2() + ";" + p.getEstado() + "\n";
                writer.write(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decide desde dónde leer partidos.txt: si ya existe una copia en
     * el almacenamiento interno (porque algún partido ya cambió de
     * estado), la usa; de lo contrario, lee la versión original de
     * assets.
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
     * Agrega una nueva línea a resultados.txt con el resultado oficial
     * de un partido, usando MODE_APPEND para no sobrescribir los
     * resultados registrados anteriormente.
     *
     * @param resultado el resultado oficial a guardar
     */
    private void guardarResultado(Resultado resultado) {
        try (OutputStreamWriter writer = new OutputStreamWriter(openFileOutput("resultados.txt", MODE_APPEND))) {
            String linea = resultado.getIdResultado() + ";" + resultado.getIdPartido() + ";" +
                    resultado.getGolesSeleccion1R() + ";" + resultado.getGolesSeleccion2R() + "\n";
            writer.write(linea);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lee resultados.txt desde el almacenamiento interno y construye la
     * lista de resultados oficiales ya registrados. Si el archivo aún
     * no existe (porque no se ha registrado ningún resultado todavía),
     * retorna una lista vacía en vez de fallar.
     *
     * @return la lista de resultados oficiales registrados hasta el momento
     */
    private ArrayList<Resultado> cargarResultados() {
        ArrayList<Resultado> lista = new ArrayList<>();
        java.io.File archivo = new java.io.File(getFilesDir(), "resultados.txt");

        if (!archivo.exists()) {
            return lista;
        }

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
     * regresa al menú principal del administrador.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void volver(View view) {
        finish();
    }
}