# Text Mining Provider relation extraction

The code in this repository containerizes the tuning and running of BERT models for the task of sentence classification. Models are based on the NCBI BlueBERT models and tuned to classify sentences as asserting specific Biolink associations.


## Targeted Biolink Associations
Items in the list below should be used in place of the `[TASK_NAME]` placeholder in the Docker commands detailed on this page. A status of `IN PROGRESS` indicates that a model is still being developed (and it may not yet perform well enough to use in production), while a status of `MATURE` indicates that the model has reached a performance level deemed acceptable for production use.

| *TASK_NAME* | *Status* |
| ----------- | -------- |
| bl_chemical_to_disease_or_phenotypic_feature | `IN PROGRESS` |
| bl_chemical_to_gene | `IN PROGRESS` |
| bl_disease_to_phenotypic_feature |`IN PROGRESS` |
| bl_gene_regulatory_relationship | `IN PROGRESS` |
| bl_gene_to_disease | `IN PROGRESS` |
| bl_gene_to_expression_site | `IN PROGRESS` 
| bl_gene_to_go_term | `IN PROGRESS` |


## Create the base Docker image that will be used for both model tuning and sentence classification

Build the base Docker image that is the foundation for both model tuning and sentence classification with the tuned model.

```
docker build -t bluebert-base:[BASE_VERSION] -f base.Dockerfile
```

where:
* `[BASE_VERSION]` = the version of the base image (Note: this will be referenced by the `docker build` command for both the tuning and classification image builds below).



## Tuning the BlueBERT models

Model tuning has been containerized using Docker. Note: aspects of the containers are specific to the Google Cloud environment as the tuned model is saved to a Google Cloud Storage bucket. Models are tuned with data available in the `data/` directory. For a given task, a model can be tuned by first building the Docker image:

```
 docker build --build-arg "PROJECT_ID=[PROJECT_ID]" \
              --build-arg "TASK_NAME=[TASK_NAME]" \
              --build-arg "BASE_VERSION=[BASE_VERSION]" \
              --build-arg "TUNED_MODEL_VERSION=[TUNED_MODEL_VERSION]" \
              -t [TASK_NAME]-train:TUNED_MODEL_VERSION -f train.Dockerfile
```

 where:
* `[PROJECT_ID]` = the id for this project - it is used to retrieve the already built base image (from base.Dockerfile)
* `[BASE_VERSION]` = the version of the base container to use
* `[TASK_NAME]` = the name of the task that the model is being trained for. TASK_NAME must align with names used by the [prob2label.py]() script and also must align with the data directory structure, e.g. bl_chemical_to_gene
* `[TUNED_MODEL_VERSION]` = the version of the model being trained. This version will be used in the exported model file name.

And then by running the image:

```
docker run --rm [TASK_NAME]-train:[TUNED_MODEL_VERSION] [MODEL_STORAGE_BUCKET]
```

where: 
* `[TASK_NAME]` = the name of the task (this name must match the image name used in the previous docker command)
* `[TUNED_MODEL_VERSION]` = the version of the model to use (this version must match the container version used in the previous docker command)
* `[MODEL_STORAGE_BUCKET]` = the bucket where the trained model will be stored as a tarball, e.g. `gs://xyz`


## Classifying sentences with the tuned models

To classify sentences using pre-tuned models from the steps above (which have been stored in a Google Cloud Storage bucket), we first create a container with the pre-tuned model:

```
docker build --build-arg "PROJECT_ID=[PROJECT_ID]" \
              --build-arg "BASE_VERSION=[BASE_VERSION]" \
              --build-arg "TASK_NAME=[TASK_NAME]" \
              --build-arg "TUNED_MODEL_VERSION=[TUNED_MODEL_VERSION]" \
              --build-arg "MODEL_STORAGE_BUCKET=[MODEL_STORAGE_BUCKET]" \
              --build-arg "CLASSIFICATION_LABELS=[CLASSIFICATION_LABELS]" \
              -t [TASK_NAME]-predict:TUNED_MODEL_VERSION -f predict.Dockerfile
```

 where:
* `[PROJECT_ID]` = the id for this project - it is used to retrieve the already built base image (from base.Dockerfile)
* `[BASE_VERSION]` = the version of the base container to use
* `[TASK_NAME]` = the name of the task that the model is being trained for. TASK_NAME must align with names used by the prob2label.py script and also must align with the data directory structure, e.g. bl_chemical_to_gene
* `[TUNED_MODEL_VERSION]` = the version of the model being trained. This version will be used in the exported model file name.
* `[MODEL_STORAGE_BUCKET]` = the Google Cloud Storage bucket where the tuned-model is located
* `[CLASSIFICATION_LABELS]` =  the classification labels that will be used as part of the file header for the output file, e.g. for the `bl_chemical_to_disease` task, the classification labels string should be "`treats false`".


And then to classify sentences that are located in files in a Google Cloud Storage bucket, run the following:

```
docker run --rm [TASK_NAME]-predict:[TUNED_MODEL_VERSION] [SENTENCE_BUCKET] [COLLECTION] [OUTPUT_BUCKET]
```

 where: 
* `[TASK_NAME]` = the name of the task (this name must match the image name used in the previous docker command)
* `[TUNED_MODEL_VERSION]` = the version of the model to use (this version must match the container version used in the previous 
* `[SENTENCE_BUCKET]` = the full GCP path to where the TSV to-be-classified sentence files are located, e.g. gs://xyz/sentences/chemical-disease/
* `[COLLECTION]` = the name of the collection being processed, e.g. PUBMED_SUB_31, 2021_06_08; this will be embedded in the output file name.
* `[OUTPUT_BUCKET]` = the output bucket where classified sentences will be placed, e.g. `gs://xyz`

