package io.agora.chatdemo.general.livedatas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * Used when setting up and listening to a single data source: LiveData
 * It is convenient to automatically cancel the listening of the previous data source when you need to switch data sources
 *
 * @param <T> Type of data source to monitor
 */
public class SingleSourceLiveData<T> extends MutableLiveData<T> {
    private LiveData<T> lastSource;
    private T lastData;
    private final Observer<T> observer = new Observer<T>() {
        @Override
        public void onChanged(T t) {
            if (t != null && t == lastData) {
                return;
            }

            lastData = t;
            setValue(t);
        }
    };

    /**
     * Set the data source, and unlisten the data source when it has been set
     *
     * @param source
     */
    public void setSource(LiveData<T> source) {
        if (lastSource == source) {
            return;
        }

        if (lastSource != null) {
            lastSource.removeObserver(observer);
        }
        lastSource = source;

        if (hasActiveObservers()) {
            lastSource.observeForever(observer);
        }
    }

    @Override
    protected void onActive() {
        super.onActive();

        if (lastSource != null) {
            lastSource.observeForever(observer);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        if (lastSource != null) {
            lastSource.removeObserver(observer);
        }
    }
}
