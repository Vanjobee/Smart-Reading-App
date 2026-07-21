# SGB-READ

A native Android reading app for early readers, built around a farm and rice-field
theme for the Zanjera farming community. Built with Kotlin and Jetpack Compose.

## Features

- **Farm theme** throughout: rice-paddy backgrounds, a nipa hut, and ~60 hand-drawn
  flat-vector farm icons (animals, produce, tools, weather) rendered entirely in
  Compose — no bitmap assets required.
- **4 learning modules, 3 activities each**, matching the SGB-READ curriculum:
  1. **Letter Recognition** — Trace the Letter, Letter Basket, Match Upper & Lowercase
  2. **Phonics** — Listen and Match, Tap the Letter, Letter Hunt
  3. **Blending (CVC Words)** — Build the Word, Supply the Missing Letter, Blend and Read
  4. **Consonant & Vowel Digraphs** — Listen and Build, Picture-to-Word Match, Digraph Hunt
- **Voice-over & pronunciation** via Android's TextToSpeech (US English, slowed rate)
  for every letter, word, and instruction, plus short chime/SFX for correct/incorrect
  answers and activity completion.
- **Immediate, gentle feedback**: correct answers get a chime, checkmark, and a random
  praise phrase (Great Job!, Excellent!, Super Reader!...); incorrect answers get a
  soft tone and another try — never a penalty.
- **Progress tracking** persisted with DataStore, visualized as plant growth:
  Seed → Sprout → Growing Plant → Flowering Plant → Harvest.
- **Full SGB-READ vocabulary progression** (Levels 1–7, from 3-letter CVC words
  through advanced farm vocabulary) modeled in `data/Vocabulary.kt`.

## Project structure

```
app/src/main/java/com/sgbread/app/
  data/         vocabulary, letters/digraphs, module metadata, farm icon keys
  audio/        TextToSpeech + SoundPool wrapper
  progress/     DataStore-backed progress repository & ViewModel
  ui/theme/     farm color palette, typography
  ui/icons/     FarmIcon.kt — all farm illustrations, drawn with Compose Canvas
  ui/components/ shared widgets: backgrounds, progress bar, feedback banners, tiles
  navigation/   NavGraph wiring home → module → activity screens
  screens/      home/module list + module1..4 activity screens
```

## Building

This project uses Gradle 8.9 / AGP 8.5 / Kotlin 2.0, targeting `compileSdk 34`,
`minSdk 26`. Open the project root in Android Studio (Koala or newer) and let it
sync, or from the command line:

```
./gradlew assembleDebug
```

## Design notes

- Interactions are **tap-based** rather than free-drag (e.g. "tap a letter, then tap
  its basket") — the source document allows drag *or* tap, and tapping is more
  reliable for young children's motor skills.
- All farm pictures are vector illustrations drawn at runtime (`ui/icons/FarmIcon.kt`)
  so the app has no image asset pipeline to maintain.
- SFX (`app/src/main/res/raw/*.wav`) are procedurally generated placeholder tones;
  swap them for recorded voice/SFX assets whenever available.
