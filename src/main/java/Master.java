import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Master {
    Queue<String> workQueue;
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
            Worker worker = new Worker("Thread " + i ,workQueue);
            threadList.add(worker);
            worker.start();
        }
    }

    private void createQueue(String startUrl) {
        workQueue = new LinkedList<>();
        try {
            DataAccess.getAccess().addNode(startUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        workQueue.add(startUrl);
    }
}
