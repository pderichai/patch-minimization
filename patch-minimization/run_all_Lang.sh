#!/opt/local/bin/bash
javac -d bin -sourcepath src -cp bin:lib/diff-utils.jar src/edu/washington/bugisolation/main/Defects4JMain.java
for i in {1..65}
do
    printf "%s\n" "Lang" "$i" "true" | java -ea -cp bin:lib/diff-utils.jar edu.washington.bugisolation.main.Defects4JMain
    printf "%s\n" "Lang" "$i" "false" | java -ea -cp bin:lib/diff-utils.jar edu.washington.bugisolation.main.Defects4JMain
done
