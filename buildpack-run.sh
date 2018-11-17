# This is buildpack-run.sh
# Trick to clone AvaIre's git into a seperate folder, copying .git to the top directory. Followed by putting the config.yml on the right place.
rm -rf .git && git clone https://github.com/avaire/avaire.git && cp -r avaire/.git . && cp src/main/resources/config.yml . && rm -rf avaire/ && echo script worked.
# Cleaning any build artifacts, done before and after the gradle proces.
rm -rf build/ && echo cleaned out build artifacts.
