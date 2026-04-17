# Taller 3 - Lista de Tareas con Fragments, Persistencia y Recordatorios

## Estudiante
- **Nombre:** Cristofer Danilo Muñoz Luna
- **Taller:** 3 - Lista de Tareas con Fragments
- **Fecha:** Abril 2026

## Objetivos Implementados

### 1. Arquitectura Single Activity + Fragments
- **MainActivity** como host principal
- **TaskListFragment** para mostrar lista de tareas
- **TaskDetailFragment** para crear/editar tareas
- Navegación mediante Navigation Component

### 2. Interfaz de Usuario
- **fragment_task_list.xml**: Lista con RecyclerView y FAB
- **fragment_task_detail.xml**: Formulario con campos de entrada
- **item_task.xml**: Diseño individual para cada tarea
- TaskAdapter con DiffUtil para rendimiento optimizado

### 3. Persistencia de Datos
- **TaskRepository** con SharedPreferences y JSON
- **Task.kt** como modelo de datos
- Las tareas persisten al cerrar y reabrir la app

### 4. Sistema de Recordatorios
- **TaskReminderReceiver** para notificaciones locales
- **AlarmManager** para programar recordatorios
- **AlarmUtils** para gestión de alarmas
- Permisos configurados en AndroidManifest.xml

### 5. Arquitectura MVVM
- **TaskListViewModel** y **TaskDetailViewModel**
- Separación de lógica de UI y negocio
- LiveData para observación de cambios

## Características Implementadas

### Funcionalidades Principales
- [x] Crear nuevas tareas
- [x] Ver lista de tareas
- [x] Editar tareas existentes
- [x] Activar/desactivar recordatorios
- [x] Persistencia automática
- [x] Notificaciones de recordatorio

### Navegación
- [x] Navegación entre fragments
- [x] Paso de datos entre pantallas
- [x] Navegación hacia atrás automática

### Recordatorios
- [x] Opción de notificaciones locales (seleccionada)
- [x] Programación con AlarmManager
- [x] BroadcastReceiver para manejo de eventos

## Estructura del Proyecto

```
app/src/main/java/com/example/helloandroidcristofermunoz/
|
|-- model/task/
|   |-- Task.kt
|
|-- repository/task/
|   |-- TaskRepository.kt
|
|-- ui/task/
|   |-- TaskListFragment.kt
|   |-- TaskDetailFragment.kt
|
|-- viewmodel/task/
|   |-- TaskListViewModel.kt
|   |-- TaskDetailViewModel.kt
|
|-- adapter/
|   |-- TaskAdapter.kt
|
|-- receiver/
|   |-- TaskReminderReceiver.kt
|
|-- utils/
|   |-- AlarmUtils.kt
|
|-- MainActivity.kt
```

## Dependencias Principales

- **Navigation Component**: Para navegación entre fragments
- **RecyclerView**: Para lista de tareas eficiente
- **CardView**: Para diseño de items
- **Gson**: Para serialización JSON
- **ViewModel y LiveData**: Para arquitectura MVVM

## Permisos Requeridos

- `POST_NOTIFICATIONS`: Para mostrar notificaciones
- `SCHEDULE_EXACT_ALARM`: Para alarmas precisas
- `WAKE_LOCK`: Para despertar dispositivo

## Uso de la Aplicación

1. **Lista de Tareas**: Muestra todas las tareas guardadas
2. **Agregar Tarea**: Botón flotante (+) para crear nueva tarea
3. **Editar Tarea**: Click en una tarea existente
4. **Recordatorios**: Switch para activar notificación (30 segundos después)

## Notas Técnicas

- Los recordatorios se programan 30 segundos después de crear la tarea para facilitar pruebas
- Se usa findViewById en lugar de ViewBinding para simplicidad
- La persistencia usa SharedPreferences con formato JSON
- La navegación usa Bundle para pasar datos entre fragments

## Compilación y Ejecución

```bash
./gradlew build
./gradlew installDebug
```

La aplicación está lista para ejecutarse en emulador o dispositivo físico con API 24+.

## Entregables

- [x] Código fuente completo
- [x] Historial de commits descriptivo
- [x] README actualizado
- [x] Funcionalidad completa probada
