package com.github.dqqzj.fastdfs.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.github.dqqzj.fastdfs.common.NameValuePair;
import com.github.dqqzj.fastdfs.sources.FileInfo;
import com.github.dqqzj.fastdfs.sources.StorageClient;
import com.github.dqqzj.fastdfs.sources.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dqqzj.fastdfs.sources.ClientGlobal;
import com.github.dqqzj.fastdfs.sources.ServerInfo;
import com.github.dqqzj.fastdfs.sources.StorageServer;
import com.github.dqqzj.fastdfs.sources.TrackerClient;

public class FileManager implements Config {
    static Logger logger = LoggerFactory.getLogger(FileManager.class);

    private static TrackerClient trackerClient;
    private static TrackerServer trackerServer;
    private static StorageServer storageServer;
    private static StorageClient storageClient;
    private static String DEFAULT_GROUP_NAME;//单点时默认groupname

    static { // Initialize Fast DFS Client configurations
        try {
            String classPath = new File(FileManager.class.getResource("/").getFile()).getCanonicalPath();
            String fdfsClientConfigFilePath = classPath + File.separator + CLIENT_CONFIG_FILE;
            logger.info("Fast DFS configuration file path:" + fdfsClientConfigFilePath);
            ClientGlobal.init(fdfsClientConfigFilePath);
            trackerClient = new TrackerClient();
            trackerServer = trackerClient.getConnection();
            DEFAULT_GROUP_NAME = ClientGlobal.getDEFAULT_GROUP_NAME();
            storageClient = new StorageClient(trackerServer, storageServer);
        } catch (Exception e) {
            logger.error("init error", e);
        }
    }

    /**
     * @param fileName
     * @param extension
     * @param is
     * @return
     * @throws IOException
     * @throws
     * @Title: FileManager
     * @Description: 上传文件
     * @author mengfanzhu
     */
    public static String upload(String fileName, String extension, InputStream is) throws IOException {
        byte[] file_buff = null;
        if (is != null) {
            int len = is.available();
            file_buff = new byte[len];
            is.read(file_buff);
        }
        FastDFSFile file = new FastDFSFile(fileName, file_buff, extension);
        return upload(file);
    }

    public static String upload(FastDFSFile file) {
        logger.info("File Name: " + file.getFileName() + "  File Length: " + file.getContent().length);

        NameValuePair[] meta_list = new NameValuePair[3];
        meta_list[0] = new NameValuePair("width", "120");
        meta_list[1] = new NameValuePair("heigth", "120");
        meta_list[2] = new NameValuePair("author", "Diandi");

        long startTime = System.currentTimeMillis();
        String[] uploadResults = null;
        try {
            uploadResults = storageClient.upload_file(file.getContent(),
                    file.getExt(), meta_list);
        } catch (IOException e) {
            logger.error("IO Exception when uploadind the file: " + file.getFileName(), e);
        } catch (Exception e) {
            logger.error("Non IO Exception when uploadind the file: " + file.getFileName(), e);
            e.printStackTrace();
        }
        logger.info("upload_file time used: " + (System.currentTimeMillis() - startTime) + " ms");

        if (uploadResults == null) {
            logger.error("upload file fail, error code: " + storageClient.getErrorCode());
        }

        String groupName = uploadResults[0];
        String remoteFileName = uploadResults[1];

        String fileAbsolutePath = PROTOCOL
                + trackerServer.getInetSocketAddress().getHostName() + ":"
                + TRACKER_NGNIX_PORT + SEPARATOR + groupName + SEPARATOR
                + remoteFileName;

        logger.info("upload file successfully!!!  " + "group_name: "
                + groupName + ", remoteFileName:" + " " + remoteFileName);

        return fileAbsolutePath;

    }

    /**
     * @param groupName
     * @param remoteFileName
     * @return
     * @throws
     * @Title: FileManager
     * @Description: getFile by groupName& fileName
     * @author mengfanzhu
     */
    public static FileInfo getFile(String groupName, String remoteFileName) {
        try {
            return storageClient.get_file_info(groupName, remoteFileName);
        } catch (IOException e) {
            logger.error("IO Exception: Get File from Fast DFS failed", e);
        } catch (Exception e) {
            logger.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return null;
    }

    /**
     * @param remoteFileName
     * @return
     * @throws
     * @Title: FileManager
     * @Description: 单点时可取默认groupname
     * @author mengfanzhu
     */
    public static FileInfo getFile(String remoteFileName) {
        return getFile(DEFAULT_GROUP_NAME, remoteFileName);
    }

    public static void deleteFile(String groupName, String remoteFileName) throws Exception {
        storageClient.delete_file(groupName, remoteFileName);
    }

    /**
     * @param remoteFileName
     * @throws Exception
     * @throws
     * @Title: FileManager
     * @Description: 单点时去默认 groupname
     * @author mengfanzhu
     */
    public static void deleteFile(String remoteFileName) throws Exception {
        deleteFile(DEFAULT_GROUP_NAME, remoteFileName);
    }

    public static StorageServer[] getStoreStorages(String groupName) throws IOException {
        return trackerClient.getStoreStorages(trackerServer, groupName);
    }

    public static ServerInfo[] getFetchStorages(String groupName, String remoteFileName) throws IOException {
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }
}
