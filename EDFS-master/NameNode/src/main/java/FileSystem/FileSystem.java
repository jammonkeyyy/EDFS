package FileSystem;

import java.util.ArrayList;

public class FileSystem {
    ArrayList <MyFile> allFile;

    public FileSystem(){
        allFile=new ArrayList<MyFile>();
    }

    public void addFile(String filename){
        allFile.add(new MyFile(filename));
    }

    public ArrayList <MyFile> getAllFiles(){
        return allFile;
    }

    public String getAllFile() {
        String output=new String();
        for(MyFile myFile:allFile)
            output+=(myFile.getFileName()+"\n");
        return output;
    }

    public boolean isExist(String filename){
        for(MyFile myFile:allFile) {

            //System.out.println("这是检测是否有同名文件部分：");
            //System.out.println(myFile.getFileName());
            //System.out.println(filename);

            if(myFile.getFileName().equals(filename))
                return true;
        }
        return false;
    }

    public MyFile findFileByName(String filename){
        for(MyFile myFile:allFile) {

            //System.out.println("这是检测是否有同名文件部分：");
            //System.out.println(myFile.getFileName());
            //System.out.println(filename);

            if(myFile.getFileName().equals(filename))
                return myFile;
        }
        return null;
    }

    public void DeleteFileByName(String filename){
        for(int i=0;i<allFile.size();i++) {
            if(allFile.get(i).getFileName().equals(filename))
                allFile.remove(i);
        }
    }

    //以下用于测试
    public void print(){
        for(MyFile myFile:allFile) {
            System.out.println("filename:"+myFile.getFileName() + ":");
            for(Block block:myFile.getBlockArrayList()){
                System.out.println("blockID:"+block.getBlockID()+":");
                for(String string:block.getUrls())
                    System.out.println("url:"+string);
            }
        }
    }

}
