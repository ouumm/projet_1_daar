package daar;

/**
 * Etape (1) : RegEx -> arbre syntaxique, descente recursive.
 * alt    := concat ('|' concat)*
 * concat := rep*
 * rep    := atom ('*' | '+' | '?')*
 * atom   := '(' alt ')' | '.' | '\' c | lettre
 */
public class Parser {
    private final String s;
    private int pos = 0;

    private Parser(String s) { this.s = s; }

    public static Node parse(String regex) {
        Parser p = new Parser(regex);
        Node n = p.alt();
        if (p.pos != regex.length())
            throw new IllegalArgumentException("Parenthese fermante inattendue en position " + p.pos);
        return n;
    }

    private boolean more() { return pos < s.length(); }

    private Node alt() {
        Node n = concat();
        while (more() && s.charAt(pos) == '|') {
            pos++;
            n = Node.bin(Node.Type.ALT, n, concat());
        }
        return n;
    }

    private Node concat() {
        Node n = null;
        while (more() && s.charAt(pos) != '|' && s.charAt(pos) != ')') {
            Node r = rep();
            n = (n == null) ? r : Node.bin(Node.Type.CONCAT, n, r);
        }
        return n == null ? Node.eps() : n;
    }

    private Node rep() {
        Node n = atom();
        while (more()) {
            char c = s.charAt(pos);
            if (c == '*') n = Node.un(Node.Type.STAR, n);
            else if (c == '+') n = Node.un(Node.Type.PLUS, n);
            else if (c == '?') n = Node.un(Node.Type.OPT, n);
            else break;
            pos++;
        }
        return n;
    }

    private Node atom() {
        char c = s.charAt(pos++);
        if (c == '(') {
            Node n = alt();
            if (!more() || s.charAt(pos) != ')')
                throw new IllegalArgumentException("Parenthese non fermee");
            pos++;
            return n;
        }
        if (c == '.') return Node.dot();
        if (c == '\\') {
            if (!more()) throw new IllegalArgumentException("'\\' en fin de motif");
            return Node.chr(s.charAt(pos++));
        }
        if (c == '*' || c == '+' || c == '?')
            throw new IllegalArgumentException("Operateur '" + c + "' sans operande en position " + (pos - 1));
        return Node.chr(c);
    }
}
