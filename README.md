Taller 1 - Hello Wordl
Cristofer Danilo Muñoz Luna

24/02/2026
1. ¿Cuál es la función del archivo AndroidManifest.xml?
    Es el archivo principal de configuracion de una app de android. Su funcion principal es decirle al sistema android
   como es y debe funcionar la aplicacion, en este se definen permisos, nombre, version minima, la actividad principal
   que se abre al iniciar, sin este archivo la app no puede ejecutarse.
2. ¿Que diferencia existe entre activity_main.xml y MainActivity.kt?
     La mayor diferencia es que uno se encarga del diseño visual, es decir, los botones, los textos, layouts etc,
     mientras el otro contiene la logica del programa, es decir, eventos, acciones y codigo de los elementos.
     Un  ejemplo de este seria el XML crea un boton, si queremos que cada vez que se oprima el boton aumente un contador en 1,
     esa parte logica se realiza en el archivo .kt.
3. ¿Como maneja Android los recursos limitados del dispositivo movil?
      Andorid maneja los recursos(memoria, bateria,CPU) meidante:
   *Ciclo de vida de actividades
     Cierra o pausa actividades inactivas.
   *Liberacion automatica de memoria
   *Apps en segundo plano controladas
   *Optimizacion de bateria
      Limita procesos inncesarios
     -Doze Mode: Android detecta cuando el telefono esta quieto, la pantalla apagada y sin usarse,
                 esto permite detener tareas, bloquea sincronizaciones, limita acceso a red.
   *Prioridad de procesos
     La app visible tiene mayor prioridad que las que estan en segundo plano.
   Esto evita que el telefono se vuelva lento o se quede sin bateria rapido.
4.  Mencione 3 aplicaciones famosas que utilizan Kotlin
    Algunas aplicaciones muy conocidad desarrolladas toal o parcialmente con kotil son:
    -Instragram
    -Pinterest
    -Trello 
