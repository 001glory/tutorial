#### **摘要：**
FastDFS是一个开源的轻量级分布式文件系统，它对文件进行管理，功能包括：文件存储、文件同步、文件访问（文件上传、文件下载）等，解决了大容量存储和负载均衡的问题。特别适合以文件为载体的在线服务，如相册网站、视频网站等等。
FastDFS为互联网量身定制，充分考虑了冗余备份、负载均衡、线性扩容等机制，并注重高可用、高性能等指标，使用FastDFS很容易搭建一套高性能的文件服务器集群提供文件上传、下载等服务。
#### **简介：**
FastDFS服务端有两个角色：跟踪器（tracker）和存储节点（storage）。跟踪器主要做调度工作，在访问上起负载均衡的作用。
存储节点存储文件，完成文件管理的所有功能：就是这样的存储、同步和提供存取接口，FastDFS同时对文件的metadata进行管理。所谓文件的meta data就是文件的相关属性，以键值对（key value）方式表示，如：width=1024，其中的key为width，value为1024。文件metadata是文件属性列表，可以包含多个键值对。
跟踪器和存储节点都可以由一台或多台服务器构成。跟踪器和存储节点中的服务器均可以随时增加或下线而不会影响线上服务。其中跟踪器中的所有服务器都是对等的，可以根据服务器的压力情况随时增加或减少。
为了支持大容量，存储节点（服务器）采用了分卷（或分组）的组织方式。存储系统由一个或多个卷组成，卷与卷之间的文件是相互独立的，所有卷的文件容量累加就是整个存储系统中的文件容量。一个卷可以由一台或多台存储服务器组成，一个卷下的存储服务器中的文件都是相同的，卷中的多台存储服务器起到了冗余备份和负载均衡的作用。
在卷中增加服务器时，同步已有的文件由系统自动完成，同步完成后，系统自动将新增服务器切换到线上提供服务。
当存储空间不足或即将耗尽时，可以动态添加卷。只需要增加一台或多台服务器，并将它们配置为一个新的卷，这样就扩大了存储系统的容量。
FastDFS中的文件标识分为两个部分：卷名和文件名，二者缺一不可。
#### **上传交互过程**
1. client询问tracker上传到的storage，不需要附加参数；
2. tracker返回一台可用的storage；
3. client直接和storage通讯完成文件上传。
FastDFS file download

#### **下载交互过程**
1. client询问tracker下载文件的storage，参数为文件标识（卷名和文件名）；
2. tracker返回一台可用的storage；
3. client直接和storage通讯完成文件下载。
需要说明的是，client为使用FastDFS服务的调用方，client也应该是一台服务器，它对tracker和storage的调用均为服务器间的调用。

#### **如何安装FastDFS？**
[安装教程](https://www.cnblogs.com/cnmenglang/p/6731209.html)
### **重点：**
#### **java如何访问FastDFS？**

其中可以使用非maven方式，不过我亲自试过了，会发生以下几个问题，线上环境部署采用jar包形式，会去读取`fdfs_client.conf`文件中的内容，不太方便，而且上传的性能感觉和maven的方式客户端相比比较慢
#### 测试工具类如下：
```java
package com.qzj.fastfds;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.qzj.fastfds.client.FastDFSFile;
import com.qzj.fastfds.client.FileManager;
import com.qzj.fastfds.sources.FileInfo;
import com.qzj.fastfds.sources.ServerInfo;
import com.qzj.fastfds.sources.StorageServer;

public class FastdfsUtils {
    public static void main(String[] args) throws Exception {
        List<String> tempList = new ArrayList<>();
        tempList.add("M00/04/12/rB8H6lr0EaKAGoqdAALBHGE9QI8059.jpg");
        for (String url:tempList){
            new FastdfsUtils().deleteFile("group1", url);
        }

    }

    /**
     * 上传图片到linux图片服务器上
     */
    public void upload() throws Exception {
        Resource resource = new ClassPathResource("static/img/1.jpg");
        File content = resource.getFile();
        String fileName = content.getName().split("\\.")[0];
        FileInputStream fis = new FileInputStream(content);
        byte[] file_buff = null;
        if (fis != null) {
            int len = fis.available();
            file_buff = new byte[len];
            fis.read(file_buff);
        }
        FastDFSFile file = new FastDFSFile(fileName, file_buff, "jpg");
        String fileAbsolutePath = FileManager.upload(file);
        System.out.println(fileAbsolutePath);
        fis.close();
    }

    public static void deleteFile(String groupName, String remoteFileName) {
        try {
            FileManager.deleteFile(groupName, remoteFileName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getFile() throws Exception {
        FileInfo file = FileManager.getFile("M00/00/00/wKgBAloeJZ-Ab0SXAABbqxLdCuI132.jpg");
        String sourceIpAddr = file.getSourceIpAddr();
        long size = file.getFileSize();
        System.out.println("ip:" + sourceIpAddr + ",size:" + size);
    }


    public void getStorageServer() throws Exception {
        StorageServer[] ss = FileManager.getStoreStorages("group1");

        for (int k = 0; k < ss.length; k++) {
            System.err.println(k + 1 + ". " + ss[k].getInetSocketAddress().getAddress().getHostAddress() + ":" + ss[k].getInetSocketAddress().getPort());
        }
    }


    public void getFetchStorages() throws Exception {
        ServerInfo[] servers = FileManager.getFetchStorages("group1", "M00/00/00/wKgBAloeJZ-Ab0SXAABbqxLdCuI132.jpg");

        for (int k = 0; k < servers.length; k++) {
            System.err.println(k + 1 + ". " + servers[k].getIpAddr() + ":" + servers[k].getPort());
        }
    }
}
```

#### **fdfs_client.conf配置文件如下：**
```java
connect_timeout = 2
network_timeout = 30
charset = UTF-8
http.tracker_http_port = 8080
http.anti_steal_token = no
http.secret_key = FastDFS1234567890

tracker_server = 10.0.11.247:22122
tracker_server = 10.0.11.248:22122
```

### **springboot连接FastDFS**
首先导入相关依赖文件如下：
```java
 <dependency>
      <groupId>com.github.tobato</groupId>
      <artifactId>fastdfs-client</artifactId>
      <version>1.25.2-RELEASE</version>
 </dependency>
 ```
 
#### **FastDFSClientWrapper封装类**

然后创建操作封装类如下：
```java
package com.qzj.fastfds;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.exception.FdfsUnsupportStorePathException;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by qzj on 2018/5/10
 */
@Component
public class FastDFSClientWrapper {


    private final Logger logger = LoggerFactory.getLogger(FastDFSClientWrapper.class);

    @Autowired
    private FastFileStorageClient storageClient;


    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件访问地址
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        StorePath storePath = storageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
        return getResAccessUrl(storePath);
    }


    // 封装图片完整URL地址
    private String getResAccessUrl(StorePath storePath) {
        String fileUrl = "http://xx.xx.xx.xx:8888" + "/" + storePath.getFullPath();
        return fileUrl;
    }

    /**
     * 删除文件
     * @param fileUrl 文件访问地址
     * @return
     */
    public void deleteFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return;
        }
        try {
            StorePath storePath = StorePath.praseFromUrl(fileUrl);
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
        } catch (FdfsUnsupportStorePathException e) {
            logger.warn(e.getMessage());
        }
    }

}
```

#### **总结：**
本章内容讲解了一下如何搭建轻量级文件系统，可能大公司都有自己的文件系统，不过自己亲自试一试的话会更好的体会一下文件系统的优势，然后给大家介绍了我在工作中所使用到的工具，建议搭建采用springboot方式构建，2种方式尽量不要一起使用，第一种方式代码量太大了，看起来不是很优雅，希望这篇文章对大家有所帮助！

本文的代码已经上传到GitHub，大家可以参考一下：
https://github.com/dqqzj/tutorial/tree/master/fastfds
        
        
        
        
        
        
        