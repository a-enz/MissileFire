base=$(pwd)
apron=/home/sae/apron
export CLASSPATH=.:$base/soot-2.5.0.jar:$apron/japron/apron.jar:$apron/japron/gmp.jar:$base/bin
export LD_LIBRARY_PATH=$apron/lib

java ch.ethz.sae.Verifier ExampleTest
