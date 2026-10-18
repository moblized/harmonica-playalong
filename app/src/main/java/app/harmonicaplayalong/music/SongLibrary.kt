package app.harmonicaplayalong.music

/**
 * The built-in songs. Melodies and harmonies were checked against the Mutopia Project editions
 * (public-domain and Creative Commons engravings of the original scores) where one exists.
 * Pitches are written as in the score; the app transposes each song to suit the harmonica.
 */
object SongLibrary {

    private const val MUTOPIA = "Notes checked against the Mutopia Project edition (mutopiaproject.org)."

    private val firstBatch: List<Song> = listOf(
        Song(
            id = "ode-to-joy",
            title = "Ode to Joy",
            composer = "Ludwig van Beethoven",
            work = "Symphony No. 9 in D minor, Op. 125 — finale",
            year = "1824",
            about = "The tune Beethoven set to Schiller's poem in the finale of his last symphony. " +
                "It moves almost entirely by step, which makes it the classic first harmonica melody.",
            timeSignature = "4/4",
            tempo = 108,
            key = "C major",
            style = BackingStyle.MARCH,
            notation = """
                E5q E5q F5q G5q | G5q F5q E5q D5q | C5q C5q D5q E5q | E5q. D5e D5h |
                E5q E5q F5q G5q | G5q F5q E5q D5q | C5q C5q D5q E5q | D5q. C5e C5h |
                D5q D5q E5q C5q | D5q E5e F5e E5q C5q | D5q E5e F5e E5q D5q | C5q D5q G4h |
                E5q E5q F5q G5q | G5q F5q E5q D5q | C5q C5q D5q E5q | D5q. C5e C5h |
            """,
            chords = "C | Am G | C | G | C | Am G | C | G7 C | G C | G C | G E7 | D7 G | C C7 | F | C | G7 C |",
            source = "$MUTOPIA Harmony from the hymn setting entered by Peter Chubb.",
        ),
        Song(
            id = "fur-elise",
            title = "Für Elise",
            composer = "Ludwig van Beethoven",
            work = "Bagatelle No. 25 in A minor, WoO 59",
            year = "1810",
            about = "The opening theme of Beethoven's best-known piano piece, played twice. " +
                "The rocking E–D♯ figure needs bends on a diatonic harmonica, so take it slowly at first.",
            timeSignature = "3/8",
            tempo = 72,
            key = "A minor",
            pickupBeats = 0.5,
            style = BackingStyle.ARPEGGIO,
            notation = """
                E5s D#5s | E5s D#5s E5s B4s D5s C5s | A4e rs C4s E4s A4s | B4e rs E4s G#4s B4s | C5e rs E4s E5s D#5s |
                E5s D#5s E5s B4s D5s C5s | A4e rs C4s E4s A4s | B4e rs E4s C5s B4s | A4e re E5s D#5s |
                E5s D#5s E5s B4s D5s C5s | A4e rs C4s E4s A4s | B4e rs E4s G#4s B4s | C5e rs E4s E5s D#5s |
                E5s D#5s E5s B4s D5s C5s | A4e rs C4s E4s A4s | B4e rs E4s C5s B4s | A4q.
            """,
            chords = "- | - | Am | E | Am | - | Am | E | Am | - | Am | E | Am | - | Am | E | Am",
            source = MUTOPIA,
        ),
        Song(
            id = "symphony-5",
            title = "Symphony No. 5 — Fate motif",
            composer = "Ludwig van Beethoven",
            work = "Symphony No. 5 in C minor, Op. 67 — I. Allegro con brio",
            year = "1808",
            about = "Short-short-short-long: the four-note motif and the opening bars where it is passed " +
                "between the second violins, violas and first violins. The held notes are the fermatas.",
            timeSignature = "2/4",
            tempo = 144,
            key = "C minor",
            style = BackingStyle.PAD,
            notation = """
                re G4e G4e G4e | Eb4h~ | Eb4h | re F4e F4e F4e | D4h~ | D4h~ | D4h |
                re G4e G4e G4e | Eb4e Ab4e Ab4e Ab4e | G4e Eb5e Eb5e Eb5e | C5h~ | C5e G4e G4e G4e | D4e Ab4e Ab4e Ab4e | G4e F5e F5e F5e | D5h~ |
                D5e G5e G5e F5e | Eb5h | D5e G5e G5e F5e | Eb5h | D5e G5e G5e F5e | Eb5q rq | C5q rq | G5h~ | G5h |
            """,
            chords = "- | - | - | - | - | - | - | - | Ab | Cm | Cm | Cm | G7 | G7 | G | G | Cm | G | Cm | G | Cm | Ab | G | G |",
            source = "$MUTOPIA The motif is pieced together from the string parts.",
        ),
        Song(
            id = "symphony-7",
            title = "Symphony No. 7 — Allegretto",
            composer = "Ludwig van Beethoven",
            work = "Symphony No. 7 in A major, Op. 92 — II. Allegretto",
            year = "1812",
            about = "The slow, insistent theme the violas and cellos play at the start of the second movement, " +
                "all built on one long-short-short-long-long rhythm.",
            timeSignature = "2/4",
            tempo = 76,
            key = "A minor",
            style = BackingStyle.PAD,
            notation = """
                E4q E4e E4e | E4q E4q | E4q E4e E4e | E4q E4q | E4q E4e F#4e | G4q G4q | G4q G4e G4e | G4q rq |
                G4q G4e A4e | B4q B4q | F#4q F#4e G#4e | A4q A4q | E4q E4e E4e | E4q E4q | E4q F#4e G#4e | A4q rq |
                G4q G4e A4e | B4q B4q | F#4q F#4e G#4e | A4q A4q | E4q E4e E4e | E4q E4q | E4q F#4e G#4e | A4h |
            """,
            chords = "Am | E | E | Am | Am | Am | G | C | C | B7 | E | Am | Am | E Am | E | Am | " +
                "C | B7 | E | Am | Am | E Am | E | Am",
            source = MUTOPIA,
        ),
        Song(
            id = "moonlight",
            title = "Moonlight Sonata",
            composer = "Ludwig van Beethoven",
            work = "Piano Sonata No. 14 in C♯ minor, Op. 27 No. 2 — I. Adagio sostenuto",
            year = "1801",
            about = "The quiet melody that floats over the rippling triplets in the first movement. " +
                "Long notes and a slow tempo make it good breath-control practice.",
            timeSignature = "4/4",
            tempo = 96,
            key = "C# minor",
            pickupBeats = 1.0,
            style = BackingStyle.TRIPLETS,
            notation = """
                G#4e. G#4s | G#4h. G#4e. G#4s | G#4h A4h | G#4h F#4q B4q | E4q rq rh |
                rh rq G4e. G4s | G4h. G4e. G4s | G4h. F#4q | F#4h G4q E4q | F#4h F#4h |
                B3q rq rq B4q | C5h. A#4q | B4h. B4q | C5h. A#4q | B4h B4h |
                B4h A4h | G4h F#4h | C#4h C#4q C#4q | F#4w
            """,
            chords = "C#m | G#7 | C#m F#m | E B7 | E | Em | G | C | Bm Em | F#7 | B | Em | B | Em | " +
                "B G#dim | Fdim F#m | G Cdim | C#7 | F#m",
            source = MUTOPIA,
        ),
        Song(
            id = "pathetique",
            title = "Pathétique — Adagio",
            composer = "Ludwig van Beethoven",
            work = "Piano Sonata No. 8 in C minor, Op. 13 — II. Adagio cantabile",
            year = "1798",
            about = "One of Beethoven's most singable slow themes, played twice. " +
                "Keep the long notes steady and let the little turn in bar 5 flow.",
            timeSignature = "2/4",
            tempo = 40,
            key = "Ab major",
            style = BackingStyle.FLOWING16,
            notation = """
                C4q Bb3q | Eb4q. Db4e | C4e Eb4e Ab4e Bb4e | Eb4q. E4e | F4q Bb3e. C4z Db4z | Eb4q A3q | Db4q C4s Bb3s Ab3s G3s | Bb3q Ab3q |
                C4q Bb3q | Eb4q. Db4e | C4e Eb4e Ab4e Bb4e | Eb4q. E4e | F4q Bb3e. C4z Db4z | Eb4q A3q | Db4q C4s Bb3s Ab3s G3s | Bb3q Ab3q |
            """,
            chords = "Ab Eb7 | Ab Eb7 | Ab Fm | Eb | Bbm Eb7 | F7 | Bbm Eb7 | Eb7 Ab | " +
                "Ab Eb7 | Ab Eb7 | Ab Fm | Eb | Bbm Eb7 | F7 | Bbm Eb7 | Eb7 Ab |",
            source = MUTOPIA,
        ),
        Song(
            id = "sonatina-menuetto",
            title = "Menuetto in G",
            composer = "Ludwig van Beethoven",
            work = "Piano Sonata No. 20 in G major, Op. 49 No. 2 — II. Tempo di Menuetto",
            year = "1796",
            about = "The graceful minuet theme from one of Beethoven's two 'easy' sonatas — " +
                "he liked it enough to reuse it in his Septet.",
            timeSignature = "3/4",
            tempo = 116,
            key = "G major",
            style = BackingStyle.WALTZ,
            notation = """
                G4e. F#4s F#4h | F#4e. G4s G4h | G4e. B4s D5q. E5e | D5e C5e B4q G4e re |
                G4e. F#4s F#4h | F#4e. G4s G4h | G4e. B4s D5q. C#5e | C5e A4e G4q rq |
                G4e. F#4s F#4h | F#4e. G4s G4h | G4e. B4s D5q. E5e | D5e C5e B4q G4e re |
                G4e. F#4s F#4h | F#4e. G4s G4h | G4e. B4s D5q D5s C#5s D5s C#5s | D5s C5s B4s A4s G4q rq |
            """,
            chords = "G D . | D7 G . | G D . | D7 G . | G D . | D7 G . | G . D7 | D7 G . | " +
                "G D . | D7 G . | G D . | D7 G . | G D . | D7 G . | G . D7 | D7 G . |",
            source = MUTOPIA,
        ),
        Song(
            id = "bach-minuet",
            title = "Minuet in G",
            composer = "J. S. Bach / C. Petzold",
            work = "Minuet in G major, BWV Anh. 114 — from the Notebook for Anna Magdalena Bach",
            year = "1725",
            about = "Long credited to Bach and now thought to be by Christian Petzold. " +
                "A favourite first piece on every instrument; the second half visits D major.",
            timeSignature = "3/4",
            tempo = 120,
            key = "G major",
            style = BackingStyle.WALTZ,
            notation = """
                D5q G4e A4e B4e C5e | D5q G4q G4q | E5q C5e D5e E5e F#5e | G5q G4q G4q |
                C5q D5e C5e B4e A4e | B4q C5e B4e A4e G4e | F#4q G4e A4e B4e G4e | A4h. |
                D5q G4e A4e B4e C5e | D5q G4q G4q | E5q C5e D5e E5e F#5e | G5q G4q G4q |
                C5q D5e C5e B4e A4e | B4q C5e B4e A4e G4e | A4q B4e A4e G4e F#4e | G4h. |
                B5q G5e A5e B5e G5e | A5q D5e E5e F#5e D5e | G5q E5e F#5e G5e D5e | C#5q B4e C#5e A4q |
                A4e B4e C#5e D5e E5e F#5e | G5q F#5q E5q | F#5q A4q C#5q | D5h. |
                D5q G4e F#4e G4q | E5q G4e F#4e G4q | D5q C5q B4q | A4e G4e F#4e G4e A4q |
                D4e E4e F#4e G4e A4e B4e | C5q B4q A4q | B4e D5e G4q F#4q | G4h. |
            """,
            chords = "G | G | C | G | Am | G | D | D | G | G | C | G | Am | G | D7 | G | " +
                "G | D | Em | A | A | A7 | D . A | D | G | C | G | D | D | Am | G . D | G |",
            source = MUTOPIA,
        ),
        Song(
            id = "brahms-lullaby",
            title = "Lullaby",
            composer = "Johannes Brahms",
            work = "Wiegenlied (Guten Abend, gut' Nacht), Op. 49 No. 4",
            year = "1868",
            about = "Brahms's cradle song, written for a friend's new baby. Gentle and narrow in range — " +
                "a lovely piece for working on a soft, even tone.",
            timeSignature = "3/4",
            tempo = 84,
            key = "C major",
            pickupBeats = 1.0,
            style = BackingStyle.FLOWING,
            notation = """
                E4e E4e | G4q. E4e E4q | G4h E4e G4e | C5q B4q. A4e | A4q G4q D4e E4e |
                F4q D4q D4e E4e | F4q rq D4e F4e | B4e A4e G4q B4q | C5q rq C4e C4e |
                C5h A4e F4e | G4h E4e C4e | F4q G4q A4q | E4e G4q. C4e C4e |
                C5h A4e F4e | G4h E4e C4e | F4e G4s F4s E4q D4q | C4h
            """,
            chords = "- | C Em . | C Em . | C Em . | Dm G . | Dm G . | Dm G7 . | G7 | C . C7 | " +
                "F | C | Dm G . | C . C7 | F | C | Dm Em G7 | C",
            source = MUTOPIA,
        ),
        Song(
            id = "blue-danube",
            title = "The Blue Danube",
            composer = "Johann Strauss II",
            work = "An der schönen blauen Donau, Op. 314 — first waltz",
            year = "1866",
            about = "The world's most famous waltz. The tune leaps an octave every four bars, " +
                "so it is a good workout for moving cleanly between the low and high holes.",
            timeSignature = "3/4",
            tempo = 150,
            key = "C major",
            pickupBeats = 1.0,
            style = BackingStyle.WALTZ,
            notation = """
                C4q | C4q E4q G4q | G4h G5q | G5h E5q | E5h C4q | C4q E4q G4q | G4h G5q | G5h F5q |
                F5h B3q | B3q D4q A4q | A4h A5q | A5h F5q | F5h B3q | B3q D4q A4q | A4h A5q | A5h E5q |
                E5h C4q | C4q E4q G4q | C5h C6q | C6h G5q | G5h C4q | C4q E4q G4q | C5h C6q | C6h A5q |
                A5h D4q | D4q F4q A4q | A4h.~ | A4q F#4q G4q | E5h.~ | E5q C5q E4q | E4h D4q | A4h G4q | C5h
            """,
            chords = "- | C | C | C | C | C | C | G7 | G7 | G7 | G7 | G7 | G7 | G7 | G7 | C | C | " +
                "C | C | C | C | C | C | C | F | Dm | Dm | G7 | C | C | F | G7 | C",
            source = MUTOPIA,
        ),
        Song(
            id = "twinkle",
            title = "Twinkle Variations — Theme",
            composer = "Wolfgang Amadeus Mozart",
            work = "12 Variations on 'Ah! vous dirai-je, maman', K. 265",
            year = "1782",
            about = "The French tune Mozart wrote twelve variations on — the same melody as " +
                "'Twinkle, Twinkle, Little Star'. All in holes 4 to 6, all natural notes.",
            timeSignature = "2/4",
            tempo = 100,
            key = "C major",
            style = BackingStyle.MARCH,
            notation = """
                C5q C5q | G5q G5q | A5q A5q | G5h | F5q F5q | E5q E5q | D5q D5q | C5h |
                G5q G5q | F5q F5q | E5q E5q | D5h | G5q G5q | F5q F5q | E5q E5q | D5h |
                C5q C5q | G5q G5q | A5q A5q | G5h | F5q F5q | E5q E5q | D5q D5q | C5h |
            """,
            chords = "C | C | F | C | F | C | G7 | C | C | G7 | C | G | C | G7 | C | G | " +
                "C | C | F | C | F | C | G7 | C |",
            source = "Traditional French melody, as used by Mozart.",
        ),
        Song(
            id = "largo",
            title = "Largo — Going Home",
            composer = "Antonín Dvořák",
            work = "Symphony No. 9 'From the New World', Op. 95 — II. Largo",
            year = "1893",
            about = "The cor anglais solo from the New World Symphony, later sung as 'Goin' Home'. " +
                "Slow and almost pentatonic, it sits beautifully on the harmonica without a single bend.",
            timeSignature = "4/4",
            tempo = 52,
            key = "Ab major",
            style = BackingStyle.PAD,
            notation = """
                C5e. Eb5s Eb5q C5e. Bb4s Ab4q | Bb4e. C5s Eb5e. C5s Bb4h | C5e. Eb5s Eb5q C5e. Bb4s Ab4q | Bb4e C5e Bb4e. Ab4s Ab4h |
                F5e. Ab5s Ab5q G5e Eb5e F5q | F5e Ab5e G5e Eb5e F5h | F5e. Ab5s Ab5q G5e Eb5e F5q | F5e Ab5e G5e Eb5e F5h |
                C5e. Eb5s Eb5q C5e Bb4e Ab4q | Bb4e. C5s Eb5e. C5s Bb4h | C5e. Eb5s Eb5q Ab5e. Bb5s C6q | Bb5e. Ab5s Bb5e F5e Ab5h |
            """,
            chords = "Ab | Eb | Ab | Eb Ab | Fm | Cm Fm | Fm | Cm Fm | Ab | Eb | Ab | Db Ab |",
            source = "Cor anglais part, checked against the Mutopia Project edition (mutopiaproject.org).",
        ),
        Song(
            id = "canon-in-d",
            title = "Canon in D",
            composer = "Johann Pachelbel",
            work = "Canon and Gigue in D major — Canon",
            year = "c. 1680",
            about = "The violin lines that unfold over Pachelbel's eight-chord ground bass, " +
                "from the slow opening to the first run of eighth notes.",
            timeSignature = "4/4",
            tempo = 66,
            key = "D major",
            style = BackingStyle.FLOWING,
            notation = """
                F#5q E5q D5q C#5q | B4q A4q B4q C#5q | D5q C#5q B4q A4q | G4q F#4q G4q E4q |
                D5e C#5e D5e D4e C#4e A4e E4e F#4e | D4e D5e C#5e B4e C#5e F#4e A4e B4e | G4e F#4e E4e G4e F#4e E4e D4e C#5e | B4e A4e G4e F#4e E4e G4e F#4e E4e |
                F#5q E5q D5q C#5q | B4q A4q B4q C#5q | D5w |
            """,
            chords = "D A Bm F#m | G D G A | D A Bm F#m | G D G A | D A Bm F#m | G D G A | D A Bm F#m | G D G A | " +
                "D A Bm F#m | G D G A | D |",
            source = "Violin lines as commonly played; the eighth-note passage follows the Mutopia Project edition.",
        ),
    )

    /** Library order: Beethoven first, then roughly by period. */
    private val ORDER = listOf(
        "ode-to-joy", "fur-elise", "symphony-5", "symphony-7", "moonlight", "pathetique", "pathetique-rondo",
        "violin-concerto", "sonatina-menuetto",
        "canon-in-d", "vivaldi-spring", "vivaldi-summer", "vivaldi-autumn", "vivaldi-winter",
        "bach-minuet", "bach-air", "emperor-hymn",
        "eine-kleine", "eine-kleine-romanze", "eine-kleine-menuetto", "rondo-alla-turca", "k331-theme",
        "sonata-facile", "twinkle", "minuet-k2", "horn-concerto-4", "deh-vieni", "ave-verum",
        "schubert-ave-maria", "william-tell", "traumerei", "gounod-ave-maria", "chopin-prelude-7", "wedding-march", "brindisi",
        "brahms-lullaby", "toreador", "blue-danube", "largo", "stars-and-stripes", "gymnopedie", "entertainer", "jupiter",
    )

    val songs: List<Song> = (firstBatch + MoreSongs.songs + EvenMoreSongs.songs).sortedBy { s ->
        ORDER.indexOf(s.id).let { if (it < 0) Int.MAX_VALUE else it }
    }

    fun byId(id: String): Song? = songs.firstOrNull { it.id == id }
}
