package app.harmonicaplayalong.audio

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class PitchResult(val frequency: Double, val clarity: Double, val rms: Double) {
    val voiced: Boolean get() = frequency > 0
}

/**
 * YIN pitch detector (de Cheveigné & Kawahara, 2002) for a single melody line.
 * [bufferSize] samples are analysed at once; half of them is the integration window.
 */
class Yin(
    private val sampleRate: Int,
    private val bufferSize: Int,
    minFrequency: Double = 150.0,
    maxFrequency: Double = 2800.0,
) {
    private val window = bufferSize / 2
    private val tauMin = max(2, floor(sampleRate / maxFrequency).toInt())
    private val tauMax = min(window - 1, ceil(sampleRate / minFrequency).toInt())
    private val diff = FloatArray(tauMax + 2)

    /** Lower is stricter. */
    var threshold = 0.15f

    /** Signals quieter than this RMS are treated as silence. */
    var silence = 0.004

    fun detect(buf: FloatArray): PitchResult {
        require(buf.size >= bufferSize)
        var sum = 0.0
        for (i in 0 until bufferSize) sum += buf[i] * buf[i]
        val rms = sqrt(sum / bufferSize)
        if (rms < silence) return PitchResult(0.0, 0.0, rms)

        // Difference function
        for (tau in 1..tauMax) {
            var d = 0f
            for (j in 0 until window) {
                val delta = buf[j] - buf[j + tau]
                d += delta * delta
            }
            diff[tau] = d
        }
        // Cumulative mean normalised difference
        diff[0] = 1f
        var running = 0f
        for (tau in 1..tauMax) {
            running += diff[tau]
            diff[tau] = if (running == 0f) 1f else diff[tau] * tau / running
        }
        // Absolute threshold, then walk down to the local minimum
        var tau = -1
        var t = tauMin
        while (t <= tauMax) {
            if (diff[t] < threshold) {
                while (t + 1 <= tauMax && diff[t + 1] < diff[t]) t++
                tau = t
                break
            }
            t++
        }
        if (tau < 0) {
            // No clear period; accept the global minimum only when it is still fairly clean.
            var best = tauMin
            for (k in tauMin..tauMax) if (diff[k] < diff[best]) best = k
            if (diff[best] > 0.3f) return PitchResult(0.0, 1.0 - diff[best], rms)
            tau = best
        }
        // Parabolic interpolation
        val better = if (tau in 1 until tauMax) {
            val s0 = diff[tau - 1]
            val s1 = diff[tau]
            val s2 = diff[tau + 1]
            val denom = 2f * (2f * s1 - s2 - s0)
            if (denom != 0f) tau + (s2 - s0) / denom else tau.toFloat()
        } else {
            tau.toFloat()
        }
        return PitchResult(sampleRate / better.toDouble(), 1.0 - diff[tau], rms)
    }
}
