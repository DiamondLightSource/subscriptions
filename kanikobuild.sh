#!/usr/bin/env bash
# Script to be called by .gitlab-ci.yml to perform container build using gitlab kubernetes executor

echo 'Building image...'


# By default the image is published with a :latest tag
DESTINATION="gcr.io/diamond-pubreg/daq-core/api-portal"
CMD="/kaniko/executor --context $CI_PROJECT_DIR --dockerfile $CI_PROJECT_DIR/Dockerfile"

# If this is a test build, ensure we don't push
if [ ! -z "${CI_CONTAINER_TEST_BUILD}" ]
then
  CMD=$CMD" --no-push"
else
  CMD=$CMD" --destination $DESTINATION:latest"

  # If the commit is tagged, the image is also published with a :<tag> tag
  if [ ! -z "${CI_COMMIT_TAG}" ]
  then
    CMD=$CMD" --destination $DESTINATION:$CI_COMMIT_TAG"
  fi

  # If running on a branch pipeline a :<branch> tag
  if [ ! -z "${CI_COMMIT_BRANCH}" ]
  then
    CMD=$CMD" --destination $DESTINATION:$CI_COMMIT_BRANCH"
  fi
fi

# Actual build happens here
echo "Command to execute is..."
echo $CMD
$CMD
