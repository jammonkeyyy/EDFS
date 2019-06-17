package NameNode;

import DataNode.DataNode;
import DataNode.DataNodeManager;
import FileSystem.FileSystem;
import FileSystem.Block;
import FileSystem.MyFile;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

@SpringBootApplication
@EnableEurekaServer
@RestController
public class NameNodeApplication {

    @Value(value="${block.default-size}")
    public int BlockSize;

    @Value(value="${block.default-replicas}")
    public int replicaNum;


    public String nameNodeTempDir="F:/DFSDATA/nameNodeTemp/";

    /*TODO:replace file system by database */
    //file system
    FileSystem myFileSystem=new FileSystem();



    //DataNode manager
    DataNodeManager dataNodeManager=new DataNodeManager();


    public static void main(String [] args){
        SpringApplication.run(NameNodeApplication.class,args);
    }

    @GetMapping("/AllFile")
    public String ListAllFile(){
        return myFileSystem.getAllFile();
    }


    @RequestMapping(value="/{filename}.{suffix}", method = RequestMethod.POST)
    public @ResponseBody void UploadFile(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request,
                                           @PathVariable String filename,
                                           @PathVariable String suffix) throws IOException{

        //将文件存入fileSystem
        String fileName = filename+"."+suffix;
        //判断是否有同名文件
        if(myFileSystem.isExist(fileName)){
            System.out.println("the file already exists");
            return;
        }

        myFileSystem.addFile(fileName);

        //解析文件属性
        int fileSize=file.getBytes().length;
        int blockNum=fileSize/BlockSize;
        blockNum=fileSize % BlockSize ==0 ? blockNum : blockNum+1;


        FileInputStream fis = (FileInputStream) file.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(fis);

        for(int blockId = 0; blockId < blockNum; blockId++){

            int fileNum=myFileSystem.getAllFiles().size();
            Block file_block=myFileSystem.getAllFiles().get(fileNum-1).addBlock(blockId);

            // 分割文件，临时存到硬盘，
            byte blockByteArray[] = new byte[BlockSize];
            int blockSizeTemp = bis.read(blockByteArray);

            String blockName = filename+"."+blockId;

            File fileSlice=writeToLocal(blockName,blockByteArray,blockSizeTemp);

            // 选择节点存储block
            ArrayList<DataNode> dataNodes = dataNodeManager.getDatanodesByReplica(replicaNum);

            // 上传
            for(int i = 0; i < replicaNum; ++i){

                // 获取该datanode的url
                String dataNodeUrl = dataNodes.get(i).getUrl();

                //System.out.println(dataNodeUrl+filename+"."+blockId+"."+dataNodes.get(i).getNodeID());
                // 上传Block
                sendFileBlock(dataNodeUrl+filename+"."+blockId+"."+dataNodes.get(i).getNodeID(),fileSlice);

                // 上传成功
                //System.out.println(blockName + "被上传到" + dataNodeUrl);

                // 更新DataNode信息
                dataNodes.get(i).addFileBlock(blockName);
                file_block.addUrl(dataNodeUrl);
            }

            // 删除临时存储的碎片
            fileSlice.delete();
        }
        fis.close();
        bis.close();

        System.out.println("-------------------文件上传------------------------------");
        dataNodeManager.print();
        System.out.println();
        myFileSystem.print();
        System.out.println("-------------------文件上传------------------------------");
    }


    @RequestMapping(value="/{filename}.{suffix}", method = RequestMethod.GET)
    public void Download( @PathVariable String filename,
                          @PathVariable String suffix,
                          HttpServletResponse res) throws Exception{

        String fileName = filename+"."+suffix;
        MyFile file=myFileSystem.findFileByName(fileName);
        //判断文件是否存在
        if(file==null){
            System.out.println("file doesn't exists");
            return;
        }

        File tempLoadFile=new File(nameNodeTempDir+fileName);
        tempLoadFile.createNewFile();

        FileChannel downloadFileChannel=new FileOutputStream(tempLoadFile).getChannel();


        for(Block block:file.getBlockArrayList()){
            String url=block.getUrls().get(0);
            File fileSlice=getFileBlock(url,filename+"."+block.getBlockID());
            FileChannel inputChannel=new FileInputStream(fileSlice).getChannel();
            inputChannel.transferTo(0,fileSlice.length(),downloadFileChannel);
            inputChannel.close();
        }

        downloadFileChannel.close();

        res.setHeader("content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;

        os = res.getOutputStream();
        bis = new BufferedInputStream(new FileInputStream(new File(nameNodeTempDir + fileName)));

        int i = bis.read(buff);
        while (i != -1) {
            os.write(buff, 0, i);
            os.flush();
            i = bis.read(buff);
        }
        bis.close();

    }


    @RequestMapping(value="/{filename}.{suffix}", method = RequestMethod.DELETE)
    public void DeleteFile(@PathVariable String filename,
                              @PathVariable String suffix){

        String fileName = filename+"."+suffix;
        MyFile file=myFileSystem.findFileByName(fileName);
        //判断文件是否存在
        if(file==null){
            System.out.println("file doesn't exists");
            return;
        }


        for (Block block:file.getBlockArrayList()){
            for(String string:block.getUrls()){

                DataNode dn=dataNodeManager.searchByUrl(string);
                String URL=string+filename+"."+block.getBlockID()+"."+dn.getNodeID();

                //System.out.println("Sending url:"+URL);
                RestTemplate rest=new RestTemplate();
                rest.delete(URL);

                //维护dataNodeManager
                dn.deleteFileBlock(filename+"."+block.getBlockID());
            }
        }

        myFileSystem.DeleteFileByName(fileName);

        //System.out.println(blockNeedToDelete);


        System.out.println("-------------------文件删除------------------------------");
        myFileSystem.print();
        dataNodeManager.print();
        System.out.println("-------------------文件删除------------------------------");
    }



    private File writeToLocal(String fileName, byte[] blockByteArray, int length) {
        try {
            File file = new File(nameNodeTempDir + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(blockByteArray, 0, length);
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean sendFileBlock(String URL, File file) {
        // prepare params
        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", resource);

        // send post request
        RestTemplate rest = new RestTemplate();
        String response = rest.postForObject(URL, parameters, String.class);

        return response.equals("success");
    }


    private File getFileBlock(String dataNodeURL, String fileName) {
        try {
            // down load file into a input stream
            int nodeID=dataNodeManager.searchByUrl(dataNodeURL).getNodeID();

            String resourceURL = dataNodeURL + fileName+"."+nodeID;
            UrlResource urlResource = new UrlResource(new URL(resourceURL));
            InputStream inputStream = urlResource.getInputStream();

            // write into a byte array
            byte[] bytes = new byte[BlockSize];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n, blockSize = 0;
            while ( (n=inputStream.read(bytes)) != -1) {
                out.write(bytes,0,n);
                blockSize += n;
            }

            bytes = out.toByteArray();
            // write into a file
            File file = writeToLocal(fileName, bytes, blockSize);

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /*--------Eureka---------*/
    @EventListener
    public void listen(EurekaInstanceRegisteredEvent event) {
        InstanceInfo instanceInfo = event.getInstanceInfo();
        String dataNodeUrl = instanceInfo.getHomePageUrl();
        System.err.println(dataNodeUrl + "!!!!!!!!!!!!!!!!!!进行注册!!!!!!!!!!!!!!!!");

        dataNodeManager.addDataNode(dataNodeUrl);
    }

}
