/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.FileInputStream;

/**
 * Sample program to provide details on the seek issue
 * in GCS connector.  Please note that this is just for demo
 * purpose.
 */

public class TestGCSSeek {

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();

    //TODO : Please populate these values correctly for your GCS account
    conf.set("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem");
    conf.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS");
    conf.set("fs.gs.project.id", "rajeshtest-00000");  //SET YOUR PROJECT ID
    conf.set("fs.gs.working.dir", "/");
    conf.set("fs.gs.auth.service.account.email",
        "xyz@rajeshtest-00000.iam.gserviceaccount.com"); //SET YOUR SERVICE ACCOUNT DETAIL
    conf.set("fs.gs.auth.service.account.enable", "true");
    conf.set("fs.gs.auth.service.account.keyfile",
        "/Users/rbalamohan/Downloads/rajeshtest-xyz.p12"); //SET YOUR CREDENTIAL FILE

    File file = new File("./data/000000_0");
    byte[] data = new byte[(int) file.length()]; //small file
    try (FileInputStream fis = new FileInputStream(file)) {
      IOUtils.readFully(fis, data);
    }

    //TODO : Provide correct path to uplaod the file
    String gcsPath = "gs://rajeshtest-00000.appspot.com/000000_0";
    Path filePath = new Path(gcsPath);
    FileSystem fs = filePath.getFileSystem(conf);

    if (fs.exists(filePath)) {
      System.out.println("Deleting " + filePath + " (recursive=false)");
      fs.delete(filePath, false);
    }

    //Write sample data.
    try (FSDataOutputStream out = fs.create(filePath)) {
      out.write(data);
    }

    //Read data now.
    try (FSDataInputStream in = fs.open(filePath)) {
      long len = fs.getFileStatus(filePath).getLen();
      System.out.println("File Length : " + len);

      in.readFully(281655, new byte[16384], 0, 16384);
      in.readFully(297741, new byte[86], 0, 86); //This would error out with Seek bug

      System.out.println("Completed!!");
    }
  }

}
