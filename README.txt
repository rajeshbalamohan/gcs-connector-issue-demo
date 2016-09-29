Steps:
=====

1. Populate your GCS details in TestGCSeek.java
	- Important settings are "fs.gs.project.id", "fs.gs.auth.service.account.email", "fs.gs.auth
	.service.account.keyfile", and the file path in "gcsPath".
2. "mvn clean package"
3. "java -cp gcs-connector-1.5.4-hadoop2-SNAPSHOT-shaded.jar:./target/gcs-connector-issue-demo-1.0-SNAPSHOT-jar-with-dependencies.jar: TestGCSSeek"
4. This would automatically upload the file to GCS and would run a seek test to reproduce the issue.


Example Output:
==============

File Length : 298039
Exception in thread "main" java.io.IOException: Somehow read -1 bytes trying to skip 297741 more bytes to seek to position 297741, size: 298039
	at com.google.cloud.hadoop.gcsio.GoogleCloudStorageReadChannel.position(GoogleCloudStorageReadChannel.java:535)
	at com.google.cloud.hadoop.fs.gcs.GoogleHadoopFSInputStream.seek(GoogleHadoopFSInputStream.java:301)
	at org.apache.hadoop.fs.FSInputStream.read(FSInputStream.java:74)
	at com.google.cloud.hadoop.fs.gcs.GoogleHadoopFSInputStream.read(GoogleHadoopFSInputStream.java:266)
	at org.apache.hadoop.fs.FSInputStream.readFully(FSInputStream.java:121)
	at org.apache.hadoop.fs.FSDataInputStream.readFully(FSDataInputStream.java:111)
	at TestGCSSeek.main(TestGCSSeek.java:69)


Issue:
======
1. "performLazySeek" needs to be added in GoogleCloudStorageReadChannel.position(pos)
2. Jar file with fix is located in "gcs-connector-1.5.4-hadoop2-SNAPSHOT-shaded-WITH-FIX.jar"
3. Same program would work fine with
  	"java -cp gcs-connector-1.5.4-hadoop2-SNAPSHOT-shaded-WITH-FIX.jar:./target/gcs-connector-issue-demo-1.0-SNAPSHOT-jar-with-dependencies.jar: TestGCSSeek"


Note:
=====
Huge amount of effort has gone in terms of developing FS contract tests in Hadoop. It would be
good to certify GCS connector against those contract tests to identify such issues.