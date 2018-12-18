#!/usr/bin/env bash

GITHUB_USER=$(cut -d/ -f1 <<< "$GITHUB_REPO")
repo_temp=$(mktemp -d)
push_uri="https://$GITHUB_USER:$GITHUB_SECRET_TOKEN@github.com/$GITHUB_REPO"

git config --global user.email "updater@updater" && git config --global user.name "AutoUpdater"

git clone "https://github.com/$GITHUB_REPO" "$repo_temp"
cd "$repo_temp"

git checkout -b upstream-master master
git pull --no-edit $UPSTREAM_REPO master

CONFLICTS=$(git ls-files -u | wc -l)
if [ "$CONFLICTS" -gt 0 ] ; then
    echo "There is a merge conflict. Aborting"
    git merge --abort
    curl -u $GITHUB_USER:$GITHUB_SECRET_TOKEN -H "Content-Type: application/json" -X POST -d '{"title": "Merge conflict detected", "body": "Heroku could not update your repo. Please check for merge conflicts and update manually!","labels": ["merge conflict"]}' https://api.github.com/repos/$GITHUB_REPO/issues
    exit 1
fi

git checkout master
git merge --no-edit --no-ff upstream-master

# Redirect to /dev/null to avoid secret leakage
git push "$push_uri" master >/dev/null 2>&1
