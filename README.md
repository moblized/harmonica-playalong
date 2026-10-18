# Harmonica Play-Along

An Android app for playing classical melodies on a 10-hole diatonic harmonica: Beethoven's Ode to Joy, Für Elise,
the Fifth and Seventh symphonies, the Moonlight and Pathétique sonatas, and other classics.
Built with Kotlin and Jetpack Compose.

## Install on your phone

Every push to `main` builds a signed APK on GitHub Actions and publishes it as a release.

1. Open the repo's **Releases** page on your phone and download `HarmonicaPlayAlong.apk` from the latest release.
2. Open the file. If Android asks, allow your browser to install apps.
3. Later builds install over the old one and keep your scores and settings, because every build uses the same signing key (`keystore/`).

## How it works

- **Falling notes.** Notes fall down ten lanes, one per hole, onto a drawing of a harmonica. A note reaches the line
  when it should be played, and its height is its length. Blue is blow, orange is draw, a pink outline means bend.
- **Tab strip.** The tab scrolls along underneath (`4  -4  5`), with note names if you want them. Tap any note to start from there.
- **The band.** A synthesised harmonica plays the melody and a small band plays bass and chords. Turn the melody,
  the band and the click on or off, slow the tempo down to 40 %, or loop the phrase you are working on.
- **Listen mode.** The app listens through the microphone and scores each note you play on pitch and on time,
  and keeps your best score per song. Use headphones (or turn the melody off) so it only hears you.
- **Wait for me.** The song only moves on when you play the next note, so you can learn a tune at your own pace.
- **Free play.** Play any note to see which hole it is, how in tune it is, a pitch trace, and a bend meter that shows
  how far down a bend you are.
- **Any harmonica.** Choose your harp's key (G to F♯). Tabs stay the same; the band and note names follow your harp.
- **Best key automatically.** Each song is transposed to the key that needs the fewest bends on a C-style Richter harp.
  You can move it up or down an octave or a half step yourself.

## Songs

| Song | Composer | Level on the harmonica |
| --- | --- | --- |
| Ode to Joy (Symphony No. 9) | Beethoven | Beginner |
| Für Elise | Beethoven | Advanced (blow bends) |
| Symphony No. 5 — Fate motif | Beethoven | Intermediate |
| Symphony No. 7 — Allegretto | Beethoven | Intermediate |
| Moonlight Sonata | Beethoven | Advanced |
| Pathétique — Adagio | Beethoven | Intermediate |
| Menuetto in G (Sonata Op. 49 No. 2) | Beethoven | Intermediate |
| Minuet in G | Bach / Petzold | Advanced (overblows in the second half) |
| Lullaby | Brahms | Beginner |
| The Blue Danube | Johann Strauss II | Advanced (one overblow) |
| Twinkle Variations — Theme | Mozart | Beginner |
| Largo — Going Home (New World Symphony) | Dvořák | Beginner |
| Canon in D | Pachelbel | Beginner |

Melodies and harmonies were checked against the free editions of the [Mutopia Project](https://www.mutopiaproject.org).
Songs live in `app/src/main/java/app/harmonicaplayalong/music/SongLibrary.kt` in a short text notation, so adding one is
a matter of typing its notes (see the comment on `Song` in `Song.kt`).

## Build it yourself

Open the folder in Android Studio (JDK 17), or run `./gradlew assembleRelease`.
The APK lands in `app/build/outputs/apk/release/`. `./gradlew testDebugUnitTest` checks every song parses,
fits the harmonica, and that the synth and pitch detector behave.
