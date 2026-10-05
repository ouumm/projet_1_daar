package daar;

import java.util.*;

/** Etapes (3) determinisation (sous-ensembles) et (4) minimisation (Moore). Alphabet : 256 octets. */
public class Dfa {
    public static final int SIGMA = 256;
    public int[][] next;      // next[etat][octet]
    public boolean[] accept;
    public int start;

    public int size() { return next.length; }

    /** (3) Methode des sous-ensembles. Les etats acceptants sont rendus absorbants (on cherche une occurrence). */
    public static Dfa determinize(Nfa nfa) {
        Map<BitSet, Integer> ids = new HashMap<>();
        List<BitSet> sets = new ArrayList<>();
        List<int[]> rows = new ArrayList<>();
        List<Boolean> acc = new ArrayList<>();

        BitSet init = new BitSet();
        init.set(nfa.start);
        init = closure(nfa, init);
        ids.put(init, 0);
        sets.add(init);

        for (int i = 0; i < sets.size(); i++) {
            BitSet cur = sets.get(i);
            boolean isAcc = cur.get(nfa.accept);
            acc.add(isAcc);
            int[] row = new int[SIGMA];
            if (isAcc) {
                Arrays.fill(row, i); // absorbant
            } else {
                BitSet[] moves = new BitSet[SIGMA];
                for (int q = cur.nextSetBit(0); q >= 0; q = cur.nextSetBit(q + 1)) {
                    for (int[] ed : nfa.edges.get(q)) {
                        if (ed[0] == Nfa.EPS) continue;
                        if (ed[0] == Nfa.ANY) {
                            for (int c = 0; c < SIGMA; c++) {
                                if (moves[c] == null) moves[c] = new BitSet();
                                moves[c].set(ed[1]);
                            }
                        } else {
                            if (moves[ed[0]] == null) moves[ed[0]] = new BitSet();
                            moves[ed[0]].set(ed[1]);
                        }
                    }
                }
                for (int c = 0; c < SIGMA; c++) {
                    if (moves[c] == null) { row[c] = -1; continue; }
                    BitSet t = closure(nfa, moves[c]);
                    Integer id = ids.get(t);
                    if (id == null) { id = sets.size(); ids.put(t, id); sets.add(t); }
                    row[c] = id;
                }
            }
            rows.add(row);
        }
        Dfa d = new Dfa();
        int n = rows.size();
        d.next = new int[n + 1][];
        d.accept = new boolean[n + 1];
        for (int i = 0; i < n; i++) {
            d.next[i] = rows.get(i);
            d.accept[i] = acc.get(i);
            for (int c = 0; c < SIGMA; c++) if (d.next[i][c] < 0) d.next[i][c] = n; // etat puits
        }
        d.next[n] = new int[SIGMA];
        Arrays.fill(d.next[n], n);
        d.start = 0;
        return d;
    }

    private static BitSet closure(Nfa nfa, BitSet set) {
        BitSet res = (BitSet) set.clone();
        Deque<Integer> st = new ArrayDeque<>();
        for (int q = set.nextSetBit(0); q >= 0; q = set.nextSetBit(q + 1)) st.push(q);
        while (!st.isEmpty()) {
            int q = st.pop();
            for (int[] ed : nfa.edges.get(q))
                if (ed[0] == Nfa.EPS && !res.get(ed[1])) { res.set(ed[1]); st.push(ed[1]); }
        }
        return res;
    }

    /** (4) Minimisation par raffinement de partition (algorithme de Moore, version naive du livre). */
    public Dfa minimize() {
        int n = size();
        int[] cls = new int[n];
        for (int i = 0; i < n; i++) cls[i] = accept[i] ? 1 : 0;
        int count = -1;
        while (true) {
            Map<String, Integer> sig = new HashMap<>();
            int[] ncls = new int[n];
            for (int i = 0; i < n; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(cls[i]);
                for (int c = 0; c < SIGMA; c++) sb.append(',').append(cls[next[i][c]]);
                Integer id = sig.get(sb.toString());
                if (id == null) { id = sig.size(); sig.put(sb.toString(), id); }
                ncls[i] = id;
            }
            cls = ncls;
            if (sig.size() == count) break;
            count = sig.size();
        }
        Dfa m = new Dfa();
        m.next = new int[count][SIGMA];
        m.accept = new boolean[count];
        for (int i = 0; i < n; i++) {
            m.accept[cls[i]] = accept[i];
            for (int c = 0; c < SIGMA; c++) m.next[cls[i]][c] = cls[next[i][c]];
        }
        m.start = cls[start];
        return m;
    }

    /** (5) Vrai si la ligne contient une occurrence du motif (parcours lineaire). */
    public boolean matches(byte[] line, int off, int len) {
        int q = start;
        if (accept[q]) return true;
        for (int i = 0; i < len; i++) {
            q = next[q][line[off + i] & 0xFF];
            if (accept[q]) return true;
        }
        return false;
    }
}
