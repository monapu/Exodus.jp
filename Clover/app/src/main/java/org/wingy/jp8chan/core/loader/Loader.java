/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wingy.jp8chan.core.loader;

import android.text.TextUtils;
import android.util.SparseArray;

import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import org.wingy.jp8chan.ChanApplication;
import org.wingy.jp8chan.core.model.Loadable;
import org.wingy.jp8chan.core.model.Post;
import org.wingy.jp8chan.core.net.ChanReaderRequest;
import org.wingy.jp8chan.utils.Logger;
import org.wingy.jp8chan.utils.Time;
import org.wingy.jp8chan.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Loader {
    private static final String TAG = "Loader";
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final int[] watchTimeouts = {10, 15, 20, 30, 60, 90, 120, 180, 240, 300, 600, 1800, 3600};

    private final List<LoaderListener> listeners = new ArrayList<LoaderListener>();
    private final Loadable loadable;
    private final SparseArray<Post> postsById = new SparseArray<Post>();
    private final List<Post> cachedPosts = new ArrayList<Post>();
    private Post op;

    private boolean destroyed = false;
    private boolean autoReload = false;
    private ChanReaderRequest request;

    private int currentTimeout;
    private int lastPostCount;
    private long lastLoadTime;
    private ScheduledFuture<?> pendingFuture;

    public Loader(Loadable loadable) {
        this.loadable = loadable;
    }

    /**
     * Add a LoaderListener
     *
     * @param l the listener to add
     */
    public void addListener(LoaderListener l) {
        listeners.add(l);
    }

    /**
     * Remove a LoaderListener
     *
     * @param l the listener to remove
     * @return true if there are no more listeners, false otherwise
     */
    public boolean removeListener(LoaderListener l) {
        listeners.remove(l);
        if (listeners.size() == 0) {
            clearTimer();
            destroyed = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Enable this if requestMoreData should me called automatically when the
     * timer hits 0
     */
    public void setAutoLoadMore(boolean autoReload) {
        if (this.autoReload != autoReload) {
            Logger.d(TAG, "Setting autoreload to " + autoReload);
            this.autoReload = autoReload;

            if (!autoReload) {
                clearTimer();
            }
        }
    }

    /**
     * Request more data if the time left is below 0 If auto load more is
     * disabled, this needs to be called manually. Otherwise this is called
     * automatically when the timer hits 0.
     */
    public void loadMoreIfTime() {
        if (getTimeUntilLoadMore() < 0L) {
            requestMoreData();
        }
    }

    /**
     * Request data for the first time.
     */
    public void requestData() {
        clearTimer();

        if (request != null) {
            request.cancel();
        }

        if (loadable.isBoardMode() || loadable.isCatalogMode()) {
            loadable.no = 0;
            loadable.listViewIndex = 0;
            loadable.listViewTop = 0;
        }

        currentTimeout = 0;
        cachedPosts.clear();
        op = null;

        request = getData();
    }

    /**
     * Request more data
     */
    public void requestMoreData() {
        clearTimer();

        if (loadable.isBoardMode()) {
            if (request != null) {
                // finish the last board load first
                return;
            }

            loadable.no++;

            request = getData();
        } else if (loadable.isThreadMode()) {
            if (request != null) {
                return;
            }

            request = getData();
        }
    }

    /**
     * Request more data and reset the watch timer.
     */
    public void requestMoreDataAndResetTimer() {
        currentTimeout = 0;
        requestMoreData();
    }

    /**
     * @return Returns if this loader is currently loading
     */
    public boolean isLoading() {
        return request != null;
    }

    public Post findPostById(int id) {
        return postsById.get(id);
    }

    public Loadable getLoadable() {
        return loadable;
    }

    public Post getOP() {
        return op;
    }

    /**
     * Get the time in milliseconds until another loadMore is recommended
     *
     * @return
     */
    public long getTimeUntilLoadMore() {
        if (request != null) {
            return 0L;
        } else {
            long waitTime = watchTimeouts[currentTimeout] * 1000L;
            return lastLoadTime + waitTime - Time.get();
        }
    }

    public List<Post> getCachedPosts() {
        return cachedPosts;
    }

    private void setTimer(int postCount) {
        clearTimer();

        if (postCount > lastPostCount) {
            currentTimeout = 0;
        } else {
            currentTimeout++;
            if (currentTimeout >= watchTimeouts.length) {
                currentTimeout = watchTimeouts.length - 1;
            }
        }

        if (!autoReload && currentTimeout < 4) {
            currentTimeout = 4; // At least 60 seconds in the background
        }

        lastPostCount = postCount;

        if (autoReload) {
            Runnable pendingRunnable = new Runnable() {
                @Override
                public void run() {
                    Utils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pendingFuture = null;
                            // Always reload, it's always time to reload when the timer fires
                            requestMoreData();
                        }
                    });
                }
            };

            Logger.d(TAG, "Scheduled reload in " + watchTimeouts[currentTimeout] * 1000L);
            pendingFuture = executor.schedule(pendingRunnable, watchTimeouts[currentTimeout], TimeUnit.SECONDS);
        }
    }

    private void clearTimer() {
        if (pendingFuture != null) {
            Logger.d(TAG, "Removed pending runnable");
            pendingFuture.cancel(false);
            pendingFuture = null;
        }
    }

    private ChanReaderRequest getData() {
        Logger.i(TAG, "Requested " + loadable.board + ", " + loadable.no);

        ChanReaderRequest request = ChanReaderRequest.newInstance(loadable, cachedPosts,
                new Response.Listener<List<Post>>() {
                    @Override
                    public void onResponse(List<Post> list) {
                        Loader.this.request = null;
                        onData(list);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Loader.this.request = null;
                        onError(error);
                    }
                }
        );

        ChanApplication.getVolleyRequestQueue().add(request);

        return request;
    }

    private void onData(List<Post> result) {
        if (destroyed)
            return;

        cachedPosts.clear();

        if (loadable.isThreadMode()) {
            cachedPosts.addAll(result);
        }

        postsById.clear();
        for (Post post : result) {
            postsById.append(post.no, post);
        }

        if (loadable.isThreadMode() && result.size() > 0) {
            op = result.get(0);
        }

        if (TextUtils.isEmpty(loadable.title)) {
            if (getOP() != null) {
                loadable.generateTitle(getOP());
            } else {
                loadable.title = "/" + loadable.board + "/";
            }
        }

        for (LoaderListener l : listeners) {
            l.onData(result, loadable.isBoardMode());
        }

        lastLoadTime = Time.get();

        if (loadable.isThreadMode()) {
            setTimer(result.size());
        }
    }

    private void onError(VolleyError error) {
        if (destroyed)
            return;

        cachedPosts.clear();

        Logger.e(TAG, "Error loading " + error.getMessage(), error);

        // 404 with more pages already loaded means endofline
        if ((error instanceof ServerError) && loadable.isBoardMode() && loadable.no > 0) {
            error = new EndOfLineException();
        }

        for (LoaderListener l : listeners) {
            l.onError(error);
        }

        clearTimer();
    }

    public static interface LoaderListener {
        public void onData(List<Post> result, boolean append);

        public void onError(VolleyError error);
    }
}
