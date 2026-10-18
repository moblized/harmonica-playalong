package app.harmonicaplayalong.music

/**
 * The third batch: more Mozart, Vivaldi's Four Seasons and Rossini's William Tell galop.
 * Unless a song's source says otherwise, notes and harmonies come from the Mutopia Project's editions.
 */
internal object EvenMoreSongs {

    private const val MUTOPIA = "Notes checked against the Mutopia Project edition (mutopiaproject.org)."

    val songs: List<Song> = listOf(
        // ---------------------------------------------------------------- Mozart
        Song(
            id = "k331-theme",
            title = "Sonata in A — Theme",
            composer = "Wolfgang Amadeus Mozart",
            work = "Piano Sonata No. 11 in A major, K. 331 — I. Andante grazioso (theme)",
            year = "1783",
            about = "The gentle, rocking siciliano tune that opens the sonata whose finale is the Rondo alla Turca. " +
                "Lean on the dotted rhythms and keep it smooth.",
            timeSignature = "6/8",
            tempo = 72,
            key = "A major",
            style = BackingStyle.FLOWING,
            notation = """
                C#5e. D5s C#5e E5q E5e | B4e. C#5s B4e D5q D5e | A4q A4e B4q B4e | C#5q E5s D5s C#5q B4e |
                C#5e. D5s C#5e E5q E5e | B4e. C#5s B4e D5q D5e | A4q B4e C#5q D5e | C#5q B4e A4q re |
                E5e. F#5s E5e F#5q. | A5e. G#5s F#5e F#5e E5e E5s rs | E5e C#5e A4e E5e D5e B4e | E5e C#5e A4e C#5q B4e |
                C#5e. D5s C#5e E5q E5e | B4e. C#5s B4e D5q D5e | A4q B4e C#5q D5e | C#5q B4e B4q C#5e |
                C#5q D5e E5q F#5s G#5z A5z | A4q C#5s B4s A4q re
            """,
            chords = "A | E7 | F#m E . | A | A | E7 | A . D | A E7 A | " +
                "A . D | D . C#m | A E7 . | A | A | E7 | A . D | A E7 . | A | A",
            source = "$MUTOPIA The theme only, without its repeats.",
        ),
        Song(
            id = "eine-kleine-romanze",
            title = "Eine kleine Nachtmusik — Romanze",
            composer = "Wolfgang Amadeus Mozart",
            work = "Serenade No. 13 in G major, K. 525 — II. Romanze: Andante",
            year = "1787",
            about = "The serenade's slow movement: a tender, slightly teasing tune with little sighs and turns. " +
                "Play it softly, like a song under a window.",
            timeSignature = "4/4",
            tempo = 76,
            key = "C major",
            style = BackingStyle.FLOWING,
            notation = """
                E5q E5q E5q. G5e | F5e D5e F5e A5e G5e. E5s G5e re | C6e C6q B5e A5e A5q G5e | G5e F5e F5e E5e G5e. E5s D5e re |
                E5q E5q E5q. G5e | F5e D5e F5e A5e G5e. E5s G5e re | C6e G5e G5e E5e D5e A5s G5s F5e E5s D5s | C5q E5s D5s C5s D5s C5q rq
            """,
            chords = "C | Dm C | F | G7 | C | G7 C | C F | G7 C",
            source = "$MUTOPIA From the first violin part.",
        ),
        Song(
            id = "eine-kleine-menuetto",
            title = "Eine kleine Nachtmusik — Menuetto",
            composer = "Wolfgang Amadeus Mozart",
            work = "Serenade No. 13 in G major, K. 525 — III. Menuetto: Allegretto",
            year = "1787",
            about = "A sturdy, courtly minuet in three. The first half climbs to a high C; " +
                "the second half winds down in a chain of eighth notes.",
            timeSignature = "3/4",
            tempo = 120,
            key = "G major",
            style = BackingStyle.WALTZ,
            notation = """
                D5q G5q A5q | B5q C6h | A5q B5q G5q | A5q F#5q E5e D5e |
                E5e F#5e G5q A5q | B5q C6q A5e G5s A5s | B5q F#5e E5s F#5s A5q | F#5q G5q rq |
                B5q A5e G5e F#5e E5e | D#5e F#5e E5e F#5e G5e G#5e | B5e A5e G5e F#5e E5e D5e | C#5e E5e D5e E5e F#5e G5e |
                A5e F#5e G5q A5q | B5q C6q A5e G5s A5s | B5q F#5e E5s F#5s A5q | F#5q G5q rq
            """,
            chords = "G . D | G Am . | D Em . | A7 D . | Em . D7 | G D7 . | Em . Am | D7 G . | " +
                "G . B7 | B7 Em . | B7 . E7 | A7 | D7 | G D7 . | Em . Am | D7 G .",
            source = "$MUTOPIA From the first violin part.",
        ),
        Song(
            id = "minuet-k2",
            title = "Minuet in F",
            composer = "Wolfgang Amadeus Mozart",
            work = "Minuet in F major, K. 2",
            year = "1762",
            about = "One of the first pieces Mozart ever wrote, at the age of six. Short, graceful " +
                "and full of little leaps.",
            timeSignature = "3/4",
            tempo = 108,
            key = "F major",
            style = BackingStyle.WALTZ,
            notation = """
                F5e A5e C5q C5q | D5e F5e Bb4q Bb4q | A4e C5e F4q E4q | E4h F4q |
                C4e E4e G4q G4q | C4e F4e A4q A4q | C4t E4t G4t Bb4q A4q | A4h G4q |
                C5e Eb5e A4q A4q | Bb4e D5e G4q G4q | A4e C5e F#4q F#4q | F#4h G4q |
                Bb4e D5e G4q G4q | A4e C5e F4q F4q | G4e Bb4e E4q E4q | E4h F4q |
                F5e A5e C5q C5q | D5e F5e Bb4q Bb4q | A4e C5e F4q E4q | E4h F4q
            """,
            chords = "F | Bb | F . C | C . F | C | F | C7 . F | F C . | " +
                "D7 | Gm | D7 | D . G | C7 | F | C7 | C . F | F | Bb | F . C | C . F",
            source = MUTOPIA,
        ),
        Song(
            id = "horn-concerto-4",
            title = "Horn Concerto No. 4 — Rondo",
            composer = "Wolfgang Amadeus Mozart",
            work = "Horn Concerto No. 4 in E-flat major, K. 495 — III. Rondo: Allegro vivace",
            year = "1786",
            about = "The galloping hunting-call rondo Mozart wrote for his friend, the horn player Joseph Leutgeb. " +
                "Lots of repeated notes: tongue them crisply.",
            timeSignature = "6/8",
            tempo = 144,
            key = "Eb major",
            pickupBeats = 0.5,
            style = BackingStyle.MARCH,
            notation = """
                Bb4e | Eb5e Eb5e Eb5e Eb5e Eb5e Eb5e | D5e F5e Bb5e Bb5e Ab5e F5e | Eb5e Eb5e Eb5e Eb5e D5e Eb5e | F5e Bb4e Bb4e Bb4q Bb4e |
                Eb5e Eb5e Eb5e Eb5e Eb5e Eb5e | D5e F5e Bb5e Bb5e Ab5e F5e | G5e Bb5e Eb5e F5e Eb5e F5e | Eb5e Eb4e Eb4e Eb4q Bb4e |
                Eb5q. G5q. | Bb5q.~ Bb5q G5e | Ab5e F5e Eb5e D5e C5e D5e | Eb5q G5e Bb4q Bb4e |
                Eb5q. G5q. | Bb5q.~ Bb5e G5e Eb5e | Ab5e F5e D5e Bb4e C5e D5e | Eb5q re rq.
            """,
            chords = "- | Eb | Bb7 | Eb | Bb | Eb | Bb7 | Eb Bb7 | Eb | " +
                "Eb | Eb | Fm Bb7 | Eb | Eb | Eb | Fm Bb7 | Eb",
            source = "$MUTOPIA From the solo horn part, written here at concert pitch: the opening solo, then the solo after the first tutti.",
        ),
        Song(
            id = "deh-vieni",
            title = "Deh vieni alla finestra",
            composer = "Wolfgang Amadeus Mozart",
            work = "Don Giovanni, K. 527 — Act II, canzonetta 'Deh vieni alla finestra'",
            year = "1787",
            about = "Don Giovanni's serenade, sung under a balcony to a mandolin accompaniment. " +
                "A lilting 6/8 tune — the band plays the mandolin's broken chords.",
            timeSignature = "6/8",
            tempo = 100,
            key = "D major",
            pickupBeats = 0.5,
            style = BackingStyle.FLOWING,
            notation = """
                A3e | A3e D4e D4e D4q C#4e | B3q B3e rq B3e | A3h B3e G3e | A3q G3e F#3e re A3e |
                A3e D4e D4e D4q C#4e | B3q. C#4q. | A3q D4e C#4e. D4s B3e | A3q rq rq | rh. |
                rq rq re A3e | A3q A3e A3e G3e F#3e | G3q. C4q. | A3q A3e D4e. E4s C4e | B3q rq. G3e |
                D4q A3e B3e. A3s G3e | D4q. Bb3q. | B3e C#4e D4e D3q F#3s E3s | D3q rq rq
            """,
            chords = "- | D | G | A7 | D | D | E7 A | D A7 | A | D | A7 | " +
                "F#m B7 | Em Am | D7 | G | D G | D Gm | G A7 | D",
            source = "$MUTOPIA From the vocal line of the piano-vocal score.",
        ),
        Song(
            id = "ave-verum",
            title = "Ave verum corpus",
            composer = "Wolfgang Amadeus Mozart",
            work = "Ave verum corpus, K. 618 — soprano line",
            year = "1791",
            about = "A short, radiant motet from the last year of Mozart's life. Slow, hushed and all long notes: " +
                "a lovely piece for practising breath control.",
            timeSignature = "4/4",
            tempo = 56,
            key = "D major",
            style = BackingStyle.PAD,
            notation = """
                A4h D5q F#4q | A4q G#4q G4h | G4q B4q A4q G4q | G4q F#4q F#4h | E4h. E4q | F#4q F#4q G4q G4q | G4h F#4q F#4q | E4w |
                E4h. A4q | A4q G#4q G#4h | E4q G#4h B4q | B4q A4q A4q A4q | D5w~ | D5q C#5q B4q A4q | A4h G#4q G#4q | A4w |
                A4h. A4q | A4q Bb4q Bb4h | Bb4q D5q C5q Bb4q | Bb4q A4q A4h | G4h. G4q | G4q Bb4q A4q G4q | G4h F4e E4e F4q | E4h rh |
                F#4h. F#4q | F#4q E4q D4q G4q | G4h. G4q | G4q F#4q E4q A4q | A4w~ | A4q G4q A4q B4q | F#4h E4q. F#4e | G4h G4h |
                D5w~ | D5h D#5h | E5q B4q C#5q D5q | C#5q B4e A4e D5q G4q | F#4h E4q. E4e | D4w
            """,
            chords = "D | E7 A7 | A7 | D | A | D A7 | D | A | A | E7 | E | C#7 F#m | D G#dim | A D | E | A | " +
                "A | A7 C7 | C7 | F | G7 | A7 | Bbdim Dm | A | D | G | Em | A | F#m | Bm D | D A | G | " +
                "D D7 | Gm Adim | E7 | A7 D | D A7 | D",
            source = "$MUTOPIA From the soprano part.",
        ),

        // ---------------------------------------------------------------- Vivaldi, The Four Seasons
        Song(
            id = "vivaldi-spring",
            title = "Spring — Allegro",
            composer = "Antonio Vivaldi",
            work = "The Four Seasons — Concerto No. 1 in E major, RV 269 'Spring' — I. Allegro",
            year = "1725",
            about = "'Spring has arrived': the joyful opening ritornello of the Four Seasons, with the orchestra's " +
                "soft echoes of every phrase.",
            timeSignature = "4/4",
            tempo = 100,
            key = "E major",
            pickupBeats = 0.5,
            style = BackingStyle.FLOWING,
            notation = """
                E5e | G#5e G#5e G#5e F#5s E5s B5q. B5s A5s | G#5e G#5e G#5e F#5s E5s B5q. B5s A5s | G#5e A5s B5s A5e G#5e F#5e D#5e B4e E5e |
                G#5e G#5e G#5e F#5s E5s B5q. B5s A5s | G#5e G#5e G#5e F#5s E5s B5q. B5s A5s | G#5e A5s B5s A5e G#5e F#5q re E5e |
                B5e A5s G#5s A5e B5e C#6e B5q E5e | B5e A5s G#5s A5e B5e C#6e B5q E5e | C#6e B5q A5e G#5e F#5s E5s F#5q |
                E5q re E5e B5e A5s G#5s A5e B5e | C#6e B5q E5e B5e A5s G#5s A5e B5e | C#6e B5q E5e C#6e B5q A5e | G#5e F#5s E5s F#5q E5q rq
            """,
            chords = "- | E | E | E B7 | E | E | E B7 | E A | E A | A . E B7 | E | A E | A E | E B7 E .",
            source = "$MUTOPIA From the first violin part.",
        ),
        Song(
            id = "vivaldi-summer",
            title = "Summer — The Storm",
            composer = "Antonio Vivaldi",
            work = "The Four Seasons — Concerto No. 2 in G minor, RV 315 'Summer' — III. Presto",
            year = "1725",
            about = "The summer thunderstorm breaks: hammered notes over a low G, then lightning-fast scales " +
                "crashing down. The violins' tremolo sixteenths are written as eighths here so you can breathe.",
            timeSignature = "3/4",
            tempo = 92,
            key = "G minor",
            style = BackingStyle.FLOWING16,
            notation = """
                G4e G3e G3e G3e G3e G3e | F4e G3e G3e G3e G3e G3e | Eb4e G3e G3e G3e G3e G3e | D4e G3e G3e G3e G3e re | rh. |
                D5e D4e D4e D4e D4e D4e | C5e D4e D4e D4e D4e D4e | Bb4e D4e D4e D4e D4e D4e | A4e D4e D4e D4e D4e re |
                rs D5s C5s Bb4s A4s G4s F#4s E4s D4s C4s Bb3s A3s | G3e G3e G3e G3e G3e G3e |
                G3s G5s F5s Eb5s D5s C5s Bb4s A4s G4s F4s Eb4s D4s | C4e C4e C4e C4e C4e C4e |
                C4s Bb5s A5s G5s F5s Eb5s D5s C5s Bb4s A4s G4s F4s | Eb4e Eb4e Eb4e Eb4e Eb4e Eb4e |
                Eb4s D6s C6s Bb5s A5s G5s F#5s E5s D5s C5s Bb4s A4s | G4e G4e G4e G4e G4e G4e |
                G4s D6s C6s Bb5s A5s G5s F#5s E5s D5s C5s Bb4s A4s | G4e G4e G4e G4e G4e G4e | D5e G4e D5e G4e G4q
            """,
            chords = "Gm | Gm | Gm | Gm | - | D | D | D | D | D7 | Gm | Gm | Cm | Cm | Eb | Ebdim | Gm | Gm | Gm | Gm",
            source = "$MUTOPIA From the first violin part; repeated sixteenths simplified to eighths.",
        ),
        Song(
            id = "vivaldi-autumn",
            title = "Autumn — Allegro",
            composer = "Antonio Vivaldi",
            work = "The Four Seasons — Concerto No. 3 in F major, RV 293 'Autumn' — I. Allegro",
            year = "1725",
            about = "'Dance and song of the peasants' celebrating the harvest: a stamping country dance, " +
                "each phrase answered by a quiet echo an octave lower.",
            timeSignature = "4/4",
            tempo = 100,
            key = "F major",
            style = BackingStyle.MARCH,
            notation = """
                A5e A5e A5e Bb5e A5q A5e Bb5e | A5e A5e A5e A5e Bb5q A5e Bb5e | A5e G5s A5s Bb5e A5e G5q rq |
                A4e A4e A4e Bb4e A4q A4e Bb4e | A4e A4e A4e Bb4e A4q A4e Bb4e | A4e G4s A4s Bb4e A4e G4q rq |
                A5e A5e A5e C6e D5q D5e D5e | G5e G5e G5e Bb5e C5q C5e F5e | A5e A5e A5e Bb5e A5e A5e A5e Bb5e | A5e A5e A5e Bb5e A5e G5s A5s Bb5e G5e |
                A5q rq A4e A4e A4e Bb4e | A4e A4e A4e Bb4e A4e A4e A4e Bb4e | A4e G4s A4s Bb4e G4e F4q rq
            """,
            chords = "F | F | F Gm C . | F | F | F Gm C . | F Gm | C7 F | F | F . F C7 | F | F | F C7 F .",
            source = "$MUTOPIA From the solo violin part.",
        ),
        Song(
            id = "vivaldi-winter",
            title = "Winter — Largo",
            composer = "Antonio Vivaldi",
            work = "The Four Seasons — Concerto No. 4 in F minor, RV 297 'Winter' — II. Largo",
            year = "1725",
            about = "Warm by the fire while the rain falls outside: the solo violin sings this calm melody " +
                "while the orchestra's pizzicato plays the raindrops.",
            timeSignature = "4/4",
            tempo = 54,
            key = "Eb major",
            style = BackingStyle.FLOWING16,
            notation = """
                Eb5e Bb5s Ab5s G5e F5s Eb5s F5e Bb4e re Bb4e | Ab5s G5s F5s Eb5s D5e Ab5e Ab5e G5e re G5e |
                F5e G5s Ab5s Bb5e C6s D6s Eb5e F5s G5s Ab5e Bb5s C6s | D5e Eb5s F5s G5e Ab5s Bb5s C5e D5s Eb5s F5e G5s Eb5s |
                D5q~ D5s Bb4s A4s Bb4s F5q~ F5s Bb4s A4s Bb4s | G5q~ G5s Bb4s A4s Bb4s A5q~ A5s F5s Eb5s F5s |
                Bb5e Bb4e re Bb5e Bb5s Ab5s G5s F5s Eb5s D5s C5s Bb4s | C5q. Bb4e Bb4q rq |
                Bb4e F5s Eb5s D5e C5s Bb4s C5e F4e re F4e | Eb5s D5s C5s Bb4s A4e Eb5e Eb5e D5e re Bb4e |
                Ab5s G5s F5s Eb5s D5e Ab5e Ab5e G5e re G5e | C5e D5s Eb5s F5e G5s Ab5s D5e Eb5s F5s G5e Ab5s Bb5s |
                Eb5e F5s G5s Ab5e Bb5s C6s D5q re D5s Eb5s | F5s D5s C5s Bb4s G5s Ab5s Bb5s G5s F5e Bb4e re D5s Eb5s |
                F5s D5s C5s Bb4s G5s Ab5s Bb5s G5s F5e Bb4e re F5s Bb5s | G5e F5s Eb5s D5e. Eb5s Eb5h~ | Eb5h rh
            """,
            chords = "Eb Bb | Bb7 Eb | Bb Cm | Bb . Ab F7 | Bb | Eb F | Bb . | F7 Bb | " +
                "Bb F | F7 Bb | Bb7 Eb | Ab Bb | Cm Ab Bb . | Bb Eb Bb . | Bb Eb Bb . | Eb Bb Eb . | Eb",
            source = "$MUTOPIA From the solo violin part.",
        ),

        // ---------------------------------------------------------------- Rossini
        Song(
            id = "william-tell",
            title = "William Tell Overture — Finale",
            composer = "Gioachino Rossini",
            work = "Guillaume Tell (William Tell) — Overture, final galop",
            year = "1829",
            about = "The famous galop that ends the overture — the 'Lone Ranger' theme. A cavalry charge of " +
                "da-da-DUM rhythms on one note, then the same tune an octave higher. Tongue the repeated notes.",
            timeSignature = "2/4",
            tempo = 120,
            key = "C major",
            pickupBeats = 0.5,
            style = BackingStyle.MARCH,
            notation = """
                G4s G4s | G4e G4s G4s G4e G4s G4s | C5e D5e E5e G4s G4s | G4e G4s G4s C5e E5s E5s | D5e B4e G4e G4s G4s |
                G4e G4s G4s G4e G4s G4s | C5e D5e E5e C5s E5s | G5e F5s E5s D5e C5s E5s | C5e G4e C5e G5s G5s |
                G5e G5s G5s G5e G5s G5s | C6e D6e E6e G5s G5s | G5e G5s G5s C6e E6s E6s | D6e B5e G5e G5s G5s |
                G5e G5s G5s G5e G5s G5s | C6e D6e E6e C6s E6s | G6e F6s E6s D6e C6s E6s | C6e G5e B5e re | C6q rq
            """,
            chords = "- | C | C | C | G7 | C | C | G7 | C | C | C | C | G7 | C | C | G7 | C G7 | C",
            source = "Transposed from E major to C. The pitches follow a published harmonica tab of the galop " +
                "(mpurintun.powweb.com); the rhythm is the galop's standard eighth-and-two-sixteenths pattern. " +
                "Not checked against a full score.",
        ),
    )
}
