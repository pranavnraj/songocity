#!/bin/sh
exec java -jar -Dspring-boot.run.jvmArguments=-Djdk.tls.client.protocols=TLSv1.2 /app.jar
