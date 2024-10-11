#!/usr/bin/env bash

# make sure the script runs relative to the repo root
set -euo pipefail && cd "$(dirname "${BASH_SOURCE[0]}")/.."

info()  { printf "%s\n" "$*" >&1; }
error() { printf "%s\n" "$*" >&2; }
trap 'echo Changeset interrupted >&2; exit 2' INT TERM


info "Publishing via gradle publish task"
ORG_GRADLE_PROJECT_NEXUS_USERNAME="lol-yo" ./gradlew publish \
  -Psigning.secretKeyRingFile="../publish-mvn.asc \
  -Psigning.keyId="4AE5701C" \
  -Psigning.password="A/4DBzp2wAbmnSEip+Erb8Hx3oJckCYdbKxQgsvKE4MZa/iI6usCg2404wcOxPNC"

# NEXUS_USERNAME=josuemontano
# NEXUS_PASSWORD=h5,K/9GC&gQZ;?>C=^:4

# 403DC5174AE5701C
# signing.keyId=4AE5701C
# signing.password=A/4DBzp2wAbmnSEip+Erb8Hx3oJckCYdbKxQgsvKE4MZa/iI6usCg2404wcOxPNC
# signing.secretKeyRingFile=/Users/ullrich/Projects/magicbell-android/foobar.gpg
