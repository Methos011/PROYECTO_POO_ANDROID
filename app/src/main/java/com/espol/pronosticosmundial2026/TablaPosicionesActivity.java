package com.espol.pronosticosmundial2026;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.espol.pronosticosmundial2026.modelo.Administrador;
import com.espol.pronosticosmundial2026.modelo.Participante;
import com.espol.pronosticosmundial2026.modelo.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Pantalla que muestra la tabla de posiciones de todos los participantes
 * registrados en el sistema, ordenada de mayor a menor puntaje (y
 * alfabéticamente en caso de empate) gracias a que Participante
 * implementa Comparable. La tabla se genera dinámicamente en tiempo de
 * ejecución dentro de un TableLayout, agregando una fila de encabezado
 * y una fila por cada participante, destacando el podio (1°, 2°, 3°
 * lugar) con emojis de medalla.
 *
 * @author Sebastian Espinoza
 */
public class TablaPosicionesActivity extends AppCompatActivity {

    /**
     * Inicializa la pantalla: infla el layout, carga la lista de
     * participantes con sus puntajes actualizados, la ordena con
     * Collections.sort() (que internamente usa el compareTo() de
     * Participante), y construye la tabla completa llamando a
     * crearEncabezado() y crearFila() por cada participante.
     *
     * @param savedInstanceState estado previamente guardado de la actividad, si existe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tabla_posiciones);

        ArrayList<Participante> participantes = cargarParticipantes();
        Collections.sort(participantes);

        TableLayout tablaLayout = findViewById(R.id.tablaLayout);
        crearEncabezado(tablaLayout);

        int posicion = 1;
        for (Participante p : participantes) {
            crearFila(tablaLayout, posicion, p);
            posicion++;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Construye y agrega la fila de encabezado de la tabla (Pos.,
     * Participante, Pts.), creada de forma dinámica con TableRow y
     * TextView en vez de estar fija en el XML.
     *
     * @param tabla el TableLayout donde se agrega la fila
     */
    private void crearEncabezado(TableLayout tabla) {
        TableRow fila = new TableRow(this);
        fila.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView posicion = crearCelda("Pos.", 13, true);
        TextView nombre = crearCelda("Participante", 13, true);
        TextView puntos = crearCelda("Pts.", 13, true);

        puntos.setGravity(Gravity.CENTER);

        fila.addView(posicion, parametrosCelda());
        fila.addView(nombre, parametrosCelda());
        fila.addView(puntos, parametrosCelda());

        tabla.addView(fila);
    }

    /**
     * Construye y agrega una fila con los datos de un participante:
     * posición, nombre completo y puntaje. Si la posición es 1, 2 o 3,
     * reemplaza el número por un emoji de medalla (🥇🥈🥉) para
     * destacar visualmente el podio.
     *
     * @param tabla el TableLayout donde se agrega la fila
     * @param posicion la posición del participante dentro de la tabla ordenada
     * @param participante el participante cuyos datos se muestran en la fila
     */
    private void crearFila(TableLayout tabla, int posicion, Participante participante) {
        TableRow fila = new TableRow(this);
        fila.setPadding(dp(12), dp(14), dp(12), dp(14));

        TextView tvPosicion = crearCelda(String.valueOf(posicion), 15, true);
        TextView tvNombre = crearCelda(participante.getNombreCompleto(), 15, true);
        TextView tvPuntos = crearCelda(String.valueOf(participante.getPuntajeAcumulado()), 16, true);

        tvPosicion.setGravity(Gravity.CENTER);
        tvPuntos.setGravity(Gravity.CENTER);

        if (posicion == 1) {
            tvPosicion.setText("🥇");
        } else if (posicion == 2) {
            tvPosicion.setText("🥈");
        } else if (posicion == 3) {
            tvPosicion.setText("🥉");
        }

        fila.addView(tvPosicion, parametrosCelda());
        fila.addView(tvNombre, parametrosCelda());
        fila.addView(tvPuntos, parametrosCelda());

        tabla.addView(fila);
    }

    /**
     * Crea un TextView configurado con el texto, tamaño y color
     * corporativo indicados, usado como celda dentro de las filas de
     * la tabla.
     *
     * @param texto contenido de la celda
     * @param tamano tamaño de fuente en sp
     * @param negrita si el texto debe mostrarse en negrita
     * @return el TextView ya configurado, listo para agregarse a una fila
     */
    private TextView crearCelda(String texto, int tamano, boolean negrita) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(tamano);
        tv.setTextColor(getColor(R.color.text_primary));

        if (negrita) {
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return tv;
    }

    /** @return los parámetros de margen usados por cada celda de la tabla */
    private TableRow.LayoutParams parametrosCelda() {
        TableRow.LayoutParams parametros = new TableRow.LayoutParams();
        parametros.setMargins(dp(4), dp(4), dp(4), dp(4));
        return parametros;
    }

    /** @return el valor en píxeles equivalente al valor en dp, según la densidad de pantalla */
    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    /**
     * Cruza los datos de participantes.txt (id y puntaje) con los datos
     * de usuarios.txt (nombre, credenciales) para construir la lista
     * completa de participantes con su puntaje real. Lee siempre desde
     * el almacenamiento interno si ya existe una copia actualizada por
     * ActualizarPuntajesActivity, o desde assets si es la primera vez
     * que se ejecuta la aplicación.
     *
     * @return la lista de participantes con sus puntajes actuales
     */
    private ArrayList<Participante> cargarParticipantes() {
        ArrayList<Participante> listaParticipantes = new ArrayList<>();
        ArrayList<Usuario> usuarios = cargarUsuarios();

        try (InputStream is = abrirParticipantes();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine();
            String linea;

            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.strip().split(";");
                int id = Integer.parseInt(datos[0]);
                int puntaje = Integer.parseInt(datos[1]);

                for (Usuario u : usuarios) {
                    if (u instanceof Participante && u.getId() == id) {
                        Participante p = (Participante) u;
                        Participante nuevo = new Participante(
                                p.getId(),
                                p.getUsername(),
                                p.getPassword(),
                                p.getNombreCompleto(),
                                puntaje
                        );
                        listaParticipantes.add(nuevo);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listaParticipantes;
    }

    /**
     * Decide desde dónde leer participantes.txt: si ya existe una copia
     * en el almacenamiento interno (generada al actualizar puntajes),
     * la usa; de lo contrario, lee la versión original de assets.
     *
     * @return el InputStream del archivo participantes.txt correspondiente
     * @throws IOException si ocurre un error al abrir el archivo
     */
    private InputStream abrirParticipantes() throws IOException {
        java.io.File archivo = new java.io.File(getFilesDir(), "participantes.txt");

        if (archivo.exists()) {
            return openFileInput("participantes.txt");
        } else {
            return getAssets().open("participantes.txt");
        }
    }

    /**
     * Lee usuarios.txt desde assets y construye la lista completa de
     * usuarios del sistema, distinguiendo entre Participante y
     * Administrador según el campo tipo.
     *
     * @return la lista de todos los usuarios del sistema
     */
    private ArrayList<Usuario> cargarUsuarios() {
        ArrayList<Usuario> listaUsuarios = new ArrayList<>();

        try (InputStream is = getAssets().open("usuarios.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine();
            String linea;

            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.strip().split(";");
                int id = Integer.parseInt(datos[0]);
                String username = datos[1];
                String password = datos[2];
                String nombres = datos[3];
                String tipo = datos[4];

                if (tipo.equals("PARTICIPANTE")) {
                    Participante p = new Participante(id, username, password, nombres, 0);
                    listaUsuarios.add(p);
                } else if (tipo.equals("ADMINISTRADOR")) {
                    Administrador a = new Administrador(id, username, password, nombres, "Administrador");
                    listaUsuarios.add(a);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listaUsuarios;
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
