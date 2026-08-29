Pronósticos Mundial 2026 

Aplicación móvil desarrollada en Android para gestionar pronósticos de los partidos de la Copa Mundial de la FIFA 2026. La plataforma permite a los fanáticos registrar sus predicciones, competir en una tabla de posiciones global y hacer seguimiento de los resultados oficiales del torneo. 

El sistema cuenta con un modelo de roles que separa la experiencia de los jugadores y las herramientas de gestión de los administradores.

## Características Principales

### Autenticación y Perfiles
* **Inicio de Sesión Seguro:** Sistema de autenticación que identifica automáticamente si el usuario es un Participante o un Administrador, redirigiéndolo a su menú correspondiente.

###  Módulo del Participante
* **Registro de Pronósticos:** Los usuarios pueden explorar los partidos por fase (Fase de grupos, Octavos, Cuartos, etc.) e ingresar su predicción de goles para cada selección. Solo se permiten pronósticos en partidos con estado "ABIERTO".
* **Mis Pronósticos:** Un panel personal donde el usuario puede revisar su historial de predicciones, compararlas con los resultados oficiales (una vez finalizado el partido) y visualizar los puntos ganados en cada encuentro.
* **Tabla de Posiciones (Ranking):** Clasificación general en tiempo real de todos los participantes, ordenada de mayor a menor según el puntaje acumulado.

###  Módulo del Administrador
* **Gestión de Estados de Partidos:** Control total sobre el flujo de los encuentros. Los administradores pueden cambiar el estado de los partidos de *ABIERTO* a *CERRADO* (bloqueando nuevos pronósticos) y finalmente a *FINALIZADO*.
* **Registro de Resultados Oficiales:** Capacidad para ingresar los marcadores finales reales de los partidos una vez que concluyen.
* **Motor de Actualización de Puntajes:** Herramienta automatizada que cruza los resultados oficiales con los pronósticos de todos los usuarios para recalcular y actualizar la tabla de posiciones general.

##  Sistema de Puntuación
El cálculo de puntos tras cada partido finalizado se basa en las siguientes reglas:
* **3 puntos:** Acertar el marcador exacto del partido.
* **2 puntos:** Acertar el ganador y la diferencia de goles exacta.
* **2 puntos:** Acertar que el partido terminará en empate, sin acertar el marcador exacto.
* **1 punto:** Acertar únicamente al ganador del partido.
* **0 puntos:** No acertar el resultado.

##  Tecnologías Utilizadas
* **Lenguaje:** Java
* **Entorno de Desarrollo:** Android Studio
* **Persistencia de Datos:** Manejo de archivos de texto local (`.txt`) para datos
