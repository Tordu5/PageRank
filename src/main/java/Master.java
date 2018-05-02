import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Master {
    Queue<String> workQueue;
    BlockingQueue<String> blockingWorkQueue;
    ArrayList<Thread> threadList;
    public Master(String startUrl,int threadCount){
        createQueue(startUrl);
        createThreads(threadCount);
        forkThreads();
        System.out.print("finish");
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
            Worker worker = new Worker("Thread " + i ,blockingWorkQueue);
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
}
