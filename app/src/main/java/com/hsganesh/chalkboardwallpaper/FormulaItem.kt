package com.hsganesh.chalkboardwallpaper

/**
 * The bank of formula/annotation text used to densely fill the board.
 * Plain strings only — placement (position, size, rotation) is decided
 * procedurally at render time so the board reads like a real blackboard
 * someone has genuinely filled while solving problems, not a fixed
 * template.
 *
 * Only glyphs confirmed present in the chalk handwriting font are used
 * directly (² ³ ¹ √ ± × ≈ ≠ ≤ ≥ ÷ ° μ π Δ ∫ ∂); every other Greek
 * letter or math operator is spelled out (theta, psi, curl, sum, ...)
 * so the whole board renders in one consistent handwriting style
 * instead of falling back to a mismatched system font mid-word.
 */
object FormulaBank {

    val pool: List<String> = listOf(
        "E = mc\u00B2", "F = ma", "a\u00B2+b\u00B2=c\u00B2", "curl B = \u03BC0 J",
        "\u222B0-3 x\u00B2 dx = 9", "d/dx sin x = cos x", "chi\u00B2 = sum(O-E)\u00B2/E",
        "x=(-b\u00B1\u221A(b\u00B2-4ac))/2a", "sin\u00B2A+cos\u00B2A=1", "\u0394V=nRT",
        "F=Gm1m2/r\u00B2", "s\u00B2 = 1/N sum(xi-m)\u00B2",
        "sum 1/n\u00B2 = \u03C0\u00B2/6", "m=m0/\u221A(1-v\u00B2/c\u00B2)", "PV=nRT",
        "psi(x,t)", "ih dpsi/dt = H psi",
        "div E = rho/e0", "3! = 6", "log2(8)=3",
        "lim x\u21920 sinx/x=1", "det[2 1;1 3]=5", "[1 0;0 1]", "|v|=\u221A(x\u00B2+y\u00B2)",
        "A\u00B7B=|A||B|cosT", "v = u + at", "s = ut + \u00BDat\u00B2",
        "P=IV", "V=IR", "KE=\u00BDmv\u00B2", "sum k=1..n = n(n+1)/2", "n!/(n-r)!",
        "C(n,r)=n!/r!(n-r)!", "\u222B e^x dx = e^x+C", "d/dx ln x = 1/x",
        "grad\u00B2 phi = 0", "y=mx+c", "\u0394x\u0394p \u2265 h/2",
        "T=2\u03C0\u221A(L/g)", "lambda = h/p",
        "sin(A+B)=sinAcosB+cosAsinB", "cos2A=1-2sin\u00B2A", "\u222B1/x dx=ln|x|",
        "z=a+bi", "|z|=\u221A(a\u00B2+b\u00B2)", "e^(i\u03C0)+1=0", "du/dt = a d\u00B2u/dx\u00B2",
        "f'(x)=lim h\u21920 [f(x+h)-f(x)]/h", "q=CV",
        "R = rho L/A", "w = 2\u03C0f", "E=hf", "p=mv", "F=-kx", "W=Fd",
        "sum F=ma", "g=9.8 m/s\u00B2", "c=3\u00D710\u2078 m/s", "Na=6.022\u00D710\u00B2\u00B3",
        "sinh x=(e^x-e^-x)/2", "curl E = -dB/dt", "H psi = E psi",
        "for all x in R", "exists y: f(y)=0", "A and B = empty set", "rank(A)=2",
        "trace(A) = sum aii", "d\u00B2f/dxdy", "theta + phi = \u03C0/2", "wt + phi",
        "curl E = -\u2202B/\u2202t", "\u2202\u00B2u/\u2202t\u00B2 = c\u00B2 \u2202\u00B2u/\u2202x\u00B2"
    )

    /** A small set of iconic equations occasionally drawn larger / bolder. */
    val heroPool: List<String> = listOf(
        "E = mc\u00B2", "F = ma", "a\u00B2+b\u00B2=c\u00B2", "e^(i\u03C0)+1=0",
        "PV = nRT", "curl B = \u03BC0 J", "\u0394x\u0394p \u2265 h/2"
    )
}
