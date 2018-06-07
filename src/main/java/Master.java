import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Master {
    BlockingQueue<String> blockingWorkQueue;
    ArrayList<Thread> threadList;

    int amountOfThreads;
    int maxAmountNodes;
    String startUrl;

    public Master(){
        amountOfThreads = 4;
        maxAmountNodes = 500;
        startUrl = "duckduckgo.com";
    }

    private void forkThreads() {
        threadList.parallelStream().forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void createThreads(int threadCount) {
        threadList = new ArrayList<>();
        for (int i=0;i<threadCount;i++){
            Worker worker = new Worker();
            worker.setWorkerName("worker "+i);
            worker.setMaxNodes(maxAmountNodes/amountOfThreads);
            worker.setQueue(blockingWorkQueue);
            threadList.add(worker);
            worker.start();
        }
    }

    private void createQueue(String startUrl) {
        blockingWorkQueue = new LinkedBlockingDeque<>();
        try {
            DataAccess.getAccess().addNode(startUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        blockingWorkQueue.add(startUrl);
        //workQueue.add(startUrl);
    }

    public void setAmountOfThreads(int amount){
        this.amountOfThreads = amount;
    }

    public void setMaxNodes(int maxAmount){
        this.maxAmountNodes = maxAmount;
    }

    public void setStartUrl (String url){
        this.startUrl=url;
    }

    public void start() {
        createQueue(startUrl);
        createThreads(amountOfThreads);
        forkThreads();
        System.out.print("finish");
    }

    public void createVizualizationJson(){
        JsonArray nodes = getNodesJson();
        JsonArray links = getLinksJson();
        JsonObject vizualisation = new JsonObject();
        vizualisation.add("nodes",nodes);
        vizualisation.add("links",links);
        writeJsonToFile(vizualisation);
    }

    private JsonArray getNodesJson() {
        JsonArray nodesArray = new JsonArray();
        DataAccess dataAccess = DataAccess.getAccess();
        try {
            ResultSet nodesTable = dataAccess.getWebcrawlerTable();
            while (nodesTable.next()){
                JsonObject node = new JsonObject();
                node.addProperty("id",nodesTable.getInt("id"));
                node.addProperty("url",nodesTable.getString("url"));
                nodesArray.add(node);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nodesArray;
    }

    private JsonArray getLinksJson() {
        JsonArray linksArray = new JsonArray();
        DataAccess dataAccess = DataAccess.getAccess();
        try {
            ResultSet linksTable = dataAccess.getLinkTable();
            while (linksTable.next()){
                JsonObject link = new JsonObject();
                link.addProperty("source",linksTable.getInt("source"));
                link.addProperty("target",linksTable.getInt("target"));
                linksArray.add(link);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return linksArray;
    }

    private void writeJsonToFile(JsonObject json) {
        try (Writer writer = new FileWriter("./src/main/resources/Webcrawler.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
