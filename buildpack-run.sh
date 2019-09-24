if [ -d ".git" ]; then
  echo Second part, move needed files for AvaIre to parent directory. Clean all other files to keep everything organized.
  echo When website is cloned and moved into the same directory. Needed files are then placed back in current directory.
  mv AvaIre.jar ../AvaIre.jar
  mv avairebotapache2.conf ../avairebotapache2.conf
  mv Procfile ../Procfile
  mv update.sh ../update.sh
  rm -r * & rm -r .git # Entire /app/ directory is cleared out, to prepare to retrieve the webfiles
  git clone https://github.com/avaire/website # Example: avaire/website
  mv website/{.[!.],}* .
  rm -r website/
  # Moving the config files needed to run everything back into the original directory
  mv ../AvaIre.jar .
  java -jar AvaIre.jar
  java -jar AvaIre.jar --generate-json-file
  mv ../avairebotapache2.conf .
  mv commandMap.json storage/commandMap.json
  mv ../update.sh .
  mv ../Procfile . # The Procfile will now override the Procfile from the website repo
else # If AvaIre.jar isn't compiled
  echo First part, set the .git folder.
  git clone https://github.com/avaire/avaire # Example: avaire/avaire
  mv avaire/.git .
fi # The third time the build-run.sh is ran, it won't be this script but the one over at the website repo!
