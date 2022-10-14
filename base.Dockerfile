FROM tensorflow/tensorflow:1.15.4-gpu

RUN \
    # Update nvidia GPG key - from: https://github.com/NVIDIA/nvidia-docker/issues/1631
    rm /etc/apt/sources.list.d/cuda.list && \
    rm /etc/apt/sources.list.d/nvidia-ml.list && \
    apt-key del 7fa2af80 && \
    apt-get update && apt-get install -y --no-install-recommends wget && \
    wget https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-keyring_1.0-1_all.deb && \
    dpkg -i cuda-keyring_1.0-1_all.deb && \
    apt-get update

RUN apt-get update && apt-get install -y \
  git \
  less \
  vim \
  wget \
  && rm -rf /var/lib/apt/lists/*

# set up directories that will be used by this container (and its children) 
RUN mkdir -p /home/dev/models/baseline && \
    mkdir -p /home/dev/output && \
    mkdir -p /home/dev/data

WORKDIR /home/dev

# Installs google cloud sdk, this allows use of gsutil
# from: https://cloud.google.com/ai-platform/training/docs/custom-containers-training
RUN wget -nv \
    https://dl.google.com/dl/cloudsdk/release/google-cloud-sdk.tar.gz && \
    mkdir /root/tools && \
    tar xvzf google-cloud-sdk.tar.gz -C /root/tools && \
    rm google-cloud-sdk.tar.gz && \
    /root/tools/google-cloud-sdk/install.sh --usage-reporting=false \
        --path-update=false --bash-completion=false \
        --disable-installation-options && \
    rm -rf /root/.config/* && \
    ln -s /root/.config /config && \
    # Remove the backup directory that gcloud creates
    rm -rf /root/tools/google-cloud-sdk/.install/.backup

# Install a customized version of the NCBI BlueBERT implementation
RUN git clone https://github.com/UCDenver-ccp/bluebert.git ./ccp-bluebert.git 

# Path configuration
ENV PATH $PATH:/root/tools/google-cloud-sdk/bin

ENV DATASET_DIR '/home/dev/data'
ENV OUTPUT_DIR '/home/dev/output'
ENV PYTHONPATH '.'


# To build:
#
# docker build -t bluebert-base:[BASE_VERSION] -f base.Dockerfile