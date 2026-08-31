package com.espol.pronosticosmundial2026;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.espol.pronosticosmundial2026.excepciones.CredencialesInvalidasException;
import com.espol.pronosticosmundial2026.modelo.Administrador;
import com.espol.pronosticosmundial2026.modelo.Participante;
import com.espol.pronosticosmundial2026.modelo.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Pantalla de inicio de sesión de la aplicación, y punto de entrada
 * (Launcher Activity) del sistema. Permite al usuario ingresar su
 * nombre de usuario y contraseña, valida las credenciales contra los
 * datos cargados desde usuarios.txt, e identifica el tipo de usuario
 * autenticado (Participante o Administrador) para redirigirlo al menú
 * principal correspondiente, enviando su id y nombre completo mediante
 * un Intent.
 *
 * @author David Delgado
 */
public class LoginActivity extends AppCompatActivity {
    private EditText editUser;
    private EditText editPass;
    private Button botonSesion;

    /**
     * Inicializa la pantalla de login: infla el layout, obtiene las
     * referencias a los campos de usuario, contraseña y el botón de
     * inicio de sesión mediante findViewById(), y configura el manejo
     * de los márgenes del sistema (barra de estado y navegación).
     *
     * @param savedInstanceState estado previamente guardado de la actividad, si existe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        editUser = findViewById(R.id.editUser);
        editPass = findViewById(R.id.editPass);
        botonSesion = findViewById(R.id.botonSesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contenedor_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Controlador de evento asociado al botón "Iniciar sesión" mediante
     * el atributo android:onClick del XML. Lee el usuario y la contraseña
     * ingresados, busca una coincidencia dentro de la lista de usuarios
     * cargada desde el archivo, y según el resultado: lanza
     * CredencialesInvalidasException si no hay coincidencia, o identifica
     * el tipo de usuario (usando instanceof) para navegar al menú de
     * Participante o de Administrador, pasando el id y el nombre completo
     * mediante Intent.putExtra().
     *
     * @param view la vista (botón) que disparó el evento
     */
    public void iniciarSesion(View view) {
        String username = editUser.getText().toString();
        String password = editPass.getText().toString();
        ArrayList<Usuario> usuarios = cargarUsuarios();

        Usuario usuarioEncontrado = null;

        for(Usuario u: usuarios){
            if((u.getUsername().equals(username)) && (u.getPassword().equals(password))){
                usuarioEncontrado = u;
                break;
            }
        }

        try {
            if(usuarioEncontrado == null){
                throw new CredencialesInvalidasException("El usuario o la contraseña son incorrectos.");
            } else {
                if (usuarioEncontrado instanceof Participante){
                    Intent intent = new Intent(this, MenuParticipanteActivity.class);
                    intent.putExtra("nombreUsuario",usuarioEncontrado.getNombreCompleto());
                    intent.putExtra("idUsuario", usuarioEncontrado.getId());
                    startActivity(intent);
                }
                else if (usuarioEncontrado instanceof  Administrador){
                    Intent intent = new Intent(this, MenuAdministradorActivity.class);
                    intent.putExtra("nombreUsuario",usuarioEncontrado.getNombreCompleto());
                    startActivity(intent);
                }
            }
        } catch(CredencialesInvalidasException e){
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lee el archivo usuarios.txt desde la carpeta assets (archivo de
     * solo lectura, nunca se modifica) y construye la lista completa de
     * usuarios del sistema. Por cada línea, separa los campos por punto
     * y coma, salta la fila de encabezado, y según el tipo de usuario
     * crea un objeto Participante (con puntaje inicial en 0) o un
     * Administrador (consultando su cargo real mediante
     * cargarCargosAdministradores()).
     *
     * @return la lista de todos los usuarios (participantes y administradores) del sistema
     */
    private ArrayList<Usuario> cargarUsuarios(){
        ArrayList<Usuario> listaUsuarios = new ArrayList<>();
        HashMap<Integer, String> cargos = cargarCargosAdministradores();

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
                    String cargo = cargos.getOrDefault(id, "Administrador");
                    Administrador a = new Administrador(id, username, password, nombresCompletos, cargo);
                    listaUsuarios.add(a);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listaUsuarios;
    }

    /**
     * Lee el archivo administradores.txt desde assets y construye un
     * mapa que asocia el id de cada administrador con su cargo. Se usa
     * como apoyo dentro de cargarUsuarios(), para que cada objeto
     * Administrador se construya con su cargo real en vez de un valor
     * fijo.
     *
     * @return un mapa de id de administrador a su cargo correspondiente
     */
    private HashMap<Integer, String> cargarCargosAdministradores(){
        HashMap<Integer, String> cargos = new HashMap<>();
        try (InputStream is = getAssets().open("administradores.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine(); // salta el encabezado
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.strip().split(";");
                int id = Integer.parseInt(datos[0]);
                String cargo = datos[1];
                cargos.put(id, cargo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cargos;
    }
}