if [ -d ".git" ]; then # After compile remove .git (as it will be replaced) and delete everything else with exception after $. Get website except wrong Procfile for this build
  rm -r .git/ & rm -rf $(find * -name "*" ! -name "AvaIre.jar" ! -name "avairebotapache2.conf" ! -name "Procfile" ! -name "update.sh")
  git clone https://github.com/avaire/website && rm website/Procfile && mv website/{.[!.],}* . && rm -r website/ # Example: avaire/website
  java -jar AvaIre.jar --generate-json-file && mv commandMap.json storage/commandMap.json # Get needed configs and move command file to right directory
else # If AvaIre.jar isn't compiled
  git clone https://github.com/avaire/avaire && mv avaire/.git . # Example: avaire/avaire
fi # The third time the build-run.sh is ran, it won't be this script but the one over at the website repo!
