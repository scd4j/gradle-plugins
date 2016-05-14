# loop in folders and execute ./gradlew wrapper
for f in *;do [[ -d "$f" && ! -L "$f" && ! $f == ".*" && ! $f == "gradle" ]] || continue; echo '-----------------'; cd $f; pwd; ./gradlew wrapper; cd ..; pwd; done
