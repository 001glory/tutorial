package com.github.dqqzj.fastdfs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.dqqzj.fastdfs.client.FastDFSFile;
import com.github.dqqzj.fastdfs.client.FileManager;
import com.github.dqqzj.fastdfs.sources.FileInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.github.dqqzj.fastdfs.sources.ServerInfo;
import com.github.dqqzj.fastdfs.sources.StorageServer;

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
