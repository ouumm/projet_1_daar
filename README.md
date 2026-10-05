# DAAR Projet 1 : clone de egrep

Support : parentheses, alternative `|`, concatenation, etoile `*`, point `.`, lettres ASCII (bonus : `+`, `?`, echappement `\`).

## Compilation (Java 11+)
    make

## Utilisation
    ./egrep-clone [-c] [-n] [--stats] [--tree] [--algo dfa|nfa|kmp|bm] REGEX FICHIER
    ./egrep-clone 'S(a|g|r)+on' texts/sample.txt

- `-c` nombre de lignes, `-n` numeros de lignes, `--stats` tailles des automates et temps.
- `--algo dfa` (defaut) : arbre -> NFA (Aho-Ullman) -> DFA (sous-ensembles) -> DFA minimal (Moore) -> parcours lineaire.
- `--algo nfa` : simulation directe du NFA (reference de performance).
- `--algo kmp` : KMP, uniquement si le motif est une simple concatenation de lettres.
- `--algo bm` : Boyer-Moore (mauvais caractere + bon suffixe), meme restriction que KMP.

Code retour : 0 si au moins une ligne, 1 sinon, 2 en cas d'erreur (comme egrep).

## Tests et benchmarks
    make test                        # compare la sortie a egrep (dfa, nfa, kmp, bm)
    python3 tests/make_report.py     # regenere rapport.pdf depuis bench.csv et bench.png (reportlab, matplotlib)
    python3 tests/bench.py FICHIER 10  # egrep vs clone, produit bench.csv et bench.png
Mettre des livres Gutenberg (.txt) dans `texts/` pour les tests.

## Structure
`src/daar/` : Node, Parser (1), Nfa (2), Dfa (3, 4, 5), Kmp, BoyerMoore, Egrep (main).
