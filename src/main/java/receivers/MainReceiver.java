package receivers;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

public abstract class MainReceiver<T> extends Receiver<T> {

    protected StorageLevel storageLevel;


    public MainReceiver(StorageLevel storageLevel) {
        super(storageLevel);
        this.storageLevel = storageLevel;
    }

    public MainReceiver(){this(StorageLevel.MEMORY_AND_DISK_2());}

    protected abstract void receive();

    @Override
    public void onStart(){
        Thread t = new Thread(() -> receive());
        t.start();
    }

    @Override
    public void onStop() {

    }

    @Override
    public StorageLevel storageLevel() {
        return storageLevel;
    }
}
