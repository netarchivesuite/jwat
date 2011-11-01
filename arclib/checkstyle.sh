#!/bin/sh
java -Dcheckstyle.localeCountry=dk -Dcheckstyle.cache.file=checkstyleCache -jar checkstyle-5.4-all.jar -c NetarchiveSuite_checks.xml -r src/
