ARG PROJECT_ID=latest
ARG BASE_VERSION=latest
FROM gcr.io/${PROJECT_ID}/bluebert-base:${BASE_VERSION}

# Note: TUNED_MODEL_VERSION is the version of the model being trained. 
#       This version will be used in the exported model file name.
ARG TUNED_MODEL_VERSION=latest

# Note: TASK_NAME must align with names used by the prob2label.py script 
#       and also must align with the data directory structure, e.g. bl_chemical_to_gene
ARG TASK_NAME=latest

# Download the base BlueBERT model
WORKDIR /home/dev/models/baseline
RUN wget https://drive.google.com/u/0/open?id=1GJpGjQj6aZPV-EfbiQELpBkvlGtoKiyA && \
    tar -xzvf biobert_large_v1.1_pubmed.tar.gz

# copy the task-specific training/evaluation data into the container
COPY data/${TASK_NAME}/data.tsv /home/dev/data/
# copy the training entrypoint script into the container
COPY scripts/train.entrypoint.sh /home/dev/entrypoint.sh
# copy resources used to evaluate the model into the container
COPY scripts/prob2label.py /home/dev/

WORKDIR /home/dev
## install blue benchmark repository; note, it's possible some of the pip install commands below are not needed 
RUN git clone https://github.com/ncbi-nlp/BLUE_Benchmark.git ./blue_benchmark.git && \
    cd blue_benchmark.git && \
    pip install -r requirements.txt && \
    pip install fire && \
    pip install jsonlines && \
    pip install pandas && \
    pip install tabulate && \
    pip install sklearn

ENV BlueBERT_DIR '/home/dev/models/baseline'

WORKDIR /home/dev/data
# split data.tsv randomly into train/dev/test = 60%/20%/20%
RUN sort -R data.tsv > data.random.tsv && \
    split -l $[ $(wc -l data.random.tsv|cut -d" " -f1) * 60 / 100 ] data.random.tsv && \
    mv xaa train.tsv && \
    mv xab rest.tsv && \
    split -l $[ $(wc -l rest.tsv|cut -d" " -f1) * 50 / 100 ] rest.tsv && \
    mv xaa test.tsv && \
    # tie breaker may cause an xac file with one line so we cat remaining xa* files into dev.tsv
    cat xa* > dev.tsv && \ 
    rm rest.tsv xa*

# create test.ids file that is used by prob2label.py
RUN cut -f 1 test.tsv | sed -E 's/^(.*)+/\1\t\1\tT1\tT2\t/' > test.ids

# convert the test.tsv file to the format expected by the BLUE_Benchmark code
RUN cut -f 1,3 test.tsv > test.blue.gs && sed -E -i 's/^(.*)+\t/\1\t\1\tT1\tT2\t/' test.blue.gs

# add headers
RUN sed -i '1s/^/id	docid	arg1	arg2	label\n/' test.blue.gs && \
    sed -i '1s/^/id	docid	arg1	arg2	label\n/' test.ids && \
    sed -i '1s/^/id	sentence	label\n/' train.tsv && \
    sed -i '1s/^/id	sentence	label\n/' dev.tsv && \
    sed -i '1s/^/id	sentence	label\n/' test.tsv

ENV TUNED_MODEL_VERSION_ENV=$TUNED_MODEL_VERSION
ENV TASK_NAME_ENV=$TASK_NAME

ENTRYPOINT /home/dev/entrypoint.sh "$TASK_NAME_ENV" "$TUNED_MODEL_VERSION_ENV" "$@" 

# To build:
# docker build --build-arg "PROJECT_ID=[PROJECT_ID]" \
#              --build-arg "TASK_NAME=[TASK_NAME]" \
#              --build-arg "BASE_VERSION=[BASE_VERSION]" \
#              --build-arg "TUNED_MODEL_VERSION=0.1" \
#              -t task_name-train:0.1 -f train.Dockerfile
# 
# where:
#  [PROJECT_ID] = the id for this project - it is used to retrieve the already built base image (from base.Dockerfile)
#  [BASE_VERSION] = the version of the base container to use
#  [TASK_NAME] = the name of the task that the model is being trained for. TASK_NAME must align with names used by the prob2label.py script 
#                and also must align with the data directory structure, e.g. bl_chemical_to_gene
#  [TUNED_MODEL_VERSION] = the version of the model being trained. This version will be used in the exported model file name.


# To run:
# docker run --rm chemicaltodisease:0.1 [MODEL_STORAGE_BUCKET]
#
# where: 
#  [MODEL_STORAGE_BUCKET] = the bucket where the trained model will be stored as a tarball, e.g. gs://xyz