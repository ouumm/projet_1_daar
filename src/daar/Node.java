package daar;

/** Noeud de l'arbre syntaxique d'une RegEx (Aho-Ullman, fig. 10.29). */
public class Node {
    public enum Type { EPS, CHAR, DOT, CONCAT, ALT, STAR, PLUS, OPT }
    public final Type type;
    public final char c;
    public final Node left, right;

    public Node(Type t, char c, Node l, Node r) { type = t; this.c = c; left = l; right = r; }
    public static Node eps() { return new Node(Type.EPS, '\0', null, null); }
    public static Node chr(char c) { return new Node(Type.CHAR, c, null, null); }
    public static Node dot() { return new Node(Type.DOT, '\0', null, null); }
    public static Node bin(Type t, Node l, Node r) { return new Node(t, '\0', l, r); }
    public static Node un(Type t, Node l) { return new Node(t, '\0', l, null); }

    /** Vrai si l'arbre est une pure concatenation de lettres (cas KMP). */
    public boolean isLiteral() {
        if (type == Type.CHAR) return true;
        if (type == Type.CONCAT) return left.isLiteral() && right.isLiteral();
        return false;
    }

    public void literal(StringBuilder sb) {
        if (type == Type.CHAR) sb.append(c);
        else { left.literal(sb); right.literal(sb); }
    }

    @Override public String toString() {
        switch (type) {
            case EPS: return "eps";
            case CHAR: return String.valueOf(c);
            case DOT: return ".";
            case CONCAT: return "(" + left + " . " + right + ")";
            case ALT: return "(" + left + " | " + right + ")";
            case STAR: return left + "*";
            case PLUS: return left + "+";
            default: return left + "?";
        }
    }
}
