#!/bin/bash

TASK_NAME=$1
TUNED_MODEL_VERSION=$2
EPOCHS=$3
MODEL_STORAGE_BUCKET=$4


echo "~~~~~~~~TASK_NAME: ${TASK_NAME}"
echo "~~~~~~~~TUNED_MODEL_VERSION: ${TUNED_MODEL_VERSION}"
echo "~~~~~~~~EPOCHS: ${EPOCHS}"
echo "~~~~~~~~MODEL_STORAGE_BUCKET: ${MODEL_STORAGE_BUCKET}"

if [ -z "${MODEL_STORAGE_BUCKET}" ]; then
  echo "!!!!!! MODEL_STORAGE_BUCKET IS NOT SET. EXITING... !!!!!!  "
  exit 1
fi


# tune the BERT model
pushd /home/dev/ccp-bluebert.git
python bluebert/run_bluebert.py \
  --do_train=true \
  --do_eval=true \
  --do_predict=true \
  --task_name=$TASK_NAME \
  --vocab_file=$BlueBERT_DIR/vocab.txt \
  --bert_config_file=$BlueBERT_DIR/bert_config.json \
  --init_checkpoint=$BlueBERT_DIR/bert_model.ckpt \
  --num_train_epochs=$EPOCHS \
  --data_dir=$DATASET_DIR \
  --output_dir=$OUTPUT_DIR \
  --do_lower_case=true
[ $? -eq 0 ] || exit 1
popd

# convert the BERT output file to the format expected by the BLUE_Benchmark code
pushd /home/dev/output
python /home/dev/prob2label.py ${TASK_NAME} > /home/dev/data/bert.out.tsv
[ $? -eq 0 ] || exit 1
popd

# evaluate the BERT model
pushd /home/dev/blue_benchmark.git
python blue/eval_rel.py /home/dev/data/test.blue.gs /home/dev/data/bert.out.tsv | tee /home/dev/data/bert.score
[ $? -eq 0 ] || exit 1
popd

# export the model to a bucket
pushd /home/dev/output
# copy the vocab.txt and bert_config.json files into the tuned model directory before creating tarball
cp $BlueBERT_DIR/vocab.txt .
[ $? -eq 0 ] || exit 1
cp $BlueBERT_DIR/bert_config.json .
[ $? -eq 0 ] || exit 1
tar -czvf "${TASK_NAME}.${TUNED_MODEL_VERSION}.tar.gz" *
[ $? -eq 0 ] || exit 1


echo "MODEL OUTPUT FILE: ${MODEL_STORAGE_BUCKET}/bert/${TASK_NAME}/${TASK_NAME}.${TUNED_MODEL_VERSION}.tar.gz"
echo "SCORE OUTPUT FILE: ${MODEL_STORAGE_BUCKET}/bert/${TASK_NAME}/${TASK_NAME}.${TUNED_MODEL_VERSION}.score.txt"

# NOTE: If the storage bucket path changes, it must also be updated in 
#       the predict.Dockerfile where the model is downloaded.
# note: we remove spaces from the output path b/c MODEL_STORAGE_BUCKET has a trailing space
OUT_PATH=$(echo "${MODEL_STORAGE_BUCKET}/bert/${TASK_NAME}/${TASK_NAME}.${TUNED_MODEL_VERSION}.tar.gz" | tr -d " ")
gsutil cp "/home/dev/output/${TASK_NAME}.${TUNED_MODEL_VERSION}.tar.gz" "${OUT_PATH}"
[ $? -eq 0 ] || exit 1

# export the model evaluation metrics to a bucket
# note: we remove spaces from the output path b/c MODEL_STORAGE_BUCKET has a trailing space
OUT_PATH=$(echo "${MODEL_STORAGE_BUCKET}/bert/${TASK_NAME}/${TASK_NAME}.${TUNED_MODEL_VERSION}.score.txt" | tr -d " ")
gsutil cp /home/dev/data/bert.score "${OUT_PATH}"
[ $? -eq 0 ] || exit 1