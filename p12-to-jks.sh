#!/usr/bin/env bash

INFILE=$1
OUTFILE=$2
#SRC_ALIAS=$3
DEST_ALIAS=$3

function usage() {
  #echo "USAGE:   ./p12-to-jks <INPUT_CERT_P12_FILE> <OUTPUT_CERT_P12_FILE> <INPUT_ALIAS> <OUTPUT_ALIAS>"
  echo "USAGE:   ./p12-to-jks <INPUT_CERT_P12_FILE> <OUTPUT_CERT_P12_FILE> <OUTPUT_ALIAS>"
  echo "EXAMPLE: ./p12-to-jks in.p12 out.p12 myalias"
}

if [ -z "$INFILE" ]
then
      echo "Required: input file name"
      usage
      exit 1
fi

if [ -z "$OUTFILE" ]
then
      echo "Required: output file name (without extensions)"
      usage
      exit 2
fi

#if [ -z "$SRC_ALIAS" ]
#then
#      echo "Required: src alias"
#      usage
#      exit 2
#fi

if [ -z "$DEST_ALIAS" ]
then
      echo "Required: dest alias"
      usage
      exit 2
fi

echo 1/3: Creating temporary new P12 file with new password
echo
echo "--- ENTER YOUR NEW PASSWORD: "
read -r -s NEW_PW
echo

echo Parameters
echo Infile: $INFILE
echo Outfile: $OUTFILE

echo 1/4: Extracting private key PEM
echo
echo "--- ENTER YOUR EXISTING CERTIFICATE PASSWORD --- "
openssl pkcs12 -in $INFILE -nocerts -out privateKey.pem -passout pass:$NEW_PW

echo 2/4: Extracting public key PEM
echo
echo "--- ENTER YOUR EXISTING CERTIFICATE PASSWORD --- "
openssl pkcs12 -in $INFILE -clcerts -nokeys -out publicCert.pem

FRIENDLY_NAME=`cat privateKey.pem | grep friendlyName | awk '{print substr($0, 19, 150); exit}'`

SRC_ALIAS=$FRIENDLY_NAME
echo Found alias: $FRIENDLY_NAME
echo

echo 3/4: Converting PEM to P12

echo
echo "--- ENTER YOUR NEW PASSWORD --- "
openssl pkcs12 -export -out $OUTFILE \
  -inkey privateKey.pem \
  -in publicCert.pem \
  -passin pass:$NEW_PW \
  -name "$FRIENDLY_NAME"

rm privateKey.pem publicCert.pem

# Convert to JKS
echo 4/4: Converting new P12 to JKS keystore
echo
echo "--- ENTER YOUR NEW PASSWORD --- "
keytool -importkeystore \
    -srckeystore $OUTFILE \
    -srcstoretype pkcs12 \
    -destkeystore $OUTFILE.jks \
    -deststoretype jks \
    -deststorepass $NEW_PW \
    -srcalias "$SRC_ALIAS" \
    -destalias "$DEST_ALIAS"
