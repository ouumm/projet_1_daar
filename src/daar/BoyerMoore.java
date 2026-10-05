package daar;

import java.nio.charset.StandardCharsets;

/**
 * Boyer-Moore : recherche d'un facteur, comparaison de droite a gauche.
 * Deux heuristiques : mauvais caractere (bad character) et bon suffixe (good suffix).
 */
public class BoyerMoore {
    private final byte[] p;
    private final int m;
    private final int[] last = new int[256]; // derniere position de chaque octet dans p
    private final int[] shift;               // decalage bon suffixe
    private final int[] border;

    public BoyerMoore(String pattern) {
        p = pattern.getBytes(StandardCharsets.ISO_8859_1);
        m = p.length;
        java.util.Arrays.fill(last, -1);
        for (int i = 0; i < m; i++) last[p[i] & 0xFF] = i;
        shift = new int[m + 1];
        border = new int[m + 1];
        goodSuffix();
    }

    /** Precalcul du tableau bon suffixe (cas 1 et cas 2 du cours classique). */
    private void goodSuffix() {
        int i = m, j = m + 1;
        border[i] = j;
        while (i > 0) {
            while (j <= m && p[i - 1] != p[j - 1]) {
                if (shift[j] == 0) shift[j] = j - i;
                j = border[j];
            }
            i--; j--;
            border[i] = j;
        }
        j = border[0];
        for (i = 0; i <= m; i++) {
            if (shift[i] == 0) shift[i] = j;
            if (i == j) j = border[j];
        }
    }

    public boolean matches(byte[] t, int off, int len) {
        if (m == 0) return true;
        int s = 0;
        while (s <= len - m) {
            int j = m - 1;
            while (j >= 0 && p[j] == t[off + s + j]) j--;
            if (j < 0) return true;
            int bad = j - last[t[off + s + j] & 0xFF];
            s += Math.max(shift[j + 1], bad);
        }
        return false;
    }
}
