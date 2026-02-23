# Taller 1 - Hello Android

## Información del Estudiante
- **Nombre:** Cristofer Danilo Muñoz Luna  
- **Código:** *(agrega tu código estudiantil aquí)*  
- **Fecha:** 24/02/2026  

---

## Respuestas

### **1. Función del AndroidManifest.xml**
El archivo **`AndroidManifest.xml`** es el archivo principal de configuración de una aplicación Android.  
Su función principal es indicarle al sistema Android **cómo es y cómo debe funcionar la aplicación**.

En este archivo se definen aspectos como:
- Los **permisos** de la aplicación  
- El **nombre del paquete**  
- La **versión mínima de Android**  
- La **actividad principal** que se ejecuta al iniciar la app  

Sin este archivo, **la aplicación no puede ejecutarse**.

---

### **2. Diferencia entre `activity_main.xml` y `MainActivity.kt`**
La diferencia principal es la función que cumple cada archivo:

- **`activity_main.xml`** se encarga del **diseño visual** de la aplicación, como botones, textos y layouts.
- **`MainActivity.kt`** contiene la **lógica del programa**, es decir, las acciones, eventos y el comportamiento de los elementos.

**Ejemplo:**  
El archivo XML puede crear un botón, pero si se desea que al presionarlo aumente un contador en 1, esa lógica se programa en el archivo `.kt`.

---

### **3. Gestión de recursos en Android**
Android maneja los recursos limitados del dispositivo móvil (memoria, batería y CPU) mediante diferentes mecanismos, entre ellos:

- **Ciclo de vida de las actividades:** pausa o cierra aplicaciones inactivas.  
- **Liberación automática de memoria.**  
- **Control de aplicaciones en segundo plano.**  
- **Optimización de batería:** limita procesos innecesarios.  
  - **Doze Mode:** se activa cuando el teléfono está quieto, con la pantalla apagada y sin uso, restringiendo tareas, sincronizaciones y acceso a red.  
- **Prioridad de procesos:** las aplicaciones visibles tienen mayor prioridad que las que están en segundo plano.

Esto permite que el dispositivo funcione de forma eficiente y que la batería dure más tiempo.

---

### **4. Aplicaciones famosas que usan Kotlin**
Algunas aplicaciones muy conocidas desarrolladas total o parcialmente con Kotlin son:

- **Instagram**  
- **Pinterest**  
- **Trello**
