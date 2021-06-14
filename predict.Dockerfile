ARG PROJECT_ID=latest
ARG BASE_VERSION=latest
FROM gcr.io/${PROJECT_ID}/bluebert-base:${BASE_VERSION}

ARG TUNED_MODEL_VERSION=latest

# Note: TASK_NAME must align with names used by the prob2label.py script 
#       and also must align with the data directory structure, e.g. bl_chemical_to_gene 
ARG TASK_NAME=latest

# MODEL_STORAGE_BUCKET is the GCP bucket where the trained model will be downloaded from, e.g. gs://a/b/c/
ARG MODEL_STORAGE_BUCKET=latest

# CLASSIFICATION_LABELS is a tab-delimited string of the possible classification labels for the task, e.g. 'treats  false'
ARG CLASSIFICATION_LABELS=latest

COPY scripts/predict.entrypoint.sh /home/dev/entrypoint.sh

# WORKDIR /home/dev
## install bluebert repository
# RUN git clone https://github.com/UCDenver-ccp/bluebert.git ./ccp-bluebert.git && \
#     cd ccp-bluebert.git && \
#     pip install -r requirements.txt
    # pip install fire && \
    # pip install jsonlines && \
    # pip install pandas && \
    # pip install tabulate && \
    # pip install sklearn


# ENV DATASET_DIR '/home/dev/data'
# ENV OUTPUT_DIR '/home/dev/output'
# ENV PYTHONPATH '.'

ENV BlueBERT_DIR '/home/dev/models/tuned'

WORKDIR /home/dev/models/tuned
# download the model
ENV TUNED_MODEL_VERSION_ENV=$TUNED_MODEL_VERSION
ENV TASK_NAME_ENV=$TASK_NAME
ENV MODEL_STORAGE_BUCKET_ENV=$MODEL_STORAGE_BUCKET
ENV CLASSIFICATION_LABELS_ENV=$CLASSIFICATION_LABELS

RUN gcloud auth login && \
    gsutil cp "${MODEL_STORAGE_BUCKET_ENV}/bert/${TASK_NAME_ENV}/${TASK_NAME_ENV}.${TUNED_MODEL_VERSION_ENV}.tar.gz" . && \
    tar -xzf "${TASK_NAME_ENV}.${TUNED_MODEL_VERSION_ENV}.tar.gz" 
    # && \
    # cp /home/dev/models/baseline/vocab.txt . && \
    # cp /home/dev/models/baseline/bert_config.json .

ENTRYPOINT /home/dev/entrypoint.sh "${TASK_NAME_ENV}" "${CLASSIFICATION_LABELS_ENV}" "${TUNED_MODEL_VERSION_ENV}" "$@" 


# To build:
# docker build --build-arg "PROJECT_ID=[PROJECT_ID]" \
#              --build-arg "BASE_VERSION=[BASE_VERSION]" \
#              --build-arg "TASK_NAME=[TASK_NAME]" \
#              --build-arg "TUNED_MODEL_VERSION=[TUNED_MODEL_VERSION]" \
#              --build-arg "MODEL_STORAGE_BUCKET=[MODEL_STORAGE_BUCKET]" \
#              --build-arg "CLASSIFICATION_LABELS=[CLASSIFICATION_LABELS]" \
#              -t task-name:0.1 -f predict.Dockerfile
# 
# where:
#  [PROJECT_ID] = the id for this project - it is used to retrieve the already built base image (from base.Dockerfile)
#  [BASE_VERSION] = the version of the base container to use
#  [TASK_NAME] = the name of the task that the model is being trained for. TASK_NAME must align with names used by the prob2label.py script 
#                and also must align with the data directory structure, e.g. bl_chemical_to_gene
#  [TUNED_MODEL_VERSION] = the version of the model being trained. This version will be used in the exported model file name.
#  [MODEL_STORAGE_BUCKET] = the Google Cloud Storage bucket where the tuned-model is located
#  [CLASSIFICATION_LABELS] =  the classification labels that will be used as part of the file header for the output file, e.g. for the `bl_chemical_to_disease` task, the classification labels string should be "`treats false`".


# To run:
# docker run --rm [IMAGE_NAME]:[IMAGE_VERSION] [SENTENCE_BUCKET] [COLLECTION] [OUTPUT_BUCKET]
#
# where: 
#  [IMAGE_NAME] = the name of the Docker image - which will be the same as the TASK_NAME
#  [IMAGE_VERSION] = the version of the Docker image - which will be the same as the TUNED_MODEL_VERSION
#  [SENTENCE_BUCKET] = the full GCP path to where the TSV to-be-classified sentence files are located, e.g. gs://xyz/sentences/chemical-disease/
#  [COLLECTION] = the name of the collection being processed, e.g. PUBMED_SUB_31, 2021_06_08
#  [OUTPUT_BUCKET] = the output bucket where classified sentences will be placed, e.g. gs://xyz