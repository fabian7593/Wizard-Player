[![Wizard Player Banner](https://github.com/fabian7593/Wizard-Player/blob/main/imgs/fulllogo.png)](https://github.com/fabian7593/Wizard-Player)

# ⚡ WIZARD PLAYER ⚡

📌 **Important:**
This is the official documentation for **Wizard Player**, a professional and fully functional library for video playback on Android.
While written in a sarcastic and humorous tone for a more entertaining read, all content is completely valid, useful, and production-ready.

## 💀 WIZARD SKULL 🧟‍♀️

<img src="https://github.com/fabian7593/Wizard-Player/blob/main/imgs/icononly.png?raw=true" alt="WIZARDSKULL" width="60"/>

> *"Ah... I knew you'd summon me. I am Wizard Skull, the resurrected will of Arcane Coder, brought back to life after the legendary Programmer's Fast. Three days without sleep, food, drink, smoke, or showers (of course xD), just spewing Kotlin like a mad sorcerer. The result: Wizard Player. Now bow down, apprentice, and behold the most spectral VLC-based player in Android history."*

[![Chat with Wizard Skull GPT](https://img.shields.io/badge/Tenshi%20GPT-OpenAI-brightgreen?logo=chatbot)](https://chatgpt.com/share/68855b5d-6c80-8011-ae35-bfd33dc3a612)

---

## 🔎 Table of Invocations

* [What is Wizard Player?](#what-is-wizard-player)
* [Bewitching Features](#bewitching-features)
* [How to Use It (without selling your soul)](#how-to-use-it-without-selling-your-soul)
* [Dark Customization](#dark-customization)
* [Events and Tracking](#events-and-tracking)
* [Infernal Compatibility](#infernal-compatibility)
* [Contribute to the Chaos](#contribute-to-the-chaos)
* [Spell List (TODO)](#spell-list-todo)
* [Known Bugs (HAHAHA)](#known-bugs-hahaha)
* [Farewell from the Beyond](#farewell-from-the-beyond)

---

## ✨ What is Wizard Player?

Wizard Player is an **open-source library for Android**, created by the mythical [Arcane Coder](https://github.com/fabian7593) during a coding session so intense it almost pierced the existential plane.

It plays videos like a gray wizard on Android TV, phones, tablets... and according to rumors, even on a smart coffee maker.

Supports MP4, MKV, AVI, internal and external subtitles, customizable buttons, branding, and everything you need to feel like you’re running your own TV channel… legally, of course *wink wink*.

---

## ✨ Bewitching Features

* VLC engine under the hood for video playback.
* Support for Android TV and touch gestures.
* Continue watching dialogs (resurrects from the last second).
* Audio, subtitle, and aspect ratio selector.
* Subtitles with shadow, borders, and magical styles.
* Channel logo in the corner (add your totally NOT PIRATE brand *wink wink*).
* Connection fallback handling like a network paladin.
* Smooth TV focus navigation.
* Dynamic playlist and autoplay.

---

## ⚙ How to Use It (without selling your soul)

```kotlin
val videoItems = listOf(
    VideoItem(
        title = "Episode 1",
        subtitle = "The Chaos Beginning",
        url = "https://yourdarkserver.com/ep1.mkv",
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
    watermarkResId = R.drawable.your_logo,
    fontSize = FontSize.HIGH,
    borderType = BorderType.NORMAL
)

val labels = PlayerLabels(
    nextLabel = "Next ▶",
    audioLabel = "Languages",
    subtitleLabel = "Subtitles",
    exitPrompt = "Press back again to exit... if you dare"
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

### 🧾 Configuration Breakdown

> *"Ah, curious mortal... let me explain each arcane scroll of `PlayerConfig`. If you don't define them, default values will be invoked from the code... or the whispers of the underworld."*

* `videoItems`: The list of your cursed videos. Each may include title, subtitle, season, episode, and the exact second where the user's soul left off.
* `primaryColor`: Primary UI color (default: a spellbinding electric blue).
* `focusColor`: Focused button color. Pure white like Wizard Skull’s bones.
* `inactiveColor`: Color for inactive elements. Gray as a Monday without internet.
* `diameterButtonCircleDp`: Diameter of circular buttons. Bigger = more mystical power.
* `iconSizeDp`: Icon size inside buttons.
* `showSubtitleButton`: Show subtitle button? (true by default).
* `showAudioButton`: Show audio selector? Only if you're not a mono-track caveman.
* `showAspectRatioButton`: Allow changing aspect ratio? Vital for visual exorcisms.
* `autoPlay`: If `true`, starts playback automatically like a possessed demon.
* `startEpisodeNumber`: Start from a specific episode.
* `preferenceLanguage`: Preferred audio language. `es`, `en`, `fr`... even Elvish if you dare.
* `preferenceSubtitle`: Preferred subtitle language.
* `preferenceVideoSize`: Initial aspect ratio. AUTOFIT, FILL, CINEMATIC... however you distort reality.
* `watermarkResId`: Logo in the corner. Add your channel, or your face if you’re an influencer.
* `showWatermark`: Whether to show the logo (true by default).
* `brandingSize`: Logo size in dp (magical pixels).
* `playbackProgress`: Resume point in milliseconds.
* `fontSize`: Subtitle text size. SMALL, MEDIUM, HIGH depending on your soul size.
* `borderType`: Subtitle border: NONE, BASIC, NORMAL. Elegance from the beyond.
* `hasShadowText`: Shadow in subtitles? Wizard Skull loves shadows.
* `textColor`: Subtitle text color. White (`0xffffff`) by default.

---

## 🎨 Dark Customization

* Change colors, icons, and sizes.
* Modify every label without touching `strings.xml`.
* Start from any episode, set playback time, and summon your show.
* Focus buttons on TV like channeling crystals.
* Load logos, branding, and texts like your own third-world Netflix (lol).

---

## 🔊 Events and Tracking

Wizard Player dispatches events like magic spells:

* Aspect ratio change
* Subtitle change
* Audio track change
* Current video info

Use `onGetCurrentTime`, `onGetCurrentItem`, etc. to catch those digital spirits.

---

## 🚗 Infernal Compatibility

* Android TV
* Phones
* Tablets
* Vehicles (not approved by the ministry of transportation, but wow)

---

## ✍️ Contribute to the Chaos

1. Fork it
2. Create a new feature
3. Open a PR with your blood

Or contact [Arcane Coder](https://github.com/fabian7593)... if he ever returns from the limbo of bugs.
Or use a Ouija board. See you at midnight.

---

## ☑️ Spell List (TODO)

* [ ] Show thumbnail preview on slider focus
* [ ] Mobile and tablet gestures (volume, brightness, seek)
* [ ] Resume playback and data when switching apps

---

## ⚠️ Known Bugs (HAHAHA)

* If you fast-forward to the end of the video, it might bounce you 2 minutes back. WITCHCRAFT! (Actually, no idea how to fix that lol)

---

## 🧙 Farewell from the Beyond

> *"After implementing Wizard Player, you’ll be able to play all your videos with proper copyright and licensing... not like those who use XUI ONE or XTREAM UI for shady content... wink wink..."*

<img src="https://github.com/fabian7593/Wizard-Player/blob/main/imgs/agatha-wink-cat.gif?raw=true" alt="WIZARDSKULL" width="120"/>

> *"May your framerates be smooth, your subtitles perfectly synced, and your codecs never fail... Wizard Skull departs, until the next summoning."*

---

Apache License 2.0 © Arcane Coder
