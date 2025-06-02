# QuickSales

## 1. Descripción general
QuickSales es una aplicación Android de compraventa de artículos entre usuarios. Permite a cada usuario publicar productos nuevos o de segunda mano, gestionar sus anuncios, descubrir productos de otros vendedores, marcar favoritos, controlar su saldo (cartera) y comunicarse mediante un chat interno. El propósito principal es crear un entorno sencillo e intuitivo para que los usuarios puedan comprar y vender objetos de forma segura y eficiente.

---

## 2. Contenido
1. [Requisitos previos](#2-requisitos-previos)  
2. [Instalación](#3-instalación)  
3. [Dependencias](#4-dependencias)  
4. [Estructura del proyecto](#5-estructura-del-proyecto)  
   - [5.1 Pantalla de carga (Splash)](#51-pantalla-de-carga-splash)  
   - [5.2 Login](#52-login)  
   - [5.3 Registro](#53-registro)  
   - [5.4 Vista: Menú](#54-vista-menú)  
   - [5.5 Vista: Mis Productos](#55-vista-mis-productos)  
   - [5.6 Vista: Favoritos](#56-vista-favoritos)  
   - [5.7 Vista: Cartera](#57-vista-cartera)  
   - [5.8 Vista: Productos Comprados](#58-vista-productos-comprados)  
   - [5.9 Vista: Productos Vendidos](#59-vista-productos-vendidos)  
   - [5.10 Vista: Usuarios](#510-vista-usuarios)  
   - [5.11 Vista: Chat](#511-vista-chat)  
5. [Flujo de navegación](#6-flujo-de-navegación)  

---

## 2. Requisitos previos
- Android Studio (versión mínima recomendada: 4.0 o superior)  
- SDK de Android (nivel mínimo API 21)  
- Java 8 o superior / Kotlin (según el lenguaje usado en el proyecto)  
- Conexión a Internet (para autenticación y carga/descarga de imágenes)  
- Emulador o dispositivo Android para pruebas

---

## 3. Instalación
1. Clonar el repositorio:  
   ```bash
   git clone https://turepositorio.git

2. Abrir el proyecto en Android Studio:

   * Seleccionar “Open an existing Android Studio project”.
   * Navegar hasta la carpeta clonada.
3. Sincronizar Gradle/Dependencias:

   * Android Studio suele descargar automáticamente las dependencias.
   * Si hay errores, verificar en `build.gradle` versiones y repositorios.
4. Configurar servicios externos (por ejemplo, servidor propio, etc.):

   * Ajustar claves de API si es necesario (p. ej., URL del backend en `Constants.java` o `BuildConfig`).
5. Ejecutar la aplicación en emulador o dispositivo físico.

---

## 4. Dependencias

En el archivo `app/build.gradle` (o `build.gradle.kts`), dentro del bloque `dependencies { … }`, incluye al menos las siguientes librerías (ajusta versiones según el proyecto):

```gradle
// AndroidX y Material
implementation 'androidx.core:core-ktx:1.10.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.9.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

// Retrofit y OkHttp (para comunicación REST)
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11'

// Glide (carga y caché de imágenes)
implementation 'com.github.bumptech.glide:glide:4.15.0'
kapt 'com.github.bumptech.glide:compiler:4.15.0'

// Room (si usas base de datos local)
implementation 'androidx.room:room-runtime:2.5.2'
kapt 'androidx.room:room-compiler:2.5.2'
implementation 'androidx.room:room-ktx:2.5.2'

// Kotlin Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'

// Lifecycle (ViewModel y LiveData)
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.1'

// Navigation Component (Entre fragments/actividades)
implementation 'androidx.navigation:navigation-fragment-ktx:2.6.0'
implementation 'androidx.navigation:navigation-ui-ktx:2.6.0'

// Otras utilidades comunes
implementation 'com.google.code.gson:gson:2.10.1'  
implementation 'com.squareup.okio:okio:3.5.0'  

// Plugins en la parte superior del build.gradle (app-level)
apply plugin: 'kotlin-kapt'
```

> **Importante:**
>
> * Revisa que las versiones coincidan con las que usas en tu proyecto.
> * En `build.gradle (project-level)` debes tener habilitado el repositorio de Google y MavenCentral:
>
>   ```gradle
>   buildscript {
>       repositories {
>           google()
>           mavenCentral()
>       }
>       dependencies {
>           classpath 'com.android.tools.build:gradle:8.0.0'
>           classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20'
>       }
>   }
>   ```
> * Si usas Kotlin DSL (`build.gradle.kts`), ajusta la sintaxis en consecuencia.

---

## 5. Estructura del proyecto

A continuación se describen las diferentes “vistas” (pantallas o activities/fragments) que componen la aplicación. Para cada vista, indica:

* **Descripción**: qué función cumple.
* **Componentes principales**: Activity, Fragment, Layout XML, adapters, ViewModels, etc.
* **Clases necesarias**: listado de clases Java/Kotlin (Actividades, Adaptadores, Modelos, Utilidades).
* **Flujo de datos**: de dónde se obtiene la información y cómo se muestra.
* **Validaciones y lógica de negocio**: reglas o casos especiales que se ejecutan en esa vista.

---
### 5.1 Pantalla de carga (Splash)

#### Descripción

La pantalla de carga (generalmente `SplashActivity`) es la primera que se muestra cuando el usuario abre la aplicación. Su objetivo principal es:

* Mostrar el logotipo o animación de QuickSales durante un breve instante.
* Verificar si el usuario ya tiene una sesión activa (por ejemplo, comprobando un token almacenado en `SharedPreferences` o en algún almacenamiento interno).
* Dirigir al usuario automáticamente a la pantalla de Login si no está autenticado, o directamente al Menú principal si ya ha iniciado sesión previamente.

#### Componentes principales

* **Activity:** `SplashActivity`
* **Layout XML:** `activity_splash.xml`, contiene una imagen (logo) o animaciones ligeras.
* **Temas:** En `styles.xml`, suele definirse un tema sin ActionBar para que la pantalla ocupe todo el espacio.

#### Flujo de datos

1. **Inicio de SplashActivity:**
   Al crearse, ajusta parámetros de UI (por ejemplo, color de barra de estado, insets si se usa `WindowInsetsCompat`).
2. **Retraso breve:**
   Se mantiene la vista unos segundos (usualmente entre 1–2 segundos) para permitir que se muestre el logotipo y dar tiempo a leer o simplemente proporcionar una transición visual suave.
3. **Comprobación de sesión:**

   * Se accede a `SharedPreferences` (o a algún repositorio local) para verificar si existe un token de autenticación válido.
   * Si hay token y no ha expirado, se lanza `MenuActivity` (o la pantalla principal).
   * Si no existe token, se lanza `LoginActivity`.
4. **Cierre de SplashActivity:**
   Tras redirigir al usuario, se llama a `finish()` para que el splash no quede en la pila de actividades.

#### Validaciones y lógica de negocio

* **Token válido:** En algunos proyectos se comprueba también mediante una llamada ligera a la API si el token sigue siendo válido; si falla, se fuerza al usuario a iniciar sesión de nuevo.
* **Animaciones opcionales:** Se puede usar Lottie o `AnimatedVectorDrawable` para enriquecer la marca mientras carga.
* **Gestión de errores:** Si ocurren errores (por ejemplo, `SharedPreferences` no accesible), se asume que el usuario no está autenticado y se redirige a Login.

---

### 5.2 Login

#### Descripción

La pantalla de Login (`LoginActivity`) permite al usuario autenticarse con su correo electrónico y contraseña. Una vez validadas las credenciales frente al backend, se almacena localmente un token (o alguna marca de sesión) y se redirige al usuario al Menú principal.

#### Componentes principales

* **Activity:** `LoginActivity` ubicada en `com.iesvdc.acceso.quicksales.ui.view`
* **Layout XML:** `activity_login.xml`, incluye campos de texto para correo y contraseña, botón de “Iniciar sesión” y enlace para ir a Registro.
* **ViewModel:** `LoginViewModel` (si se aplica patrón MVVM), gestiona la llamada al repositorio de autenticación.
* **Repositorio de autenticación:** clase que envía las credenciales al servidor a través de Retrofit y obtiene un token.
* **SharedPreferences:** para almacenar de forma segura (p. ej., en modo privado) el token devuelto tras autenticación.

#### Flujo de datos

1. **Ingreso de credenciales:**
   El usuario escribe su correo electrónico y contraseña.
2. **Validación local:**

   * Se comprueba que ninguno de los campos esté vacío.
   * Opcionalmente, se verifica el formato de correo (expresión regular sencilla).
3. **Petición al backend:**

   * `LoginViewModel` (o directamente `LoginActivity`) invoca al repositorio de autenticación.
   * El repositorio usa Retrofit para enviar un `POST` a `/login` con un body JSON que contiene `{ email: "...", password: "..." }`.
4. **Recepción de respuesta:**

   * Si la respuesta es exitosa y devuelve un token, se guarda en `SharedPreferences` con una clave, por ejemplo `"auth_token"`.
   * Si la respuesta falla (usuario incorrecto o error de red), se muestra un `Toast` o `Snackbar` con el mensaje de error.
5. **Redirección:**

   * Tras guardar el token, se lanza `MenuActivity` y se termina `LoginActivity` para que no vuelva al presionar “Atrás”.
   * Si se detecta que el usuario ya tiene token válido (por ejemplo, al volver desde otra pantalla), se salta directamente la autenticación y se muestra el menú.

#### Validaciones y lógica de negocio

* **Manejo de errores de red:** Mostrar un mensaje claro si no hay conexión a Internet o si el servidor no responde.
* **Indicador de carga:** Mientras dura la petición, debería aparecer un `ProgressBar` para informar al usuario que se está procesando.
* **Recordar usuario:** Si se desea, se puede guardar también el correo en `SharedPreferences` y rellenar el campo automáticamente la próxima vez.
* **Seguridad:** Nunca almacenar la contraseña en texto plano; solo almacenar el token (y, si es sensible, cifrarlo o usar `EncryptedSharedPreferences`).

---

### 5.3 Registro

#### Descripción

La pantalla de Registro (`RegisterActivity`) permite crear una cuenta nueva. El usuario introduce correo electrónico, nombre de usuario y contraseña (con confirmación). Al enviarse, se crea el usuario en el servidor, y al recibirse la respuesta positiva, casi siempre se vuelve a la pantalla de Login (o se inicia sesión automáticamente).

#### Componentes principales

* **Activity:** `RegisterActivity` en `com.iesvdc.acceso.quicksales.ui.view`
* **Layout XML:** `activity_register.xml`, contiene campos para nombre de usuario, correo, contraseña y confirmación de contraseña, junto con un botón “Registrarse”.
* **ViewModel:** `RegisterViewModel` (opcional) para orquestar la llamada al repositorio.
* **Repositorio de autenticación:** misma clase que Login, pero con método `register(...)` que envía un `POST` a `/register` con `{ username: "...", email: "...", password: "..." }`.

#### Flujo de datos

1. **Ingreso de datos:**
   El usuario rellena nombre, correo, contraseña y confirma contraseña.
2. **Validación local:**

   * Verificar que no haya campos vacíos.
   * Asegurar que el correo tenga formato válido.
   * Comparar que contraseña y confirmación coincidan.
   * Opcionalmente, chequear longitud mínima de la contraseña (p. ej., 8 caracteres).
3. **Petición al backend:**

   * `RegisterViewModel` (o `RegisterActivity`) llama a `AuthRepository.register(...)`.
   * Retrofit envía `POST` a `/register`.
4. **Recepción de respuesta:**

   * Si el servidor devuelve éxito (usuario creado), se muestra un mensaje (`Toast` o `Snackbar`), y se redirige a `LoginActivity` automáticamente, o se inicia sesión guardando el token devuelto (si la API lo proporciona).
   * Si falla (por ejemplo, correo ya registrado), se muestra la causa exacta recibida en la respuesta.
5. **Redirección o login automático:**

   * En un escenario, tras registro exitoso se lleva al usuario a Login para que introduzca sus credenciales.
   * En otro, la API devuelve directamente un token junto con datos del usuario, se guarda el token y se va al Menú principal sin necesidad de reingresar.

#### Validaciones y lógica de negocio

* **Comprobaciones de fortaleza de contraseña:** Informar al usuario si la contraseña no cumple los requisitos mínimos (números, mayúsculas, etc.), si se definieron.
* **Confirmación visual:** Si la contraseña y la confirmación no coinciden, mostrar inmediatamente un mensaje de error debajo del campo de confirmación.
* **Errores de red:** Mostrar un `AlertDialog` o `Snackbar` si falla la creación (por ej., “El correo ya existe” o “Error de conexión”).
* **Aviso de términos y condiciones:** Opcionalmente, incluir un checkbox que el usuario debe marcar para aceptar políticas antes de habilitar el botón “Registrarse”.

---
### 5.4 Vista: Menú

#### Descripción

Pantalla principal que aparece tras iniciar sesión. Permite navegar a “Mis Productos”, “Favoritos”, “Cartera”, “Productos Comprados”, “Productos Vendidos”, “Usuarios” y “Chat”.

#### Componentes principales

* `MainActivity` (o `MenuActivity`)
* `activity_main.xml` (layout con botones/tiles para cada sección)
* Drawer/BottomNavigation (si se usa)
* `NavigationController` o `NavHostFragment` (si se emplea Jetpack Navigation)

#### Clases necesarias

* **Actividad/Fragment**

  * `MainActivity` / `MenuFragment`
* **Adaptadores** (si el menú se muestra como RecyclerView)

  * `MenuAdapter`
* **Modelos**

  * `MenuItem` (título, icono, ruta)
* **Utilidades**

  * `SessionManager` (gestión de sesión de usuario)
  * `NavigationHelper` (funciones de navegación)

#### Flujo de datos

1. Al iniciar la actividad, se verifica la sesión de usuario.
2. Se carga lista de opciones en `RecyclerView` o botones fijos.
3. Cuando se pulsa un ítem, se lanza un Intent o se navega con NavController hacia la vista correspondiente.

#### Validaciones y lógica de negocio

* Si el usuario no está autenticado, redirigir a `LoginActivity`.
* Control de permisos (por ejemplo, para acceder a la cámara o almacenamiento).

---

### 5.5 Vista: Mis Productos

#### Descripción

Muestra los productos que el usuario ha publicado. Permite ver detalles, editar o eliminar un producto.

#### Componentes principales

* `MisProductosActivity` / `MisProductosFragment`
* `activity_mis_productos.xml` (layout con RecyclerView)
* `item_producto_usuario.xml` (cada fila de producto)
* Botón flotante (FloatingActionButton) para “+ Nuevo Producto”

#### Clases necesarias

* **Actividad/Fragment**

  * `MisProductosActivity` / `MisProductosFragment`
* **Adaptadores**

  * `MisProductosAdapter`
* **Modelos**

  * `Producto` (id, título, descripción, precio, imágenes, estado, etc.)
* **Repositorios / DataSources**

  * `ProductoRepository` (métodos: `getProductosPorUsuario`, `eliminarProducto`, `actualizarProducto`, etc.)
* **ViewModel (si usas MVVM)**

  * `MisProductosViewModel`
* **Diálogos**

  * `ConfirmarEliminarDialog`
  * `AgregarEditarProductoDialog`

#### Flujo de datos

1. `MisProductosViewModel` solicita a `ProductoRepository` la lista de productos del usuario.
2. El repositorio consulta la base de datos local o el backend remoto (API REST).
3. Se notifica al adaptador (`MisProductosAdapter`) y se muestra la lista en RecyclerView.
4. Al hacer clic en “+ Nuevo Producto”, se abre `AgregarEditarProductoActivity` o un diálogo; al editar, se pasan datos existentes.

#### Validaciones y lógica de negocio

* Verificar que los campos (título, precio) no estén vacíos antes de guardar.
* Solo permitir eliminar si el producto no está en proceso de compra.
* Subida de imágenes: compresión, permisos, carga asíncrona.

---

### 5.6 Vista: Favoritos

#### Descripción

Permite al usuario ver los productos marcados como favoritos. De aquí puede acceder a los detalles de cada producto o eliminarlo de favoritos.

#### Componentes principales

* `FavoritosActivity` / `FavoritosFragment`
* `activity_favoritos.xml` (layout con RecyclerView)
* `item_producto_favorito.xml`

#### Clases necesarias

* **Actividad/Fragment**

  * `FavoritosActivity` / `FavoritosFragment`
* **Adaptadores**

  * `FavoritosAdapter`
* **Modelos**

  * `ProductoFavorito` (o reutilizar `Producto` con un campo “favorito”)
* **Repositorios / DataSources**

  * `FavoritosRepository` (métodos: `getProductosFavoritos`, `quitarFavorito`, etc.)
* **ViewModel (si aplicable)**

  * `FavoritosViewModel`

#### Flujo de datos

1. `FavoritosViewModel` consulta `FavoritosRepository` y obtiene la lista de productos guardados como favoritos.
2. Se notifica a `FavoritosAdapter` para mostrar cada item.
3. Al pulsar el icono de “corazón”, se elimina de favoritos.

#### Validaciones y lógica de negocio

* Evitar duplicados al añadir a favoritos.
* Actualizar la interfaz de usuario en tiempo real si cambian estados (LiveData / Flow).

---

### 5.7 Vista: Cartera

#### Descripción

Muestra el saldo del usuario (por ejemplo, dinero disponible para compras) y el historial de movimientos (recargas, ventas, compras).

#### Componentes principales

* `CarteraActivity` / `CarteraFragment`
* `activity_cartera.xml` (layout que muestra saldo actual y lista de transacciones)
* `item_movimiento_cartera.xml`

#### Clases necesarias

* **Actividad/Fragment**

  * `CarteraActivity` / `CarteraFragment`
* **Adaptadores**

  * `MovimientosAdapter`
* **Modelos**

  * `MovimientoCartera` (id, tipo: “recarga”/“compra”/“venta”, monto, fecha)
* **Repositorios / DataSources**

  * `CarteraRepository` (métodos: `getSaldoActual`, `getHistorialMovimientos`)
* **ViewModel (si aplicable)**

  * `CarteraViewModel`

#### Flujo de datos

1. `CarteraViewModel` solicita a `CarteraRepository` el saldo y la lista de movimientos.
2. Se muestra el saldo en la parte superior.
3. `MovimientosAdapter` llena un RecyclerView con el historial.

#### Validaciones y lógica de negocio

* Formateo correcto de fechas y monedas.
* Bloquear o notificar si el usuario intenta comprar sin saldo suficiente.

---

### 5.8 Vista: Productos Comprados

#### Descripción

Lista los productos que el usuario ha comprado. Permite ver detalles de cada compra y, si procede, valorar al vendedor.

#### Componentes principales

* `ProductosCompradosActivity` / `ProductosCompradosFragment`
* `activity_productos_comprados.xml` (layout con RecyclerView)
* `item_producto_comprado.xml`

#### Clases necesarias

* **Actividad/Fragment**

  * `ProductosCompradosActivity` / `ProductosCompradosFragment`
* **Adaptadores**

  * `CompradosAdapter`
* **Modelos**

  * `Compra` (idCompra, idProducto, fecha, precioPagado, estadoEntrega)
* **Repositorios / DataSources**

  * `CompraRepository` (métodos: `getComprasPorUsuario`)
* **ViewModel (si aplicable)**

  * `CompradosViewModel`
* **Diálogos / Fragmentos secundarios**

  * `ValoracionVendedorDialog` (si añades sistema de valoraciones)

#### Flujo de datos

1. `CompradosViewModel` solicita a `CompraRepository` la lista de compras del usuario.
2. Se muestra cada elemento en RecyclerView.
3. Al pulsar sobre uno, se abre `DetalleCompraActivity` con información completa (vendedor, datos de envío) y opción de valorar.

#### Validaciones y lógica de negocio

* Estado de entrega (“pendiente”, “enviado”, “entregado”).
* Solo permitir valorar una vez entregado.

---

### 5.9 Vista: Productos Vendidos

#### Descripción

Muestra la lista de productos que el usuario ha vendido o que tiene vendidos (pedidos pendientes de envío, completados, cancelados, etc.).

#### Componentes principales

* `ProductosVendidosActivity` / `ProductosVendidosFragment`
* `activity_productos_vendidos.xml` (layout con RecyclerView)
* `item_producto_vendido.xml`

#### Clases necesarias

* **Actividad/Fragment**

  * `ProductosVendidosActivity` / `ProductosVendidosFragment`
* **Adaptadores**

  * `VendidosAdapter`
* **Modelos**

  * `Venta` (idVenta, idProducto, comprador, estadoVenta, fechaVenta)
* **Repositorios / DataSources**

  * `VentaRepository` (métodos: `getVentasPorUsuario`)
* **ViewModel (si aplicable)**

  * `VendidosViewModel`
* **Diálogos / Fragmentos secundarios**

  * `ConfirmarEnvioDialog`
  * `DetalleVentaActivity`

#### Flujo de datos

1. `VendidosViewModel` solicita a `VentaRepository` las ventas realizadas por el usuario.
2. Se lista cada venta con su estado (p.ej., “Pendiente de envío”, “Enviado”, “Entregado”).
3. Permite marcar como “Enviado” o actualizar estado.

#### Validaciones y lógica de negocio

* Solo mostrar las ventas activas o histórico según se elija.
* Permitir marcar fecha de envío y seguimiento del pedido.

---

### 5.10 Vista: Usuarios

#### Descripción

Pantalla de perfil de usuario (propio o de otros). Muestra información básica (nombre, foto, valoraciones, historial de publicaciones).

#### Componentes principales

* `UsuarioActivity` / `UsuarioFragment`
* `activity_usuario.xml` (layout con datos de perfil, rating, lista de productos publicados por el usuario)
* `item_producto_usuario.xml` (para mostrar las publicaciones del usuario en su perfil)

#### Clases necesarias

* **Actividad/Fragment**

  * `UsuarioActivity` / `UsuarioFragment`
* **Adaptadores**

  * `PublicacionesUsuarioAdapter`
* **Modelos**

  * `Usuario` (id, nombre, email, fotoPerfil, ratingPromedio)
* **Repositorios / DataSources**

  * `UsuarioRepository` (métodos: `getUsuarioPorId`, `getPublicacionesUsuario`)
* **ViewModel (si aplicable)**

  * `UsuarioViewModel`

#### Flujo de datos

1. Al abrir la vista, se recibe un `userId` (propio o ajeno).
2. `UsuarioViewModel` solicita datos a `UsuarioRepository`.
3. Se muestra foto, nombre, rating y publicaciones recientes en un RecyclerView.

#### Validaciones y lógica de negocio

* Si es el propio usuario, mostrar botón “Editar perfil”.
* Comprobar permiso para ver datos sensibles (p.ej., email).

---

### 5.11 Vista: Chat

#### Descripción

Pantalla de lista de conversaciones y pantalla de mensajes dentro de cada conversación. Soporta texto, emojis e imágenes.

#### Componentes principales

* `ChatsActivity` / `ChatsFragment` (lista de conversaciones)
* `activity_chats.xml` (layout tipo RecyclerView de conversaciones)
* `item_conversacion.xml` (resumen de cada chat: nombre del usuario, última línea de mensaje, timestamp)
* `ChatActivity` / `ChatFragment` (detalle de una conversación)
* `activity_chat.xml` (lista de mensajes + campo de entrada + botón “Enviar”)
* `item_mensaje_enviado.xml` (burbujas de mensaje enviado)
* `item_mensaje_recibido.xml` (burbujas de mensaje recibido)

#### Clases necesarias

* **Actividad/Fragment**

  * `ChatsActivity` / `ChatsFragment`
  * `ChatActivity` / `ChatFragment`
* **Adaptadores**

  * `ConversacionesAdapter` (para la lista de chats)
  * `MensajesAdapter` (para los mensajes dentro de un chat)
* **Modelos**

  * `Conversacion` (idChat, userIdInterlocutor, últimoMensaje, timestamp)
  * `Mensaje` (idMensaje, idChat, remitente, contenido, tipo \[texto/imagen], timestamp)
* **Repositorios / DataSources**

  * `ChatRepository` (métodos: `getConversacionesPorUsuario`, `getMensajesPorChat`, `enviarMensaje`, `marcarLeido`)
* **ViewModel (si aplicable)**

  * `ChatsViewModel`
  * `ChatViewModel`
* **Servicios / WebSockets**

  * `WebSocketManager` (para conexiones en tiempo real)
* **Utilidades**

  * `DateUtils` (formateo de timestamps)
  * `ImageUtils` (para enviar/mostrar imágenes)

#### Flujo de datos

1. **Lista de conversaciones**

   * `ChatsViewModel` solicita desde `ChatRepository` la lista de chats del usuario.
   * `ConversacionesAdapter` muestra cada conversación con último mensaje y hora.
   * Al pulsar sobre una conversación, se abre `ChatActivity` pasando `chatId` y `userIdInterlocutor`.
2. **Pantalla de chat**

   * `ChatViewModel` suscribe a actualizaciones en `ChatRepository` (por ejemplo, escucha mediante WebSocket).
   * Se cargan todos los mensajes anteriores con `getMensajesPorChat(chatId)`.
   * `MensajesAdapter` muestra los mensajes en orden cronológico.
   * Al escribir y pulsar “Enviar”:

     1. Se crea un objeto `Mensaje`, se envía a través de `ChatRepository.enviarMensaje()`.
     2. Se actualiza la lista automáticamente (LiveData/Flow).
   * Mensajes entrantes se reciben en tiempo real y se agregan a la lista.

#### Validaciones y lógica de negocio

* Comprobar conectividad antes de enviar mensajes.
* Mostrar indicadores de “escribiendo…” o “en línea” (opcional).
* Marcar mensajes como leídos al abrir la conversación.
* Gestión de notificaciones push para nuevos mensajes.
* Control de permisos para acceso a la cámara/galería si se envían imágenes.

---

## 6. Flujo de navegación

1. **Pantalla de Login / Registro**

   * Si el usuario está autenticado, ir a **Menú**.
   * Si no, mostrar opciones de “Iniciar sesión” o “Crear cuenta”.
2. **Menú principal**

   * Desde aquí se accede a cualquiera de las vistas:

     * Mis Productos
     * Favoritos
     * Cartera
     * Productos Comprados
     * Productos Vendidos
     * Usuarios (buscar/ver perfil)
     * Chat
3. **Navegación secundaria**

   * Dentro de cada vista, al pulsar un elemento (p.ej., producto) se navega a la vista de detalle.
   * En la vista “Usuarios”, pulsar un producto publicado lleva al detalle de producto.
   * Desde un detalle de producto fuera de “Mis Productos”, se puede “Chatear con el vendedor” (abre Chat).


---
