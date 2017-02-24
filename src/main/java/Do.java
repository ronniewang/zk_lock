import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ronnie wang on 17/2/23.
 */
public class Do {


    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {

        ZooKeeper zooKeeper = ZookeeperHolder.getZookeeper();

        if (zooKeeper.exists(Constant.LOCK_PATH, false) == null) {
            zooKeeper.create(Constant.LOCK_PATH, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        zooKeeper.create(Constant.LOCK_PATH + "/read_", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        zooKeeper.create(Constant.LOCK_PATH + "/write_", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        String path = zooKeeper.create(Constant.LOCK_PATH + "/write_", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        Lock myLock = new Lock(path.replaceAll(Constant.LOCK_PATH + "/", ""));
        System.out.println("my lock is " + myLock);

        Handler handler = new Handler(myLock, () -> {
            System.out.println("do something when get lock");
        });
        handler.doTransaction();
        Thread.sleep(10000000);
    }
}

class Handler implements Watcher {

    private Lock myLock;

    InvokerWhenGetLock invokerWhenGetLock;

    public Handler(Lock myLock, InvokerWhenGetLock invokerWhenGetLock) {

        this.myLock = myLock;
        this.invokerWhenGetLock = invokerWhenGetLock;
    }

    @Override
    public void process(WatchedEvent event) {

        if (event.getType().equals(Watcher.Event.EventType.NodeDeleted)) {
            System.out.println(event);
            try {
                doTransaction();
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void doTransaction() throws KeeperException, InterruptedException, IOException {

        ZooKeeper zooKeeper = ZookeeperHolder.getZookeeper();
        Lock previousLock = getPreviousLock(myLock);
        if (previousLock != null) {
            zooKeeper.register(this);
            zooKeeper.exists(previousLock.getPath(), true);
            System.out.println("find monitor lock:" + previousLock);
        } else {
            System.out.println("I get lock");
            invokerWhenGetLock.doSomething();
        }
    }

    private static Lock getPreviousLock(Lock myLock) throws KeeperException, InterruptedException, IOException {

        List<String> children = ZookeeperHolder.getZookeeper().getChildren(Constant.LOCK_PATH, false);
        List<Lock> locks = children.stream().map(Lock::new).collect(Collectors.toList());
        Collections.sort(locks);

        System.out.println("children is " + locks);

        String myLockType = myLock.getType();
        Integer myNumber = myLock.getNumber();

        Lock monitorLock = null;
        if (Constant.WRITE.equals(myLockType)) {
            if (locks.get(0).getNumber() == myNumber) {//get write lock if myNumber is lowest number
                return monitorLock;
            }
            for (Lock lock : locks) {
                if (lock.getNumber() < myNumber) {
                    monitorLock = lock;
                } else {//equal
                    break;
                }
            }
        } else {//read lock
            for (Lock lock : locks) {
                if (lock.getNumber() < myNumber) {
                    if (Constant.WRITE.equals(lock.getType())) {
                        monitorLock = lock;
                    }
                } else {//equal
                    break;
                }
            }
        }
        return monitorLock;
    }
}
