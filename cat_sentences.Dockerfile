ARG PROJECT_ID=latest
ARG BASE_VERSION=latest
FROM gcr.io/${PROJECT_ID}/bluebert-base:${BASE_VERSION}

COPY scripts/cat_sentences.entrypoint.sh /home/dev/entrypoint.sh

# parameters are:
# resources_bucket
# collection
# association_key_lc
# sentence_version
ENTRYPOINT /home/dev/entrypoint.sh "$@" 