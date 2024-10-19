ARG PROJECT_ID=latest
ARG BASE_VERSION=latest
FROM gcr.io/${PROJECT_ID}/bluebert-base:${BASE_VERSION}

COPY scripts/cat_bert_output.entrypoint.sh /home/dev/entrypoint.sh

# this dockerfile is designed to cat the output of bert output that has been filtered using the sentence simplification service

# parameters are:
    # resources_bucket=$1
    # collection=$2
    # association_key_lc=$3
    # sentence_version=$4
    # model_version=$5
ENTRYPOINT /home/dev/entrypoint.sh "$@" 