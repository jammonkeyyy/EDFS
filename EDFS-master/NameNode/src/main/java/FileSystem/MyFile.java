package FileSystem;

import java.util.ArrayList;

public class MyFile {
    ArrayList <Block> blockArrayList;
    String fileName;

    public MyFile(String name){
        blockArrayList=new ArrayList<Block>();
        fileName=name;
    }

    public Block addBlock(int bid){
        blockArrayList.add(new Block(bid));
        int size=blockArrayList.size();
        return blockArrayList.get(size-1);
    }

    public String getFileName() {
        return fileName;
    }

    public ArrayList<Block> getBlockArrayList() {
        return blockArrayList;
    }
}
