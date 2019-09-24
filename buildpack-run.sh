# If AvaIre.jar is compiled
if [ -d ".git" ]; then
  echo Secondly moving some files we want to keep after getting the website. We also generate the commandMap.json.
  echo After getting the website files in the right directory, we move the files back in the right directory with the website files.
  # Generating the commandMap.json
  
  # Moving the config files needed to run AvaIre.jar and the Apache2 webserver outside the app/ folder
  mv AvaIre.jar ../AvaIre.jar
  mv avairebotapache2.conf ../avairebotapache2.conf
  mv Procfile ../Procfile
  mv update.sh ../update.sh
  # Entire /app/ directory is cleared out, to prepare to retrieve the webfiles
  echo doing the big delete tric
  rm -r -v !("AvaIre.jar"|"avairebotapache2.conf"|"Procfile"|"update.sh") {.[!.],}*
  # The website is being cloned and moven out of it's folder
  git clone https://github.com/avaire/website # Example: avaire/website
  mv website/{.[!.],}* .
  rm -r website/
  # Moving the config files needed to run everything back into the original directory
  mv ../AvaIre.jar .
  java -jar AvaIre.jar
  java -jar AvaIre.jar --generate-json-file
  mv ../avairebotapache2.conf .
  mv ../commandMap.json storage/commandMap.json
  mv ../update.sh .
  mv ../Procfile . # The Procfile will now override the Procfile from the website repo
# If AvaIre.jar isn't compiled
else
  echo First part, set the .git folder.
  git clone https://github.com/avaire/avaire # Example: avaire/avaire
  mv avaire/.git .
fi
echo This is the end of the script
# The third time the build-run.sh is ran, it won't be this script but the one over at the website repo!
