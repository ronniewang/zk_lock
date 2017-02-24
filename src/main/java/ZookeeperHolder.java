import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by ronnie wang on 17/2/23.
 */
public class ZookeeperHolder {

    static ZooKeeper zooKeeper;

    public synchronized static ZooKeeper getZookeeper() throws IOException {

        if (zooKeeper == null) {
            zooKeeper = new ZooKeeper(Constant.CONNECT_STRING, Constant.SESSION_TIMEOUT, null);
            return zooKeeper;
        } else {
            return zooKeeper;
        }
    }
}
