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
 * Pantalla del menú principal para usuarios de tipo Administrador.
 * Muestra el nombre del administrador que inició sesión (recibido
 * mediante Intent desde LoginActivity) y ofrece acceso a las dos
 * opciones exclusivas de este rol: Administrar partidos y Actualizar
 * puntajes, además de la opción de salir de la aplicación.
 *
 * @author Jair Cárdenas
 */
public class MenuAdministradorActivity extends AppCompatActivity {

    /**
     * Inicializa la pantalla: infla el layout, recupera el nombre del
     * administrador enviado por LoginActivity mediante
     * getIntent().getStringExtra(), y lo muestra en la parte superior
     * de la pantalla.
     *
     * @param savedInstanceState estado previamente guardado de la actividad, si existe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_administrador);
        String nombre = getIntent().getStringExtra("nombreUsuario");
        TextView tvNombre = findViewById(R.id.tvNombre);
        tvNombre.setText("Bienvenido, "+nombre);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Controlador de evento del botón "Administrar partidos". Navega
     * hacia AdministrarPartidosActivity mediante un Intent.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void mostrarAdministrarPartidos(View view){
        Intent intent = new Intent(this, AdministrarPartidosActivity.class);
        startActivity(intent);
    }

    /**
     * Controlador de evento del botón "Actualizar puntajes". Navega
     * hacia ActualizarPuntajesActivity mediante un Intent.
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void mostrarActualizarPuntajes(View view){
        Intent intent = new Intent(this, ActualizarPuntajesActivity.class);
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