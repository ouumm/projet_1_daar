package daar;

/** KMP : recherche d'un facteur. retenue[i] = longueur du plus long bord propre de p[0..i-1]. */
public class Kmp {
    private final byte[] p;
    private final int[] retenue;

    public Kmp(String pattern) {
        p = pattern.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        retenue = new int[p.length + 1];
        retenue[0] = -1;
        int k = -1;
        for (int i = 0; i < p.length; i++) {
            while (k >= 0 && p[k] != p[i]) k = retenue[k];
            k++;
            retenue[i + 1] = k;
        }
    }

    public int[] retenue() { return retenue; }

    public boolean matches(byte[] t, int off, int len) {
        if (p.length == 0) return true;
        int k = 0;
        for (int i = 0; i < len; i++) {
            while (k >= 0 && (k == p.length || p[k] != t[off + i])) k = retenue[k];
            k++;
            if (k == p.length) return true;
        }
        return false;
    }
}
