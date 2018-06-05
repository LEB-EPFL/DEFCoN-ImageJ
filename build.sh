if [ -z "$TRAVIS_TAG" ]
then
    echo "No tag detected; will skip all deployments."
    mvn --settings settings.xml install -Dgpg.skip -Dmaven.javadoc.skip=true -B -V
else
    echo "Tag detected; will deploy to Maven Central and GitHub."
    mvn clean deploy --settings settings.xml -B -U -Prelease
fi
