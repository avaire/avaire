# This is buildpack-run.sh
rm -rf .git && rm -rf build/ && git clone https://github.com/avaire/avaire.git && cp -r avaire/.git . && cp src/main/resources/config.yml . && rm -rf avaire/ && echo script worked.
