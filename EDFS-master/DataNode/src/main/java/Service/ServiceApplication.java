package Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class ServiceApplication {

    public static String FILEDIR=new String ("F:/DFSDATA/");
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

    @RequestMapping(value="/{filename}.{id}.{nodeID}", method = RequestMethod.POST)
    public @ResponseBody String UploadFile(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request,
                                           @PathVariable String filename,
                                           @PathVariable String id,
                                           @PathVariable String nodeID) {



        String fileName = filename+"."+id;

        //System.out.println(filename);
        //System.out.println(suffix);

        String filePath=FILEDIR+nodeID+"/";

        try {
            File targetFile = new File(filePath);
            if(!targetFile.exists()){
                targetFile.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(filePath+fileName);

            out.write(file.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        //返回json
        return "uploadimg success";
    }

    @RequestMapping(value="/{filename}.{id}.{nodeID}", method = RequestMethod.DELETE)
    public void DeleteFile(HttpServletRequest request,
                                           @PathVariable String filename,
                                           @PathVariable String id,
                                           @PathVariable String nodeID) {

        //System.out.println("进入删除函数");

        String fileName = filename+"."+id;

        //System.out.println(fileName);

        String filePath=FILEDIR+nodeID+"/";

        //System.out.println(filePath+fileName);

        File fileToDelete=new File(filePath+fileName);

        fileToDelete.delete();
    }



    @RequestMapping(value="/{filename}.{id}.{nodeID}", method = RequestMethod.GET)
    public void Download( @PathVariable String filename,
                          @PathVariable String id,
                          @PathVariable String nodeID,
                          HttpServletResponse res) {

        String fileName = filename+"."+id;
        res.setHeader("content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            os = res.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(new File(FILEDIR+nodeID+"/"
                    + fileName)));

            //System.out.println(FILEDIR+nodeID+"/" + fileName);

            int i = bis.read(buff);
            while (i != -1) {

                System.out.println(i);

                os.write(buff, 0, i);
                os.flush();
                i = bis.read(buff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}