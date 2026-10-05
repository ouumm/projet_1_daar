package daar;

import java.util.*;

/** Etape (2) : arbre -> automate non deterministe avec eps-transitions (Aho-Ullman, fig. 10.27-10.28). */
public class Nfa {
    public static final int EPS = -1, ANY = 256;
    public final ArrayList<ArrayList<int[]>> edges = new ArrayList<>(); // {symbole, cible}
    public int start, accept;

    private int newState() { edges.add(new ArrayList<>()); return edges.size() - 1; }
    private void add(int from, int sym, int to) { edges.get(from).add(new int[]{sym, to}); }

    public static Nfa fromTree(Node root) {
        Nfa a = new Nfa();
        int[] f = a.build(root);
        a.start = f[0];
        a.accept = f[1];
        return a;
    }

    /** Variante non ancree : Sigma* . R, pour trouver R n'importe ou dans la ligne. */
    public static Nfa unanchored(Node root) {
        Nfa a = fromTree(root);
        int s = a.newState();
        a.add(s, ANY, s);
        a.add(s, EPS, a.start);
        a.start = s;
        return a;
    }

    private int[] build(Node n) {
        int s = newState(), e = newState();
        switch (n.type) {
            case EPS: add(s, EPS, e); break;
            case CHAR: add(s, n.c & 0xFF, e); break;
            case DOT: add(s, ANY, e); break;
            case CONCAT: {
                int[] a = build(n.left), b = build(n.right);
                add(a[1], EPS, b[0]);
                return new int[]{a[0], b[1]};
            }
            case ALT: {
                int[] a = build(n.left), b = build(n.right);
                add(s, EPS, a[0]); add(s, EPS, b[0]);
                add(a[1], EPS, e); add(b[1], EPS, e);
                break;
            }
            case STAR: {
                int[] a = build(n.left);
                add(s, EPS, a[0]); add(s, EPS, e);
                add(a[1], EPS, a[0]); add(a[1], EPS, e);
                break;
            }
            case PLUS: {
                int[] a = build(n.left);
                add(s, EPS, a[0]);
                add(a[1], EPS, a[0]); add(a[1], EPS, e);
                break;
            }
            case OPT: {
                int[] a = build(n.left);
                add(s, EPS, a[0]); add(s, EPS, e);
                add(a[1], EPS, e);
                break;
            }
        }
        return new int[]{s, e};
    }

    public int size() { return edges.size(); }
}
