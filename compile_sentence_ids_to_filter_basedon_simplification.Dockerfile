ARG PROJECT_ID=latest
ARG BASE_VERSION=latest
FROM gcr.io/${PROJECT_ID}/bluebert-base:${BASE_VERSION}

COPY scripts/compile_filtered_sentence_ids.entrypoint.sh /home/dev/entrypoint.sh

# the goal of this container is to compile a list of sentence identifiers that should 
# be excluded based on the sentence simplification (distance between entities based 
# on the dependency parse is too far)

# parameters are:
# resources_bucket
# collection
# association_key_lc
# sentence_version
ENTRYPOINT /home/dev/entrypoint.sh "$@" 