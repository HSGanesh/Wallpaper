package com.hsganesh.chalkboardwallpaper

/**
 * A single handwritten formula/annotation placed on the board.
 * x/y are fractions of the board width/height (0f..1f) so the
 * layout scales cleanly across any screen size.
 */
data class FormulaItem(
    val text: String,
    val xFrac: Float,
    val yFrac: Float,
    val sizeSp: Float,
    val rotationDeg: Float = 0f,
    val alpha: Int = 235,
    val bold: Boolean = false
)

/**
 * The full bank of formulas/diagram captions used to fill the board.
 * Deliberately dense and varied (algebra, calculus, physics, stats) to
 * mirror a real handwritten study blackboard, while keeping a clear band
 * around the centre of the board free for the live clock.
 */
object FormulaBank {

    fun all(): List<FormulaItem> = listOf(
        // --- Top band ---
        FormulaItem("1/N \u03A3(x\u1d62 - x\u0304)\u00B2", 0.06f, 0.035f, 15f, -4f),
        FormulaItem("y = A sin(\u03C9t + \u03D5)", 0.58f, 0.02f, 15f, 2f),
        FormulaItem("\u03C7\u00B2 = \u03A3(O\u1d62-E\u1d62)\u00B2/E\u1d62", 0.54f, 0.145f, 15f, -2f),
        FormulaItem("x = (-b \u00B1 \u221A(b\u00B2-4ac)) / 2a", 0.40f, 0.19f, 14f, 1f),
        FormulaItem("F = m 4\u03C0\u00B2R / T\u00B2", 0.05f, 0.245f, 15f, -3f),
        FormulaItem("\u03D5(x)", 0.34f, 0.245f, 17f, 4f),
        FormulaItem("\u2207\u00B7E = 0", 0.75f, 0.205f, 15f, -2f),
        FormulaItem("\u2207\u00D7B = \u03BC\u2080J", 0.72f, 0.26f, 15f, 3f),
        FormulaItem("sin(\u03C0/2)", 0.55f, 0.295f, 16f, 2f),

        // --- Middle band, kept to the left/right edges so the clock stays clear ---
        FormulaItem("\u2202v/\u2202t + v\u00B7\u2207v = 0", 0.03f, 0.335f, 13f, -1f),
        FormulaItem("\u03A3\u221E n=1  n", 0.08f, 0.40f, 19f, -2f),
        FormulaItem("d/dx x\u00B2 = 2x", 0.82f, 0.355f, 16f, 2f),
        FormulaItem("det[5 1; 7 2]", 0.80f, 0.455f, 14f, 3f),
        FormulaItem("\u222B\u2080\u00B3 x\u00B2 dx", 0.04f, 0.52f, 18f, -3f),

        // --- Lower-middle band ---
        FormulaItem("i\u0127 \u2202\u03C8/\u2202t = -\u0127\u00B2/2m \u2207\u00B2\u03C8", 0.04f, 0.665f, 13f, -1f),
        FormulaItem("\u03A0\u00B3 n=1 (n+1)/n", 0.76f, 0.635f, 14f, 2f),
        FormulaItem("m = m\u2080 / \u221A(1-v\u00B2/c\u00B2)", 0.63f, 0.605f, 13f, -2f),
        FormulaItem("\u221A(7\u00B2+24\u00B2)", 0.80f, 0.70f, 15f, 3f),

        // --- Bottom band ---
        FormulaItem("E = mc\u00B2", 0.60f, 0.815f, 22f, -2f, bold = true),
        FormulaItem("\u03C3\u00B2 = 1/N \u03A3(x\u1d62-\u03BC)\u00B2", 0.05f, 0.855f, 13f, -2f),
        FormulaItem("2(2\u00B2)", 0.44f, 0.87f, 17f, 2f),
        FormulaItem("1/4 \u00D7 (8 2)", 0.62f, 0.865f, 15f, -2f),
        FormulaItem("3!", 0.84f, 0.865f, 18f, 3f),
        FormulaItem("\u2207p + \u03C1\u2207\u03A6 = 0", 0.14f, 0.925f, 13f, -1f),
        FormulaItem("\u0394V = nRT", 0.40f, 0.93f, 15f, 2f),
        FormulaItem("F = G m\u2081m\u2082/r\u00B2", 0.62f, 0.93f, 15f, -2f),
        FormulaItem("q v B", 0.06f, 0.965f, 15f, -3f),
        FormulaItem("\u2202\u00B2u/\u2202t\u00B2 = c\u00B2\u2207\u00B2u", 0.30f, 0.965f, 13f, 2f),
        FormulaItem("\u222B\u208B\u221E\u221E e\u207B\u02E3\u00B2 dx = \u221A\u03C0", 0.60f, 0.965f, 14f, -1f),

        // --- Faint motivational chalk notes, tucked in gaps ---
        FormulaItem("DISCIPLINE\nCREATES\nFREEDOM", 0.06f, 0.60f, 11f, -6f, alpha = 140),
        FormulaItem("A BETTER\nVERSION\nOF ME", 0.83f, 0.53f, 11f, -6f, alpha = 140),
        FormulaItem("SAME MINDSET\nDIFFERENT RESULTS", 0.08f, 0.79f, 10f, -6f, alpha = 140),
    )
}
