#!/usr/bin/env bash

INFILE=$1
OUTFILE=$2
DEST_ALIAS=$3

function usage() {
  echo "USAGE:   ./p12-to-jks <INPUT_CERT_P12_FILE> <OUTPUT_CERT_P12_FILE> <OUTPUT_ALIAS>"
  echo "EXAMPLE: ./p12-to-jks in.p12 out myalias"
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

if [ -z "$DEST_ALIAS" ]
then
      echo "Required: dest alias"
      usage
      exit 2
fi

echo 1/5: Creating temporary new P12 file with new password
echo
echo "--- ENTER YOUR NEW PASSWORD: "
read -r -s NEW_PW
echo

echo Parameters
echo Infile: $INFILE
echo Outfile: $OUTFILE.p12
echo

echo 2/5: Extracting private key PEM
echo
echo "--- ENTER YOUR EXISTING CERTIFICATE PASSWORD --- "
echo
read -r -s CERT_PW
openssl pkcs12 -in "$INFILE" -nocerts -out privateKey.pem -passin pass:$CERT_PW -passout pass:$NEW_PW

echo 3/5: Extracting public key PEM
openssl pkcs12 -in "$INFILE" -clcerts -nokeys -out publicCert.pem -passin pass:$CERT_PW

FRIENDLY_NAME=`cat privateKey.pem | grep friendlyName | awk '{print substr($0, 19, 150); exit}'`

SRC_ALIAS=$FRIENDLY_NAME
echo Found alias: $FRIENDLY_NAME
echo

echo 4/5: Converting PEM to P12

#echo
#echo "--- ENTER YOUR NEW PASSWORD --- "
openssl pkcs12 -export -out $OUTFILE.p12 \
  -inkey privateKey.pem \
  -in publicCert.pem \
  -passin pass:$NEW_PW \
  -passout pass:$NEW_PW \
  -name "$FRIENDLY_NAME"

rm privateKey.pem publicCert.pem

# Convert to JKS
echo 5/5: Converting new P12 to JKS keystore
#echo
#echo "--- ENTER YOUR NEW PASSWORD --- "
keytool -importkeystore \
    -srckeystore $OUTFILE.p12 \
    -srcstoretype pkcs12 \
    -destkeystore $OUTFILE.jks \
    -deststoretype jks \
    -srcstorepass $NEW_PW\
    -deststorepass $NEW_PW \
    -srcalias "$SRC_ALIAS" \
    -destalias "$DEST_ALIAS"
