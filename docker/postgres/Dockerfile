FROM postgres:14

# install build deps and build pgvector extension
RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    build-essential \
    git \
    ca-certificates \
    postgresql-server-dev-14 \
  && git clone https://github.com/pgvector/pgvector.git /tmp/pgvector \
  && cd /tmp/pgvector && make && make install \
  && rm -rf /tmp/pgvector \
  && apt-get remove -y git build-essential \
  && apt-get autoremove -y \
  && rm -rf /var/lib/apt/lists/*
