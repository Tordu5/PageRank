import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
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
}
