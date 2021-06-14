#!/bin/bash

# task_name is getting passed from the container - it is set during docker build
TASK_NAME=$1

# should match the labels that are defined in prob2label.py (tab-delimited)
CLASSIFICATION_LABELS=$2

# the version of the model used to classify the sentences - the version is used as part of the output path
TUNED_MODEL_VERSION=$3

# SENTENCE_BUCKET is getting passed in at runtime, i.e. during docker run
# This should be the full gs://bucket/a/b/c path to where the BERT input sentence TSV files are located.
SENTENCE_BUCKET=$4

# the name of the collection being processed - this will be part of the output file name
COLLECTION=$5

# the gcp bucket where the classified (labeled) sentences will be stored
OUTPUT_BUCKET=$6

# download the sentence files to process
# cat the sentence files into a single file called test.tsv in the $DATASET_DIR
mkdir /home/dev/sentences
pushd /home/dev/sentences
gsutil cp "$SENTENCE_BUCKET/*.tsv" .
# The sentence files have an identifier in the first column and the sentence to be classified 
# in the second column with placeholders, e.g. $GENE@, in place of entities. It may have other columns
# that contain metadata that are not needed during classification so we take just the first two columns
# here when forming the input file for BERT.
cat *.tsv > $DATASET_DIR/test.tsv
# add a header to test.tsv
sed -i '1s/^/id	sentence\n/' $DATASET_DIR/test.tsv
popd

# Look at the checkpoint file in the model directory to determine the init checkpoint
INIT_CKPT=$(head -n 1 $BlueBERT_DIR/checkpoint | cut -f 2 -d " " | sed 's/"//g')

# classify sentences with the previously trained BERT model
pushd /home/dev/ccp-bluebert.git
python bluebert/run_bluebert.py \
  --do_train=false \
  --do_eval=false \
  --do_predict=true \
  --task_name=$TASK_NAME \
  --vocab_file=$BlueBERT_DIR/vocab.txt \
  --bert_config_file=$BlueBERT_DIR/bert_config.json \
  --init_checkpoint=$BlueBERT_DIR/$INIT_CKPT \
  --num_train_epochs=10.0 \
  --data_dir=$DATASET_DIR \
  --output_dir=$OUTPUT_DIR \
  --do_lower_case=true
[ $? -eq 0 ] || exit 1
popd

# add a header to the bert output
pushd $OUTPUT_DIR
echo "This should be the output directory: $OUTPUT_DIR == $(pwd)"
echo $(ls -l)
sed -i "1s/^/$CLASSIFICATION_LABELS\n/" test_results.tsv
[ $? -eq 0 ] || exit 1
paste $DATASET_DIR/test.tsv test_results.tsv | gzip > classified_sentences.tsv.gz
[ $? -eq 0 ] || exit 1

# export the bert output file
gsutil cp classified_sentences.tsv.gz "${OUTPUT_BUCKET}/output/classified_sentences/${TASK_NAME}/${TUNED_MODEL_VERSION}/${TASK_NAME}.${TUNED_MODEL_VERSION}.${COLLECTION}.classified_sentences.tsv.gz"
[ $? -eq 0 ] || exit 1
popd