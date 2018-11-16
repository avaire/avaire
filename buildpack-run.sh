# This is buildpack-run.sh
rm -rf .git && rm -rf build/ && git clone https://github.com/avaire/avaire.git && cp -r avaire/.git . && cp src/main/resources/config.yml . && rm -rf avaire/ && echo script worked.
if [ -d "src/main/resources/plugins" ]; then
    echo Found plugins folder, copying...
    cp -r src/main/resources/plugins .
fi