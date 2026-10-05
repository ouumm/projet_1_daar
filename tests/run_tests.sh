#!/bin/sh
# Compare la sortie du clone a celle de egrep sur plusieurs motifs.
cd "$(dirname "$0")/.."
export LC_ALL=C
fail=0
for f in texts/*.txt; do
  for re in 'S(a|g|r)+on' 'S(a|g|r)*on' 'Sa.on' 'Chihuahua' 'Pizz(a|i)' 'P(e|o)p+' 'a|b' '(ab)*' '.' 'S.*n' 'Sag?on' '(a|b)*c' 'th(e|a)' 'x*' ; do
    for algo in dfa nfa; do
      egrep "$re" "$f" > /tmp/exp.txt
      ./egrep-clone --algo $algo "$re" "$f" > /tmp/got.txt
      if cmp -s /tmp/exp.txt /tmp/got.txt; then :; else echo "ECHEC [$algo] $re sur $f"; fail=1; fi
    done
  done
  for re in Chihuahua Pizza Pepperoni Poppers Pizzi aaab abab ababab onS S aabaab bbbb; do
    egrep "$re" "$f" > /tmp/exp.txt
    for algo in kmp bm; do
      ./egrep-clone --algo $algo "$re" "$f" > /tmp/got.txt
      cmp -s /tmp/exp.txt /tmp/got.txt || { echo "ECHEC [$algo] $re sur $f"; fail=1; }
    done
  done
done
[ $fail = 0 ] && echo "Tous les tests passent"
exit $fail
