base=$(pwd)
apron=/home/sae/apron/
export CLASSPATH=.:$base/soot-2.5.0.jar:$apron/japron/apron.jar:$apron/japron/gmp.jar
export LD_LIBRARY_PATH=$apron/lib

mkdir -p bin
javac -d bin src/*.java
javac -d bin src/ch/ethz/sae/*.java




