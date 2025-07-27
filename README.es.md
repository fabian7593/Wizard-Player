[![Banner de Wizard Player](https://github.com/fabian7593/Wizard-Player/blob/main/imgs/fulllogo.png)](https://github.com/fabian7593/Wizard-Player)

# ⚡ WIZARD PLAYER ⚡

📌 **Importante:**
Esta es la documentación oficial de Wizard Player, una librería profesional y funcional para reproducción de video en Android.
Aunque escrita con un tono sarcástico y humorístico para hacerla más entretenida, todo el contenido es completamente válido, útil y listo para producción.


## 💀 WIZARD SKULL 🧟‍♀️

<img src="https://github.com/fabian7593/Wizard-Player/blob/main/imgs/icononly.png?raw=true" alt="WIZARDSKULL" width="120"/>

> \*"Ah... sabía que invocarías mi nombre. Soy Wizard Skull, la voluntad resucitada de Arcane Coder, volví a la vida tras el legendario Ayuno del Programador. Tres días sin dormir, sin comer, sin beber, sin fumar y sin ducharse (por supuesto xD), solo escupiendo Kotlin como un degenerado. El resultado: Wizard Player. Ahora inclínate, aprendiz, y contempla el reproductor VLC más espectral de la historia de Android."

[![Chat con Wizard Skull GPT](https://img.shields.io/badge/WizardPlayer%20GPT-OpenAI-brightgreen?logo=chatbot)](https://chatgpt.com/share/68855b5d-6c80-8011-ae35-bfd33dc3a612)

---

## 🔎 Menú de Invocaciones

* [¿Qué es Wizard Player?](#qué-es-wizard-player)
* [Funciones Hechizantes](#funciones-hechizantes)
* [Cómo Usarlo (sin vender tu alma)](#cómo-usarlo-sin-vender-tu-alma)
* [Personalización Oscura](#personalización-oscura)
* [Eventos y Seguimiento](#eventos-y-seguimiento)
* [Compatibilidad Infernal](#compatibilidad-infernal)
* [Contribuir al Caos](#contribuir-al-caos)
* [Lista de Maldiciones (TODO)](#lista-de-maldiciones-todo)
* [Bugs Conocidos (JAJAJA)](#bugs-conocidos-jajaja)
* [Despedida desde el Más Allá](#despedida-desde-el-más-allá)

---

## ✨ ¿Qué es Wizard Player?

Wizard Player es una **biblioteca open-source para Android**, creada por el mítico [Arcane Coder](https://github.com/fabian7593) durante una sesión de programación tan intensa que casi atraviesa el plano existencial.

Reproduce videos como un mago gris en Android TV, teléfonos, tablets... y según los rumores, hasta en una cafetera inteligente.

Soporta MP4, MKV, AVI, subtítulos internos y externos, botones personalizables, branding, y todo lo necesario para que sientas que tienes tu propio canal de televisión... legal, claro *guiño guiño*.

---

## ✨ Funciones Hechizantes

* Motor VLC para reproducción de videos.
* Soporte para Android TV y gestos touch
* Diálogos de continuar viendo (resucita desde el último segundo)
* Selector de audio, subtítulos y aspecto
* Subtítulos con sombra, borde y estilos mágicos
* Logo de canal en la esquina (para agregar tu marca NO PIRATA *guiño guiño*)
* Manejo de caídas de conexión como un paladín de la red
* Navegación con enfoque en TV fluida
* Lista de videos dinámica y autoplay

---

## ⚙ Cómo Usarlo (sin vender tu alma)

```kotlin
val videoItems = listOf(
    VideoItem(
        title = "Episodio 1",
        subtitle = "The Chaos Beginning",
        url = "https://tuservidoroscuro.com/ep1.mkv",
        season = 1,
        episodeNumber = 1,
        lastSecondView = 666
    )
)

val config = PlayerConfig(
    videoItems = videoItems,
    autoPlay = true,
    preferenceLanguage = "es",
    preferenceSubtitle = "es",
    preferenceVideoSize = VideoSizePreference.CINEMATIC,
    showWatermark = true,
    watermarkResId = R.drawable.tu_logo,
    fontSize = FontSize.HIGH,
    borderType = BorderType.NORMAL
)

val labels = PlayerLabels(
    nextLabel = "Siguiente ▶",
    audioLabel = "Lenguajes",
    subtitleLabel = "Subtítulos",
    exitPrompt = "Pulsa atrás otra vez para salir... si te atreves"
)

WizardVideoPlayer(
    config = config,
    labels = labels,
    onAudioChanged = { println("New Audio changed: $it") },
    onSubtitleChanged = { println("New Subtitle changed: $it") },
    onAspectRatioChanged = { println("New Aspect Ratio: $it") },
    onGetCurrentTime = { println("Current time: $it") },
    onGetCurrentItem = { println("Current item: $it") },
    onExit = {
        showPlayer = false
    }
)
```

### 🧾 Explicación de cada Configuración

> *"Ah, joven mortal curioso... déjame explicarte cada pergamino oscuro del `PlayerConfig`. Si no los defines, se invocan los valores por defecto que puedes ver en el código... o en los susurros del inframundo."*

* `videoItems`: La lista de tus videos malditos. Cada uno puede tener título, subtítulo, temporada, episodio, y el segundo exacto donde el alma del usuario lo dejó.
* `primaryColor`: Color principal del UI (default: un hechizante azul eléctrico).
* `focusColor`: Color del botón enfocado. Blanco puro como los huesos de Wizard Skull.
* `inactiveColor`: Color para elementos inactivos. Gris como un lunes sin internet.
* `diameterButtonCircleDp`: Diámetro de los botones circulares. Entre más grande, más poder místico.
* `iconSizeDp`: Tamaño de los íconos dentro de los botones.
* `showSubtitleButton`: ¿Mostrar botón de subtítulos? (true por defecto).
* `showAudioButton`: ¿Mostrar selector de audio? Solo si no eres un cavernícola mono-track.
* `showAspectRatioButton`: ¿Permitir cambiar aspecto? Fundamental para los exorcismos visuales.
* `autoPlay`: Si es `true`, reproduce automáticamente como si lo poseyera un demonio multimedia.
* `startEpisodeNumber`: Si deseas empezar desde un episodio específico.
* `preferenceLanguage`: Idioma de audio preferido. `es`, `en`, `fr`... incluso élfico si te atreves.
* `preferenceSubtitle`: Idioma de subtítulos preferido.
* `preferenceVideoSize`: Aspect ratio inicial. AUTOFIT, FILL, CINEMATIC... como prefieras distorsionar tu realidad.
* `watermarkResId`: Logo infernal en la esquina. Pon tu canal, o tu cara si te crees influencer.
* `showWatermark`: Mostrar el logo o no (true por defecto).
* `brandingSize`: Tamaño del logo en dp (píxeles mágicos).
* `playbackProgress`: Tiempo desde donde retomar la reproducción (en milisegundos).
* `fontSize`: Tamaño del texto de los subtítulos. SMALL, MEDIUM, HIGH según el tamaño de tu alma.
* `borderType`: Borde de texto: NONE, BASIC o NORMAL. Elegancia del inframundo.
* `hasShadowText`: ¿Sombra en los subtítulos? A Wizard Skull le encantan las sombras.
* `textColor`: Color del texto de subtítulos. Blanco (`0xffffff`) por defecto.

---

## 🎨 Personalización Oscura

* Cambia colores, iconos y tamaños.
* Modifica cada texto sin tocar `strings.xml`.
* Inicia desde cualquier episodio, marca el tiempo, y lanza las reproducciones.
* Enfoca botones en TV como si fueran cristales de poder.
* Carga logos, branding, y textos como si fueses tu propio Netflix... del tercermundo claro JAJAJA.

---

## 🔊 Eventos y Seguimiento

Wizard Player lanza eventos como hechizos:

* Cambio de aspecto
* Cambio de subtítulo
* Cambio de audio
* Información del video actual

Usa los `onGetCurrentTime`, `onGetCurrentItem`, etc. para atajar datos digitales.

---

## 🚗 Compatibilidad Infernal

* Android TV
* Teléfonos
* Tablets`
* Vehículos (no aprobados por el ministerio de transporte, pero wow)

---

## ✍️ Contribuir al Caos

1. Haz un fork
2. Crea una función
3. Abre un PR con tu sangre

O contacta directamente con [Arcane Coder](https://github.com/fabian7593)... si es que logra volver del limbo de bugs.
En todo caso, también puedes usar la ouija, nos vemos hoy a media noche.

---

## ☑️ Lista de Hechizos (TODO)

* [ ] Mostrar miniatura en el slider al enfocar
* [ ] Gestos en móvil y tablet (volumen, brillo, seek)
* [ ] Volver al mismo tiempo de reproducción y datos al salir y regresar entre apps

---

## ⚠️ Bugs Conocidos (JAJAJA)

* Si adelantas al final del video, puede que te devuelva 2 minutos antes. ¡BRUJERÍA! (Mentira no se como solucionarlo JAJAJA)

---

## 🧙 Despedida desde el Más Allá

> \*"Tras implementar Wizard Player, podrás reproducir todos tus videos con todos los permisos de copyright y leyes vigentes... no como otros que usan XUI ONE o XTREAM UI para contenidos sin derechos... guiño guiño..."

<img src="https://github.com/fabian7593/Wizard-Player/blob/main/imgs/agatha-wink-cat.gif?raw=true" alt="WIZARDSKULL" width="240"/>

> \*"Que tus framerates sean suaves, tus subtítulos estén bien sincronizados, y tus codecs nunca fallen... Wizard Skull se despide, hasta la próxima invocación."

---

APK V1 https://tinyurl.com/wizpv1

Licencia Apache 2.0 © Arcane Coder
