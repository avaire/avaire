#!/bin/bash

welcome() {
    echo ""
    echo "Welcome to AvaIre's Linux launcher"
    echo "Please select an option to begin:"
    echo ""
    echo "   1. Start the bot normally"
    echo "   2. Start the bot with automatic restarts"
    echo "   3. Update the bot using the nightly build"
    echo "   9. Exit program"
    echo ""

    echo "Enter your option: "

    read option

    if [ 1 -eq $option ]; then
        echo "Nornal start has been selected, starting the bot..."
        echo ""

        startApplication

    elif [ 2 -eq $option ]; then
        echo "Starting the bot with automatic restarts..."
        echo ""

        startLoop
    elif [ 3 -eq $option ]; then
        echo "Updating using the nightly build..."
        echo ""

        updateWithNightlyBuild
    fi
}

startLoop() {
    while true; do
        startApplication false

        echo ""
        echo "Restarting the application in 5 seconds!"
        echo "If you want to cancel the process, you can use CTRL + C now"
        sleep 5
    done
}

startApplication() {
    java -jar AvaIre.jar

    if [ "$1" != "false" ]; then
        welcome
    fi
}

updateWithNightlyBuild() {
    echo "Downloading the latest version..."
    curl -sS https://avairebot.com/nightly-build.jar --output nightly-build.jar

    echo "Replacing jar files with the latest version..."
    mv nightly-build.jar AvaIre.jar

    echo "Done!"

    welcome
}

welcome

