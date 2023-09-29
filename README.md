# Aws RDS Eventual Consistency Test

## Prereq

1. Java 11 (Corretto was the tested version)
2. Bash shell
3. Networking to the maria db instance
4. Credentials to the maria db instance
5. A database created on the maria db instance

### Operation

This is a program that will test the acid compliance of a rds vs ec2 mariadb instances. To run,
unzip the program, from the unzipped directory run `./runTests.sh`, you may need to chmod a+x the
runTests.sh file. The source code is included. The test is rerunable and will drop older data on
rerun.
---
What it will do is create two tables (queue and work_completed). Populate the queue table with a
default of 1000 queue records. Then kick off 10 threads per instance (default 10 running java
instances). These will attempt to update the same rows against each the rows. Once each instance is
completed for all 1000 rows, it will print Duplicates Detected: (True, False). If true then two
updates to the same record were allowed between two different sessions. Parameters for the shell
script is a url, username, password, (optional) test size as integer. The url must be in this format
jdbc:mariadb://{url}:{port}/{dbname}.
Example `./runTest.sh "jdbc:mariadb://fooUrl:3306/barDb" fooUser barPassword`
---
Notes:
If you need to kill the jobs, `pkill awsRdsTest.jar` will do it. Also MariaDb will
---

# Disclaimer

Auric Mason does not make any representation or warranty with respect to any OSS or free software
that may be included in this software. Auric Mason hereby disclaims all liability to you or any
third party related to any such software that may be included in this software.