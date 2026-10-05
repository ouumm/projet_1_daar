package daar;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Clone partiel de egrep : ( ) | concatenation * . lettre (+ et ? en bonus). */
public class Egrep {
    interface Matcher { boolean matches(byte[] buf, int off, int len); }

    public static void main(String[] args) throws Exception {
        boolean count = false, number = false, stats = false, tree = false;
        String algo = "dfa";
        List<String> pos = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-c": count = true; break;
                case "-n": number = true; break;
                case "--stats": stats = true; break;
                case "--tree": tree = true; break;
                case "--algo": algo = args[++i]; break;
                default: pos.add(args[i]);
            }
        }
        if (pos.size() != 2) {
            System.err.println("Usage: egrep-clone [-c] [-n] [--stats] [--tree] [--algo dfa|nfa|kmp|bm] REGEX FICHIER");
            System.exit(2);
        }
        Node root;
        try { root = Parser.parse(pos.get(0)); }
        catch (IllegalArgumentException e) { System.err.println("RegEx invalide: " + e.getMessage()); System.exit(2); return; }
        if (tree) System.err.println("Arbre: " + root);

        long t0 = System.nanoTime();
        Matcher m;
        if (algo.equals("kmp")) {
            if (!root.isLiteral()) { System.err.println("KMP: le motif n'est pas une simple concatenation"); System.exit(2); }
            StringBuilder sb = new StringBuilder();
            root.literal(sb);
            m = new Kmp(sb.toString())::matches;
        } else if (algo.equals("bm")) {
            if (!root.isLiteral()) { System.err.println("Boyer-Moore: le motif n'est pas une simple concatenation"); System.exit(2); }
            StringBuilder sb = new StringBuilder();
            root.literal(sb);
            m = new BoyerMoore(sb.toString())::matches;
        } else if (algo.equals("nfa")) {
            m = nfaMatcher(Nfa.unanchored(root));
        } else {
            Nfa nfa = Nfa.unanchored(root);
            Dfa d = Dfa.determinize(nfa);
            int before = d.size();
            d = d.minimize();
            if (stats) System.err.printf("NFA=%d etats, DFA=%d etats, DFA minimal=%d etats%n", nfa.size(), before, d.size());
            m = d::matches;
        }
        long tPre = System.nanoTime();

        OutputStream out = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 1 << 16);
        byte[] data;
        try { data = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pos.get(1))); }
        catch (IOException e) { System.err.println("Fichier introuvable: " + pos.get(1)); System.exit(2); return; }
        long t1 = System.nanoTime(); // la lecture du fichier n'est pas comptee dans le temps de recherche
        int nb = 0, lineNo = 0, n = data.length, start = 0;
        while (start < n) {
            int end = start;
            while (end < n && data[end] != '\n') end++;
            lineNo++;
            if (m.matches(data, start, end - start)) {
                nb++;
                if (!count) {
                    if (number) out.write((lineNo + ":").getBytes(StandardCharsets.ISO_8859_1));
                    out.write(data, start, end - start);
                    out.write('\n');
                }
            }
            start = end + 1;
        }
        if (count) out.write((nb + "\n").getBytes(StandardCharsets.ISO_8859_1));
        out.flush();
        long t2 = System.nanoTime();
        if (stats) System.err.printf("precalcul=%.2f ms, recherche=%.2f ms, %d lignes%n", (tPre - t0) / 1e6, (t2 - t1) / 1e6, nb);
        System.exit(nb > 0 ? 0 : 1);
    }

    /** Simulation directe de l'automate non deterministe (reference, sans determinisation). */
    static Matcher nfaMatcher(Nfa nfa) {
        return (line, off, len) -> {
            BitSet cur = new BitSet();
            cur.set(nfa.start);
            cur = eps(nfa, cur);
            if (cur.get(nfa.accept)) return true;
            for (int i = 0; i < len; i++) {
                int c = line[off + i] & 0xFF;
                BitSet nx = new BitSet();
                for (int q = cur.nextSetBit(0); q >= 0; q = cur.nextSetBit(q + 1))
                    for (int[] e : nfa.edges.get(q))
                        if (e[0] == c || e[0] == Nfa.ANY) nx.set(e[1]);
                cur = eps(nfa, nx);
                if (cur.get(nfa.accept)) return true;
            }
            return false;
        };
    }

    static BitSet eps(Nfa nfa, BitSet s) {
        BitSet r = (BitSet) s.clone();
        Deque<Integer> st = new ArrayDeque<>();
        for (int q = s.nextSetBit(0); q >= 0; q = s.nextSetBit(q + 1)) st.push(q);
        while (!st.isEmpty()) {
            int q = st.pop();
            for (int[] e : nfa.edges.get(q))
                if (e[0] == Nfa.EPS && !r.get(e[1])) { r.set(e[1]); st.push(e[1]); }
        }
        return r;
    }
}
