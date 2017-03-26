package com.rks.musicx.data.loaders;

/**
 * Created by Coolalien on 9/11/2016.
 */

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/*
 * Created by Coolalien on 6/28/2016.
 */

public abstract class BaseAsyncTaskLoader<T> extends AsyncTaskLoader<T> {
    protected T mData;

    private String mFilter;

    public BaseAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(T data) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (data != null) {
                onReleaseResources(data);
            }
        }
        T oldData = mData;
        mData = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }

        // At this point we can release the resources associated with
        // 'oldData' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldData != null) {
            onReleaseResources(oldData);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(T data) {
        super.onCanceled(data);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(data);
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }
    }

    protected void onReleaseResources(T apps) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }

    public String getFilter() {
        return mFilter;
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }
}
