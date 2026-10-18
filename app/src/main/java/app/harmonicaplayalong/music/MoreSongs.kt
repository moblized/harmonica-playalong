package app.harmonicaplayalong.music

/**
 * The second batch of masterpieces. Like the first batch, melodies and harmonies were taken from the
 * Mutopia Project's editions of the original scores.
 */
internal object MoreSongs {

    private const val MUTOPIA = "Notes checked against the Mutopia Project edition (mutopiaproject.org)."

    val songs: List<Song> = listOf(
        Song(
            id = "pathetique-rondo",
            title = "Pathétique — Rondo",
            composer = "Ludwig van Beethoven",
            work = "Piano Sonata No. 8 in C minor, Op. 13 — III. Rondo: Allegro",
            year = "1798",
            about = "The restless rondo theme that closes the 'Pathétique', from its three-note pickup " +
                "to the falling scale runs. Minor-key and quick — take it slowly first.",
            timeSignature = "2/2",
            tempo = 132,
            key = "C minor",
            pickupBeats = 1.5,
            style = BackingStyle.MARCH,
            notation = """
                G4e C5e D5e | Eb5q. F5e D5q. Eb5e | C5h C5e B4e C5e D5e | Eb5e D5e Eb5e F5e G5q G5q | G5h. F5e G5e |
                Ab5h D5q Eb5e F5e | G5h C5q C5e D5e | Eb5q Eb5e F5e D5q D5e Eb5e | C5q rq rq F5e G5e |
                Ab5h D5q Eb5e F5e | G5h C5q C5e D5e | Eb5q Eb5e F5e D5q D5e Eb5e | C5q Bb5h Bb5q~ |
                Bb5e Ab5e G5e F5e Eb5e D5e C5e B4e | C5q C6h Bb5q~ | Bb5e Ab5e G5e F5e Eb5e D5e C5e B4e | C5q D5q Eb5q B5q | C6h
            """,
            chords = "- | Cm G7 | Cm | Cm G | G | Ddim | Eb Ab7 | Cm G | Cm | Fm | Cm | Cm G | C7 | " +
                "Fm G7 | Ab C7 | Fm G | G | Cm",
            source = MUTOPIA,
        ),
        Song(
            id = "violin-concerto",
            title = "Violin Concerto — Rondo",
            composer = "Ludwig van Beethoven",
            work = "Violin Concerto in D major, Op. 61 — III. Rondo: Allegro",
            year = "1806",
            about = "The hunting-horn tune the soloist plays to open the finale of Beethoven's only violin concerto, " +
                "a bouncy 6/8 dance that sits nicely on the harmonica.",
            timeSignature = "6/8",
            tempo = 126,
            key = "D major",
            pickupBeats = 0.5,
            style = BackingStyle.MARCH,
            notation = """
                D4e | A3q D4e F#4e A4e D4e | A3q D4e F#4e A4e G4e | E4q F#4e D4q E4e | C#4e C#4e D4e E4e re D4e |
                A3q D4e F#4e A4e D4e | A3q D4e F#4e A4e G4e | E4q F#4e D4q E4e | B3e E4e C#4e D4q.
            """,
            chords = "- | D | D | A7 D | A7 D | D | D | A7 D | A7 D",
            source = "$MUTOPIA From the solo violin part.",
        ),
        Song(
            id = "bach-air",
            title = "Air on the G String",
            composer = "Johann Sebastian Bach",
            work = "Orchestral Suite No. 3 in D major, BWV 1068 — Air",
            year = "c. 1730",
            about = "The first half of Bach's famous Air, played twice: a long held note, then a line that winds down " +
                "in ornaments. Slow, but the quick notes need a light touch.",
            timeSignature = "4/4",
            tempo = 54,
            key = "D major",
            style = BackingStyle.FLOWING,
            notation = """
                F#5w~ | F#5e B5s G5z F#5z E5s D5s C#5s D5s C#5e.~ C#5z B4z A4q | A5h~ A5s F#5s C5s B4s E5s D#5s A5s G5s | G5h~ G5s E5s B4s A4s D5s C#5s G5s F#5s | F#5q. G#5s A5s D5e D5z E5z F#5e E5s E5s D5s | C#5s B4s B4z C#5z D5e. C#5s B4s A4h |
                F#5w~ | F#5e B5s G5z F#5z E5s D5s C#5s D5s C#5e.~ C#5z B4z A4q | A5h~ A5s F#5s C5s B4s E5s D#5s A5s G5s | G5h~ G5s E5s B4s A4s D5s C#5s G5s F#5s | F#5q. G#5s A5s D5e D5z E5z F#5e E5s E5s D5s | C#5s B4s B4z C#5z D5e. C#5s B4s A4h |
            """,
            chords = "D Bm | G A7 | Am B7 | A7 | F#m E7 | A | D Bm | G A7 | Am B7 | A7 | F#m E7 | A |",
            source = "$MUTOPIA From the flute line of the flute-and-guitar edition.",
        ),
        Song(
            id = "gounod-ave-maria",
            title = "Ave Maria (Bach–Gounod)",
            composer = "Charles Gounod / J. S. Bach",
            work = "Ave Maria — melody over Bach's Prelude in C major, BWV 846",
            year = "1853",
            about = "Gounod's soaring melody laid over the first prelude of Bach's Well-Tempered Clavier. " +
                "The band plays Bach's rippling arpeggios underneath.",
            timeSignature = "4/4",
            tempo = 60,
            key = "C major",
            style = BackingStyle.FLOWING16,
            notation = """
                E5w | F5w | G5h. D5q | E5h. rq |
                A5h~ A5e A4e B4e C5e | D5q.~ D5s E5s D5q rq | G5h~ G5e G4e A4e B4e | C5q.~ C5s D5s C5q rq |
                C6h~ C6e C5e D5e E5e | F#5q. E5e D5q A4q | B4h~ B4e re D5q | E5h~ E5e E5e F5e G5e |
                A5h A4q rq | D5h~ D5e D5e E5e F5e | G5h G4q rq | C5h~ C5e C5e D5e E5e |
                F5h~ F5e F5e G5e A5e | B5q. A5e G5q D5q | E5h. re. E5s | G5h E5q re. E5s |
                A5h A4q re. A5s | A5h C5q re. A5s | C6h Eb5q re. C6s | C6h D5q re. D5s |
                D5h~ D5e D5e C5e B4e | G5q. E5e C5q rq | F5h~ F5e F5e E5e D5e | D6q. B5e G5h |
                A5h~ A5e A5e B5e C6e | E6h~ E6e C6e G5e E5e | D5h~ D5e A5e B5e A5e | A5e G5e F5e D5e B4e G4e F4e D4e |
                C4h~ C4e rq.
            """,
            chords = "C | Dm7 | G7 | C | Am | D7 | G | C | Am | D7 | G | C#dim | Dm | Fdim | C | F | Dm | G7 | C | C7 | " +
                "F | F#dim | Cm | Fm | G7 | C | G | G7 | F#dim | C | G | G7 | C",
            source = "$MUTOPIA From the violin-and-piano edition.",
        ),
        Song(
            id = "schubert-ave-maria",
            title = "Ave Maria (Schubert)",
            composer = "Franz Schubert",
            work = "Ellens dritter Gesang, D. 839",
            year = "1825",
            about = "Schubert's song to the Virgin, over the gently rocking triplets of the piano. " +
                "The melody is full of small ornaments — slow it right down while you learn them.",
            timeSignature = "4/4",
            tempo = 52,
            key = "Bb major",
            style = BackingStyle.TRIPLETS,
            notation = """
                Bb4q. A4s Bb4s D5q.~ D5s C5s | Bb4q rq C5q Bb4s A4s G4s A4s | Bb4q re D5e D5e. C5z Bb4z A4s G4s D5s E5s | D5q C#5e. A4s C5e. Bb4s A4u C5u D5u Eb5u C5u A4u |
                Bb4q. D5s C5s C5e. A4s G4u B4u D5u F5u D5u B4u | C5q~ C5u G4u A4u Bb4u A4u G4u F4q re F4e | C5e. C5s C5s. B4z C5s. D5z C5s. D5z Bb4e re Bb4e | C5e. C5s C5u B4u C5u Eb5u D5u C5u Bb4q re Bb4e |
                C5e. C5s D5s. D5z D5u C5u D5u Eb5e Eb5e re. G4s | C5e. C5s Bb4u A4u Bb4u Db5u C5u Bb4u C5q. re | Bb4q. A4s Bb4s D5q.~ D5s C5s | Bb4q rq rh |
            """,
            chords = "Bb | Gm F#dim | Gm | F | C7 | F | F7 Bb | F7 Gm | D7 Cm | C7 F | Bb | Bb |",
            source = MUTOPIA,
        ),
        Song(
            id = "eine-kleine",
            title = "Eine kleine Nachtmusik",
            composer = "Wolfgang Amadeus Mozart",
            work = "Serenade No. 13 in G major, K. 525 — I. Allegro",
            year = "1787",
            about = "Mozart's 'little night music': the bold opening fanfare, the bubbling 'rocket' theme " +
                "and the gentle answer that follows.",
            timeSignature = "4/4",
            tempo = 120,
            key = "G major",
            style = BackingStyle.MARCH,
            notation = """
                G5q re D5e G5q re D5e | G5e D5e G5e B5e D6q rq | C6q re A5e C6q re A5e | C6e A5e F#5e A5e D5q rq |
                G5e re G5q. B5e A5e G5e | G5e F#5e F#5q. A5e C6e F#5e | A5e G5e G5q. B5e A5e G5e | G5e F#5e F#5q. A5e C6e F#5e | G5e G5e F#5e E5s F#5s G5e G5e A5e G5s A5s | B5e B5e C6e B5s C6s D6q rq |
                D5h E5h | C5q C5q B4q B4q | A4q A4q G4e F#4e E4e F#4e | G4e re A4e re B4e re rq |
                D5h E5h | D5e C5e C5e C5e C5e B4e B4e B4e | B4e A4e A4e A4e G4e F#4e E4e F#4e | G4h rh |
            """,
            chords = "G | G | D7 | D | G | D7 | G | D7 | G D7 | G D7 G . | G C | C G | D7 . G D7 | G D7 G . | " +
                "G C | D7 G | G D7 C D7 | G |",
            source = "$MUTOPIA From the first violin part.",
        ),
        Song(
            id = "rondo-alla-turca",
            title = "Rondo alla Turca",
            composer = "Wolfgang Amadeus Mozart",
            work = "Piano Sonata No. 11 in A major, K. 331 — III. Alla Turca",
            year = "1783",
            about = "The 'Turkish March': Mozart imitating a Janissary band. Lots of quick turns around A, " +
                "so it is a real workout for bends.",
            timeSignature = "2/4",
            tempo = 112,
            key = "A minor",
            pickupBeats = 1.0,
            style = BackingStyle.MARCH,
            notation = """
                B4s A4s G#4s A4s | C5q D5s C5s B4s C5s | E5q F5s E5s D#5s E5s | B5s A5s G#5s A5s B5s A5s G#5s A5s | C6q A5e C6e |
                B5e A5e G5e A5e | B5e A5e G5e A5e | B5e A5e G5e F#5e | E5q E5e F5e |
                G5e G5e A5s G5s F5s E5s | D5q E5e F5e | G5e G5e A5s G5s F5s E5s | D5q C5e D5e |
                E5e E5e F5s E5s D5s C5s | B4q C5e D5e | E5e E5e F5s E5s D5s C5s | B4q B4s A4s G#4s A4s |
                C5q D5s C5s B4s C5s | E5q F5s E5s D#5s E5s | B5s A5s G#5s A5s B5s A5s G#5s A5s | C6q A5e B5e |
                C6e B5e A5e G#5e | A5e E5e F5e D5e | C5q B4e A4s B4s | A4q
            """,
            chords = "- | Am | Am | Am | Am | Em | Em | B | E | C | G | C | G | Am | E | Am | E | " +
                "Am | Am | Am | Am | Am E | Am Dm | Am E | Am",
            source = MUTOPIA,
        ),
        Song(
            id = "sonata-facile",
            title = "Sonata facile",
            composer = "Wolfgang Amadeus Mozart",
            work = "Piano Sonata No. 16 in C major, K. 545 — I. Allegro",
            year = "1788",
            about = "The opening of Mozart's 'easy' sonata: a singing four-bar tune, then scales running " +
                "down and up again. The band plays the Alberti bass underneath.",
            timeSignature = "4/4",
            tempo = 108,
            key = "C major",
            style = BackingStyle.FLOWING,
            notation = """
                C5h E5q G5q | B4q. C5s D5s C5q rq | A5h G5q C6q | G5q F5e E5s F5s E5q rq |
                A4e B4s C5s D5s E5s F5s G5s A5s G5s F5s E5s D5s C5s B4s A4s | G4e A4s B4s C5s D5s E5s F5s G5s F5s E5s D5s C5s B4s A4s G4s | F4e G4s A4s B4s C5s D5s E5s F5s E5s D5s C5s B4s A4s G4s F4s | E4e F4s G4s A4s B4s C5s D5s E5s D5s C5s B4s A4s G4s F4s E4s |
                D4e E4s F4s G4s A4s B4s C#5s D5s A4s B4s C#5s D5s E5s F5s G5s | A5s B5s C6s B5s A5s G5s F5s E5s F5s G5s A5s G5s F5s E5s D5s C5s | B4e G5e E5e C5e D5e G5e E5e C5e | D5q G5q G4q rq |
            """,
            chords = "C | G7 C | F C | G7 C | F | C | Dm | C | F | F | G | G |",
            source = MUTOPIA,
        ),
        Song(
            id = "emperor-hymn",
            title = "Emperor Hymn",
            composer = "Joseph Haydn",
            work = "Gott erhalte Franz den Kaiser, Hob. XXVIa:43 — also the slow movement of the 'Emperor' Quartet, Op. 76 No. 3",
            year = "1797",
            about = "Haydn's stately hymn, written for the Austrian emperor and later used as the German national anthem. " +
                "Steady, singable and almost all in the middle of the harmonica.",
            timeSignature = "4/4",
            tempo = 76,
            key = "Eb major",
            style = BackingStyle.PAD,
            notation = """
                Eb4q. F4e G4q F4q | Ab4q G4q F4e D4e Eb4q | C5q Bb4q Ab4q G4q | F4q G4e Eb4e Bb4h |
                Eb4q. F4e G4q F4q | Ab4q G4q F4e D4e Eb4q | C5q Bb4q Ab4q G4q | F4q G4e Eb4e Bb4h |
                F4q G4q F4e D4e Bb3q | Ab4q G4q F4e D4e Bb3q | Bb4q Ab4q G4q. G4e | A4q. A4e Bb4h |
                Eb5q. D5e C5q Bb4q | C5q. Bb4e Bb4e Ab4e G4q | F4q G4e Ab4e Bb4e C5e Ab4e F4e | Eb4q G4e F4e Eb4h |
            """,
            chords = "Eb Bb | Bb7 | Eb Bb7 | Ab Bb | Eb Bb | Bb7 | Eb Bb7 | Ab Bb | " +
                "Bb | Bb7 Bb | Gm Cm | F7 Bb | Eb | Ab Eb | Bb7 Ab | Bb Eb |",
            source = "$MUTOPIA From the soprano line of the hymn setting.",
        ),
        Song(
            id = "wedding-march",
            title = "Wedding March",
            composer = "Felix Mendelssohn",
            work = "A Midsummer Night's Dream, Op. 61 — Wedding March",
            year = "1842",
            about = "The trumpet fanfare and the grand tune that have walked couples out of church ever since. " +
                "The fanfare's triplets are good tonguing practice.",
            timeSignature = "4/4",
            tempo = 108,
            key = "C major",
            pickupBeats = 1.0,
            style = BackingStyle.MARCH,
            notation = """
                C4t C4t C4t | C4h rq C4t C4t C4t | C4h rq C4t C4t C4t | E4q E4t E4t E4t E4q E4t E4t E4t | G4q G4t G4t G4t G4q rq |
                C6h B5q. F#5e | A5q G5q F5q D5q | C5h D5q G4e. D5s | E5q rq. C5e E5e G5e |
                C6h B5q. F#5e | A5q G5q F5q D5q | C5h E5q D5e. E5s | D5h C5q rq
            """,
            chords = "- | C | C | C | C | Am B | Em Dm | C G7 | C | Am B | Em Dm | C | G7 C",
            source = "$MUTOPIA From the piano edition.",
        ),
        Song(
            id = "jupiter",
            title = "Jupiter — I Vow to Thee, My Country",
            composer = "Gustav Holst",
            work = "The Planets, Op. 32 — Jupiter (the hymn tune 'Thaxted')",
            year = "1917",
            about = "The broad central tune of 'Jupiter', which Holst later set as a hymn. " +
                "Long phrases that reward a big, steady breath.",
            timeSignature = "3/4",
            tempo = 84,
            key = "Bb major",
            pickupBeats = 1.0,
            style = BackingStyle.PAD,
            notation = """
                D4e F4e | G4q. Bb4e A4e. F4s | Bb4e C5e Bb4q A4q | G4e A4e G4q F4q |
                D4h D4e F4e | G4q. Bb4e A4e. F4s | Bb4e C5e D5q D5q | D5e C5e Bb4q C5q |
                Bb4h F4e D4e | C4q. C4e Bb3e D4e | C4q F4q F4e D4e | C4q. C4e D4e F4e |
                G4h G4e A4e | Bb4e. Bb4s A4q G4q | F4q Bb4q D4q | C4e Bb3e C4q D4q |
                F4h D4e F4e | G4q. Bb4e A4e. F4s | Bb4e C5e Bb4q A4q | G4e A4e G4q F4q |
                D4h D4e F4e | G4q. Bb4e A4e. F4s | Bb4e C5e D5q D5q | D5e C5e Bb4q C5q |
                Bb4h
            """,
            chords = "Bb | Cm | Bb | Eb | Gm | Cm | Bb | Eb | Bb | F | Dm | F | Cm | Eb | Bb | Eb | F | " +
                "Cm | Bb | Eb | Gm | Eb | Bb | Eb | Bb",
            source = "$MUTOPIA From Holst's hymn setting 'Thaxted'.",
        ),
        Song(
            id = "gymnopedie",
            title = "Gymnopédie No. 1",
            composer = "Erik Satie",
            work = "Trois Gymnopédies — No. 1, Lent et douloureux",
            year = "1888",
            about = "Satie's slow, dreamy waltz over swaying chords. Very slow with long held notes — " +
                "ideal for working on a soft, steady tone.",
            timeSignature = "3/4",
            tempo = 72,
            key = "D major",
            style = BackingStyle.WALTZ,
            notation = """
                rq F#5q A5q | G5q F#5q C#5q | B4q C#5q D5q | A4h. | F#4h.~ | F#4h.~ | F#4h.~ | F#4h. |
                rq F#5q A5q | G5q F#5q C#5q | B4q C#5q D5q | A4h. | C#5h. | F#5h. | E4h.~ | E4h.~ | E4h. |
                A4q B4q C5q | E5q D5q B4q | D5q C5q B4q | D5h.~ | D5h D5q | E5q F5q G5q | A5q C5q D5q | E5q D5q B4q | D5h.~ | D5h D5q |
                G5h. | F#5h. | B4q A4q B4q | C#5q D5q E5q | C#5q D5q E5q | F#4h. | C5h. | D5h. |
            """,
            chords = "G | D | G | D | G | D | G | D | G | D | G | D | F#m | Bm | Em | Em | Dm | " +
                "Am | E7 | G | D7 | D7 | Dm | D7 | E7 | D7 | D7 | " +
                "Em | F#m | Bm | A | A | E7 | Am | D |",
            source = MUTOPIA,
        ),
        Song(
            id = "chopin-prelude-7",
            title = "Prelude in A major",
            composer = "Frédéric Chopin",
            work = "24 Preludes, Op. 28 — No. 7, Andantino",
            year = "1839",
            about = "One of Chopin's shortest pieces: eight little mazurka phrases, each with the same lilting rhythm. " +
                "A good first taste of Chopin.",
            timeSignature = "3/4",
            tempo = 66,
            key = "A major",
            pickupBeats = 1.0,
            style = BackingStyle.WALTZ,
            notation = """
                E4q | C#5e. D5s B4q B4q | B4h F#5q | D#5e. E5s A5q A5q | A5h C#5q | A#4e. B4s D5q D5q | D5h G#4q | G#4e. A4s C#5q C#5q | C#5h E4q |
                C#5e. D5s B4q B4q | B4h F#5q | D#5e. E5s C#6q C#6q | C#6h C#5q | C#5e. D5s F#5q F#5q | F#5h G#4q | B4e. A4s A5q A5q | A5h
            """,
            chords = "- | E7 | E7 | A | A | E7 | E7 | A | A | E7 | E7 | A | F#7 | Bm | E7 | A | A",
            source = MUTOPIA,
        ),
        Song(
            id = "entertainer",
            title = "The Entertainer",
            composer = "Scott Joplin",
            work = "The Entertainer — A Rag Time Two Step",
            year = "1902",
            about = "Joplin's most famous rag (marked 'Not fast'). The first strain, with its syncopated " +
                "leaps — chromatic, so bends come in handy.",
            timeSignature = "2/4",
            tempo = 80,
            key = "C major",
            pickupBeats = 0.5,
            style = BackingStyle.MARCH,
            notation = """
                D4s D#4s | E4s C5e E4s C5e E4s C5s~ | C5q~ C5s C6s D6s D#6s | E6s C6s D6s E6e B5s D6e | C6q. D4s D#4s |
                E4s C5e E4s C5e E4s C5s~ | C5q. A5s G5s | F#5s A5s C6s E6e D6s C6s A5s | D6q. D4s D#4s |
                E4s C5e E4s C5e E4s C5s~ | C5q~ C5s C6s D6s D#6s | E6s C6s D6s E6e B5s D6e | C6q. C6s D6s |
                E6s C6s D6s E6e C6s D6s C6s | E6s C6s D6s E6e C6s D6s C6s | E6s C6s D6s E6e B5s D6e | C6q.
            """,
            chords = "- | C C7 | F C | C G7 | C | C C7 | F C | D7 | G7 | C C7 | F C | C G7 | C | C C7 | F Fm | C G7 | C",
            source = MUTOPIA,
        ),
        Song(
            id = "toreador",
            title = "Toreador Song",
            composer = "Georges Bizet",
            work = "Carmen — the Toreador refrain, as it appears in the Prelude",
            year = "1875",
            about = "'Toréador, en garde!' — Escamillo's swaggering refrain, which Bizet also puts in the opera's Prelude. " +
                "Proud dotted rhythms and one long held E.",
            timeSignature = "2/4",
            tempo = 104,
            key = "F major",
            style = BackingStyle.MARCH,
            notation = """
                C5q D5e. C5s | A4q A4q | A4e. G4s A4e. Bb4s | A4q. re | Bb4q G4e. C5s | A4q. re |
                F4q D4e. G4s | C4q. re | G4h | G4e D5e C5e Bb4e | A4e G4e A4e Bb4e | A4q. re |
                E4q A4q | A4q G#4e. B4s | E5h~ | E5h~ | E5e D5u E5u D5u C5e D5e | G4e A4e Bb4q~ |
                Bb4e A4u Bb4u A4u F4e D5e | C5q. re | F4h |
            """,
            chords = "F D7 | F | F | F | C7 | F | G7 | C | Gm | Gm | Dm | Dm | Am | B7 E7 | A | A | G7 | Gm | C7 Dm | C7 | F |",
            source = "$MUTOPIA From the piano edition of the Prelude; the final F is added so the tune ends at home.",
        ),
        Song(
            id = "brindisi",
            title = "Brindisi — Libiamo",
            composer = "Giuseppe Verdi",
            work = "La traviata — Act I, 'Libiamo ne' lieti calici'",
            year = "1853",
            about = "The drinking song from La traviata: a lilting one-in-a-bar waltz that Alfredo starts and the whole party joins. " +
                "This is his first verse.",
            timeSignature = "3/8",
            tempo = 100,
            key = "Bb major",
            pickupBeats = 0.5,
            style = BackingStyle.WALTZ,
            notation = """
                F4e | D5q.~ | D5e F4e D5e | D5e F4e D5e | D5e C#5e D5e | F5q.~ | F5e Eb5e D5e | C5e C5s B4s C5s D5s | C5e C5s B4s C5s D5s | C5q Bb4e | F4e re F4e |
                D5q.~ | D5e F4e D5e | D5e F4e D5e | D5e C#5e D5e | F5q.~ | F5e Eb5e D5e | C5e C5s B4s C5s D5s | C5e C5s B4s C5s D5s | Bb4q re | re re Bb4e |
                Eb5q Eb5e | Eb5e Eb5s D5s Eb5s F5s | Eb5q D5e | D5q F4e | C5q C5e | C5e C5s B4s C5s D5s | C5q. | Bb4e re Bb4e |
                Eb5q Eb5e | Eb5e Eb5s D5s Eb5s F5s | Eb5q. | D5q F5e | A4q A4e | A4e A4s G#4s A4s Bb4s | A4q.~ | A4e C5e F4e |
                D5q.~ | D5e F4e D5e | D5e F4e D5e | D5e C#5e D5e | G5q.~ | G5e F5e Eb5e | D5e D5s C#5s D5s Eb5s | C5e C5s B4s C5s D5s | Bb4q re
            """,
            chords = "- | Bb | Bb | Bb | Bb | Bb | Bb | F7 | F7 | Bb | Bb | " +
                "Bb | Bb | Bb | Bb | Bb | Bb | F7 | F7 | Bb | Bb | " +
                "Eb | Eb | Bb | Bb | F7 | F7 | F7 | Bb | " +
                "Eb | Eb | F7 | Dm | A | A | Dm | F7 | " +
                "Bb | Bb | Bb | Bb | G7 | Cm | Bb | F7 | Bb",
            source = "$MUTOPIA From the piano transcription's vocal line.",
        ),
        Song(
            id = "traumerei",
            title = "Träumerei",
            composer = "Robert Schumann",
            work = "Kinderszenen (Scenes from Childhood), Op. 15 — No. 7",
            year = "1838",
            about = "'Dreaming': Schumann's tender little reverie. Every phrase rises from a low C and floats down again — " +
                "play it softly and let the long notes sing.",
            timeSignature = "4/4",
            tempo = 76,
            key = "F major",
            pickupBeats = 1.0,
            style = BackingStyle.FLOWING,
            notation = """
                C4q | F4h~ F4e E4e F4e A4e | C5e F5e F5h E5e D5e | C5e F5e G4e A4e Bb4e D5e F4e G4e | A4e C5e G4h C4q |
                F4h~ F4e E4e F4e A4e | C5e A5e A5q. G5e F5e E5e | F5e A5e D5e F5e E5q. Eb5e | D5q E5e.~ E5z C5z C5q re C4e |
                F4h~ F4e E4e F4e A4e | C5e Eb5e Eb5h D5e C5e | Bb4e D5e G4e A4e Bb4q. A4e | G4q. D4q. re F4e |
                Bb4h~ Bb4e A4e Bb4e D5e | F5e Bb5e Bb5h A5e G5e | F5e A5e D5e E5e F5q. E5e | D5q. A4q. G4e.~ G4z C4z |
                F4h~ F4e E4e F4e A4e | C5e F5e F5h E5e D5e | C5e F5e G4e A4e Bb4e D5e F4e G4e | A4e C5e G4h C4q |
                F4h~ F4e E4e F4e A4e | C5e A5e A5q. G5e F5e D5e | C5e F5e G4e A4e Bb4e D5e G4e A4e | Bb4e D5e D4e E4e F4h
            """,
            chords = "- | F | Bb | Am G7 | F C7 | F | F Em | Dm C | G7 C | " +
                "F | Cm D7 | Gm | Gm D | Bb | Bb A7 | Dm | Dm C7 | " +
                "F | Bb | Am G7 | F C7 | F | F | Gm | C7 F",
            source = MUTOPIA,
        ),
        Song(
            id = "stars-and-stripes",
            title = "The Stars and Stripes Forever",
            composer = "John Philip Sousa",
            work = "The Stars and Stripes Forever (march) — Trio",
            year = "1896",
            about = "The broad trio tune of America's national march, the part everyone hums. " +
                "Long held notes over a brisk oom-pah — keep the breath steady.",
            timeSignature = "2/2",
            tempo = 200,
            key = "Ab major",
            pickupBeats = 2.0,
            style = BackingStyle.MARCH,
            notation = """
                Eb5h | Eb5h Db5q C5q | C5h B4q C5q | C5w~ | C5h B4q C5q | C5h B4q C5q | Eb5h C5q. Eb5e | Db5w | Bb4h Bb4h |
                Bb4h A4q Bb4q | Bb4h A4q Bb4q | Db5w~ | Db5h C5q Bb4q | C5q Eb5h. | F5h. F5q | Bb4w~ | Bb4h Eb5h |
                Eb5h Db5q C5q | C5h B4q C5q | C5w~ | C5h B4q C5q | C5h B4q C5q | Db5q C5q Bb4q. G5e | Bb4w | Ab4h Ab4h |
                Ab4h G4q Ab4q | Cb5h Bb4q Ab4q | Ab4w~ | Ab4q Ab4q Bb4q C5q | Eb5e re Ab4q Bb4q C5q | Eb5e re Eb4q F4q C5q | Bb4w | Ab4q rq rh
            """,
            chords = "- | Ab | Ab | Ab | Ab | Ab | Ab | Eb7 | Eb7 | " +
                "Eb7 | Eb7 | Eb7 | Eb7 | Ab | Db | Eb | Eb | " +
                "Ab | Ab | Ab | Ab | C7 | C7 | C7 | Fm | " +
                "Dbm | Fb | Ab | Ab | Ab | Ab | Eb7 | Ab",
            source = "$MUTOPIA From the piano edition (1897).",
        ),
    )
}
