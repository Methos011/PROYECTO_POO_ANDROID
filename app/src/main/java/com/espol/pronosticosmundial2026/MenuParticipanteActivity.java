package com.espol.pronosticosmundial2026;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Pantalla del menú principal para usuarios de tipo Participante.
 * Muestra el nombre del participante que inició sesión y ofrece acceso
 * a sus 3 opciones: Tabla de posiciones, Pronósticos y Mis pronósticos,
 * además de la opción de salir. A diferencia de MenuAdministradorActivity,
 * también conserva el id del usuario, ya que las pantallas de Pronósticos
 * y Mis pronósticos lo necesitan para identificar de quién son los
 * pronósticos guardados.
 *
 * @author Jair Cárdenas
 */
public class MenuParticipanteActivity extends AppCompatActivity {
    private int idUsuario;

    /**
     * Inicializa la pantalla: infla el layout, recupera el nombre y el
     * id del participante enviados por LoginActivity mediante Intent, y
     * muestra el nombre en la parte superior de la pantalla. El id se
     * guarda como atributo de instancia porque va a ser reenviado a
     * otras Activities más adelante, en los métodos que navegan a
     * Pronósticos y Mis pronósticos.
     *
     * @param savedInstanceState estado previamente guardado de la actividad, si existe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_participante);
        String nombre = getIntent().getStringExtra("nombreUsuario");
        idUsuario = getIntent().getIntExtra("idUsuario", -1);
        TextView tvNombre = findViewById(R.id.tvNombre);
        tvNombre.setText("Bienvenido, "+nombre);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Controlador de evento del botón "Tabla de posiciones". Navega
     * hacia TablaPosicionesActivity mediante un Intent.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void mostrarTablaPosiciones(View view){
        Intent intent = new Intent(this, TablaPosicionesActivity.class);
        startActivity(intent);
    }

    /**
     * Controlador de evento del botón "Pronósticos". Navega hacia
     * PronosticosActivity mediante un Intent, reenviando el id del
     * participante para que esa pantalla sepa de quién son los
     * pronósticos que se van a registrar.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void mostrarPronosticos(View view){
        Intent intent = new Intent(this, PronosticosActivity.class);
        intent.putExtra("idUsuario", idUsuario);
        startActivity(intent);
    }

    /**
     * Controlador de evento del botón "Mis pronósticos". Navega hacia
     * MisPronosticosActivity mediante un Intent, reenviando el id del
     * participante para que esa pantalla sepa de quién son los
     * pronósticos que debe mostrar.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void mostrarMisPronosticos(View view){
        Intent intent = new Intent(this, MisPronosticosActivity.class);
        intent.putExtra("idUsuario", idUsuario);
        startActivity(intent);
    }

    /**
     * Controlador de evento del botón "Salir". Cierra por completo la
     * aplicación (todas las Activities de la pila), usando finishAffinity()
     * en vez de finish(), tal como exige el enunciado.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void salir(View view){
        finishAffinity();
    }
}