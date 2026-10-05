"""Genere rapport.pdf a partir de bench.csv et bench.png (python3 tests/make_report.py)."""
import csv
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image, Table, TableStyle, PageBreak, Preformatted

ss = getSampleStyleSheet()
B = ParagraphStyle("B", parent=ss["BodyText"], fontName="Helvetica", fontSize=10.5, leading=14.5, alignment=4, spaceAfter=6)
H1 = ParagraphStyle("H1", parent=ss["Heading1"], fontSize=15, spaceBefore=12, spaceAfter=6, textColor=colors.black)
H2 = ParagraphStyle("H2", parent=ss["Heading2"], fontSize=12, spaceBefore=8, spaceAfter=4, textColor=colors.black)
T = ParagraphStyle("T", parent=ss["Title"], fontSize=22, textColor=colors.black, spaceAfter=6)
C = ParagraphStyle("C", parent=B, alignment=1, fontSize=9, textColor=colors.HexColor("#333333"))
CODE = ParagraphStyle("CODE", fontName="Courier", fontSize=8.5, leading=11)

rows = list(csv.DictReader(open("bench.csv")))
def val(rg, al):
    for r in rows:
        if r["regex"] == rg and r["algo"] == al:
            return float(r["mean_ms"]), float(r["std_ms"])
    return None

S = []
p = lambda t: S.append(Paragraph(t, B))
h1 = lambda t: S.append(Paragraph(t, H1))
h2 = lambda t: S.append(Paragraph(t, H2))

S += [Spacer(1, 3*cm), Paragraph("Projet 1 : un clone de egrep", T),
      Paragraph("DAAR 2026-2027, Sorbonne Universite, M2 Informatique (RES-ALT-SOFT)", C), Spacer(1, .5*cm),
      Paragraph("Recherche de motif par expressions regulieres : automates finis, KMP et Boyer-Moore", C), Spacer(1, 1*cm),
      Paragraph("Auteur(s) : NOM1, NOM2", C), Paragraph("Rendu du 31 octobre 2026", C), PageBreak()]

h1("1. Introduction")
p("Ce projet realise un clone de la commande <b>egrep</b> : etant donne une expression reguliere et un fichier texte, "
  "le programme affiche les lignes qui contiennent une occurrence du motif. Nous traitons le sous-ensemble de la norme ERE impose "
  "par le sujet (parentheses, alternative, concatenation, etoile, point, lettre ASCII) et nous ajoutons <b>+</b>, <b>?</b> et l'echappement "
  "par <b>\\</b>. Le programme est ecrit en Java. Trois familles d'algorithmes sont implementees : la chaine complete "
  "de la seance 1 (arbre de syntaxe, automate non deterministe, automate deterministe, automate minimal), les algorithmes de recherche de facteur "
  "<b>Knuth-Morris-Pratt</b> et <b>Boyer-Moore</b> lorsque le motif est une simple concatenation de lettres, et une simulation directe de l'automate non deterministe, "
  "utilisee comme point de comparaison. Le resultat est valide contre egrep sur plusieurs textes et motifs, puis les performances sont mesurees.")

h1("2. Definition du probleme et structures de donnees")
p("<b>Probleme.</b> Soit un motif R (une RegEx) et un fichier de l lignes. On veut les lignes u telles qu'un facteur de u appartient au langage L(R). "
  "Comme egrep, on teste chaque ligne independamment, et une ligne est retenue des qu'une occurrence existe, quelle que soit sa position.")
p("<b>Structures de donnees.</b> L'arbre de syntaxe est un arbre binaire de noeuds (type, lettre, fils gauche, fils droit). "
  "L'automate non deterministe stocke pour chaque etat une liste d'arcs (symbole, cible), avec un symbole special pour l'epsilon et un autre pour le point. "
  "L'automate deterministe est une table de transitions <i>next[etat][octet]</i> sur l'alphabet de 256 octets, avec un tableau de booleens pour les etats acceptants. "
  "Les fichiers sont lus en octets (ISO-8859-1) et parcourus sans conversion en chaines, ce qui evite de dependre de l'encodage et correspond au comportement de egrep en locale C. "
  "Les fins de ligne sont uniquement les octets \\n, un \\r eventuel reste dans la ligne comme dans egrep.")

h1("3. Algorithmes")
h2("3.1 Chaine automate (seance 1)")
p("<b>(1) Parsing.</b> Une descente recursive suit la grammaire : alternative (priorite la plus faible), concatenation, repetition (*, +, ?), atome (lettre, point, expression entre parentheses). "
  "L'arbre de <i>S(a|g|r)+on</i> est la concatenation de S, du plus de l'alternative a|g|r, de o et de n.")
p("<b>(2) Aho-Ullman.</b> Chaque noeud de l'arbre est traduit par un fragment d'automate a un etat initial et un etat final, "
  "relies par des epsilon-transitions : lettre (1 arc), concatenation (epsilon entre les deux fragments), alternative (deux epsilon en entree et en sortie), "
  "etoile (retour arriere et saut). Le nombre d'etats est lineaire en la taille du motif (au plus deux par noeud de l'arbre).")
p("<b>Recherche d'un facteur.</b> Plutot que de tester tous les suffixes d'une ligne, on construit l'automate de &Sigma;*.R : un nouvel etat initial boucle sur tous les symboles "
  "et possede un epsilon vers l'initial de R. La ligne est acceptee si cet automate atteint un etat acceptant a un instant quelconque. Un seul parcours lineaire suffit.")
p("<b>(3) Sous-ensembles.</b> Chaque etat du deterministe est un ensemble d'etats du NFA, clos par epsilon. On calcule pour chacun des 256 octets l'ensemble atteint. "
  "Nous rendons les etats acceptants <i>absorbants</i> : des qu'une occurrence est trouvee, la ligne est retenue, la suite est sans importance. "
  "Cette simplification reduit le nombre d'etats et fait que tous les acceptants se fusionnent a l'etape suivante. Dans le pire cas le deterministe a 2<sup>n</sup> etats, "
  "mais les motifs usuels restent petits (tableau 1).")
p("<b>(4) Minimisation.</b> Nous utilisons l'algorithme de Moore (raffinement de partition) : partition initiale acceptants / non acceptants, puis separation des etats dont les "
  "successeurs sont dans des classes differentes, jusqu'a stabilite. Sa complexite est O(n<sup>2</sup>|&Sigma;|) dans le pire cas. L'algorithme de Hopcroft, en O(n|&Sigma;| log n), serait preferable pour de tres gros automates, "
  "mais ici n reste petit et Moore est plus simple a justifier.")
p("<b>(5) Recherche.</b> Pour chaque ligne, on suit les transitions octet par octet depuis l'etat initial et on s'arrete des qu'un etat acceptant est atteint : O(|ligne|) par ligne, donc O(taille du fichier) au total, independamment de la complexite du motif.")

h2("3.2 Knuth-Morris-Pratt")
p("Lorsque le motif est un mot m de longueur k, KMP evite de revenir en arriere dans le texte. Le precalcul construit la liste <i>Retenue</i> : "
  "retenue[i] est la longueur du plus long bord propre du prefixe de longueur i (le plus long mot a la fois prefixe et suffixe strict). En cas d'echec apres k' lettres correspondantes, "
  "on repart a la position retenue[k'] sans reculer dans le texte. Le precalcul coute O(k) et la recherche O(n) comparaisons au plus, dans tous les cas, ce qui garantit le pire cas lineaire.")

h2("3.3 Boyer-Moore")
p("Boyer-Moore compare le motif a la fenetre courante de <b>droite a gauche</b>. A l'echec, il decale le motif du maximum de deux heuristiques. "
  "<i>Mauvais caractere</i> : on aligne la derniere occurrence dans le motif du caractere fautif du texte (tableau de 256 entiers). "
  "<i>Bon suffixe</i> : on aligne le suffixe deja reconnu avec une autre occurrence de lui-meme dans le motif, ou avec un de ses bords (tableau calcule en O(k)). "
  "Le gain vient de ce que, sur un texte naturel, la plupart des fenetres echouent des la premiere comparaison avec un grand decalage (jusqu'a k) : le nombre moyen de comparaisons par octet est nettement inferieur a 1, "
  "et il diminue quand le motif s'allonge. Dans le pire cas la version simple coute O(nk) comparaisons ; la regle de Galil permet de le ramener a O(n).")

h2("3.4 Remarques et critique")
p("<b>KMP et Boyer-Moore ne couvrent pas le cas general</b> : ils exigent un motif sans operateur. Le programme detecte ce cas dans l'arbre de syntaxe. "
  "<b>La simulation du NFA</b> est en O(|ligne| &times; nombre d'etats), contre O(|ligne|) pour le deterministe : la determinisation deplace le cout dans un precalcul qui ne depend pas du texte. "
  "<b>Limite de notre implementation</b> : les tables de 256 colonnes sont gourmandes en memoire si le deterministe grossit, une compression de l'alphabet (classes d'octets equivalents) serait une amelioration directe. "
  "Enfin egrep fait mieux que tous nos algorithmes car il combine memchr, Boyer-Moore-Horspool sur les facteurs obligatoires du motif et automates paresseux, le tout en C et en lisant par gros blocs.")

h1("4. Tests")
p("<b>Validite.</b> Le script <i>tests/run_tests.sh</i> compare octet pour octet la sortie du clone et celle de <b>egrep</b> (locale C) sur 14 expressions "
  "(alternatives, etoiles imbriquees, point, motif vide, mots simples) et sur plusieurs textes, pour les algorithmes dfa, nfa, kmp et bm. Tous les tests passent. Les motifs litteraux incluent des cas a forts bords "
  "(<i>aaab</i>, <i>ababab</i>, <i>aabaab</i>) pour exercer les tables de KMP et du bon suffixe, sur un texte binaire a deux lettres.")
p("<b>Testbed de performance.</b> Les mesures du tableau et de la figure ci-dessous ont ete faites sur un texte synthetique de 11 Mo (environ 300 000 lignes, mots tires au hasard dans un vocabulaire d'une trentaine de mots), faute d'avoir pu utiliser les livres Gutenberg dans l'environnement de developpement. "
  "Les motifs suivent la suggestion du TME2 : Chihuahua, Pizzi, Pepperoni, Pizza, Poppers, plus trois motifs avec operateurs. "
  "Chaque mesure est repetee 5 fois (moyenne et ecart type). Pour le clone on mesure le temps de recherche interne, hors demarrage de la JVM, hors lecture du fichier et hors precalcul ; "
  "pour egrep on ne peut que mesurer le temps de la commande complete avec l'option -c. La comparaison avec egrep est donc a l'avantage du clone et ne doit se lire que comme un ordre de grandeur.")

h1("5. Resultats")
S.append(Image("bench.png", width=16.5*cm, height=6.1*cm))
S.append(Paragraph("Figure 1 : temps de recherche moyen (ms, echelle logarithmique, barres d'erreur = ecart type), 5 executions.", C))
S.append(Spacer(1, .2*cm))
data = [["Motif", "NFA", "DFA", "DFA min."], ["S(a|g|r)+on", "25", "8", "6"], ["Chihuahua", "35", "11", "11"], ["(a|b)*c", "13", "5", "3"], ["S.*n", "13", "6", "4"]]
t = Table(data, colWidths=[4*cm, 3*cm, 3*cm, 3*cm])
t.setStyle(TableStyle([("GRID", (0, 0), (-1, -1), .5, colors.black), ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"), ("FONTSIZE", (0, 0), (-1, -1), 9), ("ALIGN", (1, 0), (-1, -1), "CENTER")]))
S += [t, Paragraph("Tableau 1 : nombre d'etats des automates (motif non ancre &Sigma;*.R).", C)]

def f(rg, al):
    v = val(rg, al)
    return f"{v[0]:.0f}" if v else "?"
h1("6. Discussion")
p(f"<b>Automate contre simulation.</b> Le deterministe est entre 6 et 20 fois plus rapide que la simulation du NFA (par exemple {f('Pizza','dfa')} ms contre {f('Pizza','nfa')} ms pour <i>Pizza</i>, {f('(a|b)*c','dfa')} ms contre {f('(a|b)*c','nfa')} ms pour <i>(a|b)*c</i>). "
  "Plus le NFA a d'etats actifs simultanes (cas de <i>(a|b)*c</i>), plus l'ecart augmente. Le temps du DFA ne depend presque pas du motif : c'est la propriete attendue, un acces par octet.")
p(f"<b>KMP et Boyer-Moore.</b> Sur les motifs litteraux, KMP est proche du DFA (le DFA d'un mot est precisement l'automate de KMP complet), alors que Boyer-Moore est un peu plus rapide sur les motifs longs "
  f"(<i>Pepperoni</i> : {f('Pepperoni','bm')} ms contre {f('Pepperoni','kmp')} ms pour KMP), et equivalent sur les motifs courts comme <i>Pizzi</i>, ou l'ecart est dans l'ecart type. "
  "Cela va dans le sens de la theorie : Boyer-Moore decale d'autant plus que le motif est long, alors que KMP lit chaque octet. "
  "L'ecart reste modeste car notre mesure inclut le decoupage en lignes, commun aux algorithmes, qui represente une part importante du temps ; l'effet serait plus net sur un texte plus gros ou avec un decoupage en lignes evite.")
p("<b>egrep.</b> egrep reste tres en avance, d'un facteur 40 environ. Il faut y voir le cout d'une implementation Java (verification des bornes, pas de vectorisation) et surtout les optimisations de GNU grep : recherche de facteurs obligatoires, "
  "Boyer-Moore-Horspool vectorise, absence de decoupage ligne par ligne tant qu'aucune occurrence n'est trouvee. Notre decoupage ligne par ligne a un cout non negligeable que l'on n'a pas cherche a eliminer.")
p("<b>Limites.</b> Les resultats dependent du texte et de la machine. Les nombres precis sont a lire comme des tendances. Les textes de Gutenberg ont des distributions de lettres differentes de celles de notre texte, ce qui joue surtout sur Boyer-Moore.")

h1("7. Conclusion et perspectives")
p("Nous avons implemente une chaine complete de la RegEx vers l'automate minimal, validee contre egrep, et deux algorithmes de recherche de facteur. "
  "Le deterministe offre un temps lineaire independant du motif au prix d'un precalcul, Boyer-Moore est legerement plus rapide que KMP sur les mots longs. "
  "Perspectives : un index de mots (forward index, P-trie ou filtre de Bloom) calcule une fois et mis en cache pour accelerer les recherches repetees sur un meme texte ; "
  "un automate paresseux (determinisation a la demande) pour eviter l'explosion d'etats ; la minimisation de Hopcroft ; "
  "l'extraction d'un facteur obligatoire du motif pour utiliser Boyer-Moore comme pre-filtre avant l'automate, comme le fait egrep ; "
  "enfin le support des classes de caracteres et des ancres, hors du cadre du sujet.")

SimpleDocTemplate("rapport.pdf", pagesize=A4, leftMargin=2.2*cm, rightMargin=2.2*cm, topMargin=2*cm, bottomMargin=2*cm,
                  title="DAAR Projet 1 : clone de egrep").build(S)
print("rapport.pdf ok")
