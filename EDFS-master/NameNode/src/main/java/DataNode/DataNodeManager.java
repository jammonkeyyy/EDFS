package DataNode;

import java.util.ArrayList;

public class DataNodeManager {
    ArrayList <DataNode> dataNodes;

    //该因子用于决定返回哪些dataNode节点用于存储
    static private int fact=0;

    public DataNodeManager(){
        dataNodes=new ArrayList<DataNode>();
    }

    public void addDataNode(String u){
        dataNodes.add(new DataNode(dataNodes.size(),u));
    }

    public void addFileBlock(String fb,int id){
        DataNode dn=searchByID(id);
        if(dn== null){
            System.err.println("block not found");
            return;
        }
        dn.addFileBlock(fb);
    }

    private DataNode searchByID(int id){
        for(DataNode dn:dataNodes)
            if(dn.getNodeID()==id)
                return dn;
        return null;
    }

    public DataNode searchByUrl(String u){
        for(DataNode dn:dataNodes)
            if(dn.getUrl().equals(u))
                return dn;
        return null;
    }

    public ArrayList<DataNode> getDatanodesByReplica(int r) {
        ArrayList <DataNode> result=new ArrayList<DataNode>();
        if(dataNodes.size()<=0){
            System.err.println("more DataNode needed");
            return null;
        }

        for(int i=0;i<r;i++){
            result.add(searchByID(this.fact));
            fact++;
            if(fact==dataNodes.size())
                fact=0;
        }

        return result;
    }


    //以下用于测试
    public void print(){
        for(DataNode dataNode:dataNodes){
            System.out.println("datanodeID:"+dataNode.getNodeID()+" url:"+dataNode.getUrl());
            for(String string:dataNode.getFileBlockList())
                System.out.println("fileBlock:"+string);
        }
    }
}
