package com.espol.pronosticosmundial2026;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.espol.pronosticosmundial2026.modelo.Administrador;
import com.espol.pronosticosmundial2026.modelo.EstadoPartido;
import com.espol.pronosticosmundial2026.modelo.Fase;
import com.espol.pronosticosmundial2026.modelo.Participante;
import com.espol.pronosticosmundial2026.modelo.Partido;
import com.espol.pronosticosmundial2026.modelo.Pronostico;
import com.espol.pronosticosmundial2026.modelo.Resultado;
import com.espol.pronosticosmundial2026.modelo.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Pantalla exclusiva del administrador para recalcular los puntajes de
 * todos los participantes, comparando cada pronóstico registrado contra
 * el resultado oficial de los partidos ya FINALIZADOS, según las reglas
 * de puntuación del torneo (3, 2, 1 o 0 puntos). Es la pantalla con la
 * lógica de cálculo más compleja de la aplicación: recorre partidos,
 * usuarios y archivos de pronósticos serializados de forma anidada,
 * actualiza tanto el puntaje total de cada participante como los puntos
 * individuales de cada pronóstico, y persiste ambos resultados.
 *
 * @author Jair Cárdenas
 */
public class ActualizarPuntajesActivity extends AppCompatActivity {

    /**
     * Inicializa la pantalla, infla su layout y configura el manejo de
     * los márgenes del sistema. Toda la lógica de cálculo se dispara
     * desde el botón "Actualizar puntajes", no desde aquí.
     *
     * @param savedInstanceState estado previamente guardado de la actividad, si existe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actualizar_puntajes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Calcula los puntos obtenidos por un pronóstico según las reglas
     * del torneo, comparando los goles pronosticados contra los goles
     * reales: 3 puntos si el marcador es exacto; 2 puntos si ambos
     * marcadores fueron empate (sin importar el marcador exacto); 2
     * puntos si se acertó el ganador y la diferencia de goles; 1 punto
     * si solo se acertó el ganador; 0 puntos en cualquier otro caso.
     *
     * @param golesPron1 goles pronosticados para la primera selección
     * @param golesPron2 goles pronosticados para la segunda selección
     * @param golesReal1 goles reales anotados por la primera selección
     * @param golesReal2 goles reales anotados por la segunda selección
     * @return los puntos obtenidos por el pronóstico (0, 1, 2 o 3)
     */
    private int calcularPuntos(int golesPron1, int golesPron2, int golesReal1, int golesReal2) {
        if (golesPron1 == golesReal1 && golesPron2 == golesReal2) {
            return 3;
        }

        boolean empatePronosticado = golesPron1 == golesPron2;
        boolean empateReal = golesReal1 == golesReal2;

        if (empatePronosticado && empateReal) {
            return 2;
        }

        int diferenciaPron = golesPron1 - golesPron2;
        int diferenciaReal = golesReal1 - golesReal2;

        boolean mismoGanador = (diferenciaPron > 0 && diferenciaReal > 0) || (diferenciaPron < 0 && diferenciaReal < 0);

        if (mismoGanador && diferenciaPron == diferenciaReal) {
            return 2;
        }

        if (mismoGanador) {
            return 1;
        }

        return 0;
    }

    /**
     * Controlador de evento del botón "Actualizar puntajes". Reinicia a
     * 0 el puntaje de todos los participantes, y recorre cada partido
     * FINALIZADO con su resultado oficial registrado: por cada
     * participante, carga su archivo de pronósticos serializado de esa
     * fase, calcula los puntos de su pronóstico con calcularPuntos(),
     * actualiza el campo puntosObtenidos del propio objeto Pronostico
     * (reescribiendo el archivo .dat) y suma esos puntos al total
     * acumulado del participante en un HashMap. Al finalizar, persiste
     * los puntajes actualizados en participantes.txt y notifica al
     * usuario con un Toast.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void actualizarPuntajes(View view) {
        ArrayList<Usuario> usuarios = cargarUsuarios();
        ArrayList<Partido> partidos = cargarPartidos();
        ArrayList<Resultado> resultados = cargarResultados();

        HashMap<Integer, Integer> puntajesPorUsuario = new HashMap<>();
        for (Usuario u : usuarios) {
            if (u instanceof Participante) {
                puntajesPorUsuario.put(u.getId(), 0);
            }
        }

        for (Partido p : partidos) {
            if (p.getEstado() != EstadoPartido.FINALIZADO) continue;

            Resultado resultadoEncontrado = null;
            for (Resultado r : resultados) {
                if (r.getIdPartido() == p.getPartidoId()) {
                    resultadoEncontrado = r;
                    break;
                }
            }
            if (resultadoEncontrado == null) continue;

            for (Usuario u : usuarios) {
                if (!(u instanceof Participante)) continue;

                String nombreArchivo = "pronostico_" + u.getId() + "_" + p.getFase().name() + ".dat";
                ArrayList<Pronostico> pronosticos = cargarPronosticosDeArchivo(nombreArchivo);

                for (Pronostico pr : pronosticos) {
                    if (pr.getIdPartidoP() == p.getPartidoId()) {
                        int puntos = calcularPuntos(pr.getGolesSeleccion1P(), pr.getGolesSeleccion2P(),
                                resultadoEncontrado.getGolesSeleccion1R(), resultadoEncontrado.getGolesSeleccion2R());

                        pr.setPuntosObtenidosP(puntos);

                        int actual = puntajesPorUsuario.get(u.getId());
                        puntajesPorUsuario.put(u.getId(), actual + puntos);
                    }
                }

                try (ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(nombreArchivo, MODE_PRIVATE))) {
                    oos.writeObject(pronosticos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        guardarParticipantes(puntajesPorUsuario);
        Toast.makeText(this, "Puntajes actualizados correctamente.", Toast.LENGTH_SHORT).show();
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
        if(archivoInterno.exists()){
            return openFileInput("partidos.txt");
        }
        else{
            return getAssets().open("partidos.txt");
        }
    }

    /**
     * Lee resultados.txt desde el almacenamiento interno y construye la
     * lista de resultados oficiales ya registrados, necesaria para
     * comparar contra los pronósticos. Si el archivo aún no existe,
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
                int idResultado = Integer.parseInt(datos[0]);
                int idPartido = Integer.parseInt(datos[1]);
                int golesSeleccion1 = Integer.parseInt(datos[2]);
                int golesSeleccion2 = Integer.parseInt(datos[3]);
                lista.add(new Resultado(idResultado, idPartido, golesSeleccion1, golesSeleccion2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Lee partidos.txt línea por línea (salteando el encabezado) y
     * construye la lista completa de partidos del torneo, necesaria
     * para identificar cuáles ya están FINALIZADOS.
     *
     * @return la lista completa de partidos con su estado actual
     */
    private ArrayList<Partido> cargarPartidos(){
        ArrayList<Partido> listaPartidos = new ArrayList<>();

        try (InputStream is = abrirPartidos();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine(); // salta el encabezado
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datosParticionados = linea.strip().split(";");

                int id = Integer.parseInt(datosParticionados[0]);
                Fase fase = Fase.valueOf(datosParticionados[1]);
                String fecha = datosParticionados[2];
                String hora = datosParticionados[3];
                String estadio = datosParticionados[4];
                String seleccion1 = datosParticionados[5];
                String seleccion2 = datosParticionados[6];
                EstadoPartido estado = EstadoPartido.valueOf(datosParticionados[7]);

                Partido p = new Partido(id, fase, fecha, hora, estadio, seleccion1, seleccion2, estado);

                listaPartidos.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listaPartidos;
    }

    /**
     * Lee y deserializa el archivo .dat indicado, reconstruyendo la
     * lista de pronósticos guardados por un participante para una fase
     * específica.
     *
     * @param nombreArchivo nombre del archivo .dat a leer
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
     * Lee usuarios.txt desde assets y construye la lista completa de
     * usuarios del sistema, distinguiendo entre Participante y
     * Administrador, necesaria para saber a quiénes recalcular el
     * puntaje.
     *
     * @return la lista de todos los usuarios del sistema
     */
    private ArrayList<Usuario> cargarUsuarios(){
        ArrayList<Usuario> listaUsuarios = new ArrayList<>();

        try (InputStream is = getAssets().open("usuarios.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine(); // salta el encabezado
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datosParticionados = linea.strip().split(";");

                int id = Integer.parseInt(datosParticionados[0]);
                String username = datosParticionados[1];
                String password = datosParticionados[2];
                String nombresCompletos = datosParticionados[3];
                String tipo = datosParticionados[4];

                if (tipo.equals("PARTICIPANTE")){
                    Participante p = new Participante(id, username, password, nombresCompletos, 0);
                    listaUsuarios.add(p);
                }
                else if(tipo.equals("ADMINISTRADOR")){
                    Administrador a = new Administrador(id, username, password, nombresCompletos, "Administrador");
                    listaUsuarios.add(a);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listaUsuarios;
    }

    /**
     * Reescribe por completo participantes.txt en el almacenamiento
     * interno, incluyendo la fila de encabezado, con el puntaje total
     * ya recalculado de cada participante. Se invoca al final de
     * actualizarPuntajes(), para que la Tabla de Posiciones refleje los
     * nuevos puntajes en futuras consultas.
     *
     * @param puntajes mapa de id de participante a su puntaje total recalculado
     */
    private void guardarParticipantes(HashMap<Integer, Integer> puntajes) {
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(openFileOutput("participantes.txt", MODE_PRIVATE))) {
            writer.write("idUsuario;puntajeAcumulado\n");
            for (Integer idParticipante : puntajes.keySet()) {
                int puntaje = puntajes.get(idParticipante);
                String linea = idParticipante + ";" + puntaje + "\n";
                writer.write(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Controlador de evento del botón "Volver". Cierra esta pantalla y
     * regresa al menú principal del administrador.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void volver(View view){
        finish();
    }
}