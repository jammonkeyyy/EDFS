package DataNode;

import java.util.ArrayList;

public class DataNode {
    private int NodeID;
    private String url;
    private ArrayList <String> fileBlockList=new ArrayList<String>();

    public DataNode(int id,String u){
        NodeID=id;
        url=u;
    }

    public int getNodeID() {
        return NodeID;
    }

    public void addFileBlock(String fb){
        fileBlockList.add(fb);
    }

    public String getUrl() {
        return url;
    }

    public ArrayList<String> getFileBlockList() {
        return fileBlockList;
    }

    public void deleteFileBlock(String fb){
        for(int i=0;i<fileBlockList.size();i++)
            if(fileBlockList.get(i).equals(fb))
                fileBlockList.remove(i);
    }
}
