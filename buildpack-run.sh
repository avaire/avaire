# This is buildpack-run.sh
git clone https://github.com/Macley-Kun/avaire.git
cp -r avaire/.git .
cp src/main/resources/config.yml .
rm -rf avaire/
