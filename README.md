# Taller 1 - Hello Android

## Información de los Estudiantes
- **Nombre:** Cristofer Danilo Muñoz Luna 
- **Nombre:** Karen Sirley Acosta Beltran
- **Nombre:** Emersson Andrey Forero Jerez
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


## Taller 2 - Arquitectura MVVM

### Respuestas a Preguntas Conceptuales

---

### **1. ¿Qué problema resuelve el ViewModel en Android?**

El **ViewModel** resuelve principalmente la **gestión de datos ante cambios de configuración** (como la rotación de pantalla) y la **separación de responsabilidades** dentro de la arquitectura.

**Problemas que soluciona:**

- **Pérdida de datos:** Cuando el dispositivo rota, la Activity se destruye y recrea. El ViewModel mantiene los datos intactos.  
- **Gestión del ciclo de vida:** Se elimina automáticamente cuando ya no es necesario, evitando memory leaks.  
- **Desacoplamiento:** Separa la lógica de negocio de la UI, haciendo la vista más simple.  
- **Compartición de datos:** Permite que varios fragments compartan información mediante `activityViewModels()`.

---

### **2. ¿Por qué LiveData es "lifecycle-aware" y qué beneficio trae?**

**LiveData** es *lifecycle-aware* porque respeta el ciclo de vida de Activities y Fragments, actualizando la UI **solo cuando están activos** (`STARTED` o `RESUMED`).

**Beneficios:**

- **Evita memory leaks:** Elimina automáticamente observadores cuando la vista se destruye.  
- **Previene crashes:** No envía datos cuando la UI está en segundo plano.  
- **Actualización automática:** La UI se actualiza cuando los datos cambian.  
- **Optimización:** Evita trabajo innecesario cuando la UI no es visible.

---

### **3. Explica con tus propias palabras el flujo de datos en MVVM**

El flujo de datos en **MVVM** es **unidireccional y reactivo**.

**Flujo:**

- **View (Fragment / Activity):** El usuario interactúa con la UI y notifica al ViewModel.  
- **ViewModel:** Procesa la acción, aplica lógica de negocio y actualiza los datos.  
- **Model (Repository + Data):** Gestiona los datos y responde al ViewModel.  
- **View:** Observa los cambios mediante LiveData y actualiza la UI automáticamente.

---

### **4. ¿Qué ventaja tiene usar Fragments vs múltiples Activities?**

**Ventajas:**

- **Reutilización:** Un Fragment puede usarse en varias Activities.  
- **Navegación fluida:** Permiten transiciones más suaves y manejo del back stack.  
- **Compartición de datos:** Pueden compartir ViewModel fácilmente.  
- **Menor consumo:** Son más ligeros que las Activities.  
- **UI adaptable:** Permiten diseños más flexibles (ej: tablets).

---

### **5. ¿Cómo ayuda el Repository Pattern a la arquitectura?**

El **Repository Pattern** actúa como una **fuente única de verdad** (*single source of truth*) y abstrae la fuente de datos.

**Beneficios:**

- **Abstracción:** El ViewModel no necesita saber de dónde vienen los datos.  
- **Centralización:** Toda la lógica de datos está en un solo lugar.  
- **Testing:** Permite usar mocks fácilmente.  
- **Caché:** Puede gestionar datos locales y remotos.  
- **Separación de responsabilidades:**
  - ViewModel: lógica de negocio  
  - Repository: acceso a datos  
  - DataSource: origen de datos