package FileSystem;

import java.util.ArrayList;

public class Block {
    int blockID;
    ArrayList<String> urls=new ArrayList<String>();

    public Block(int bid){
        blockID=bid;
    }

    public int getBlockID() {
        return blockID;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void addUrl(String u){
        urls.add(new String(u));
    }
}
