/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 * Copyright (C) 2014  wingy
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
package org.wingy.jp8chan.core.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.wingy.jp8chan.ChanApplication;
import org.wingy.jp8chan.R;
import org.wingy.jp8chan.chan.ChanUrls;
import org.wingy.jp8chan.core.ChanPreferences;
import org.wingy.jp8chan.core.loader.Loader;
import org.wingy.jp8chan.core.loader.LoaderPool;
import org.wingy.jp8chan.core.manager.ReplyManager.DeleteListener;
import org.wingy.jp8chan.core.manager.ReplyManager.DeleteResponse;
import org.wingy.jp8chan.core.model.Loadable;
import org.wingy.jp8chan.core.model.Pin;
import org.wingy.jp8chan.core.model.Post;
import org.wingy.jp8chan.core.model.PostLinkable;
import org.wingy.jp8chan.core.model.SavedReply;
import org.wingy.jp8chan.ui.activity.ReplyActivity;
import org.wingy.jp8chan.ui.fragment.PostRepliesFragment;
import org.wingy.jp8chan.ui.fragment.ReplyFragment;
import org.wingy.jp8chan.utils.Logger;
import org.wingy.jp8chan.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * All PostView's need to have this referenced. This manages some things like
 * pages, starting and stopping of loading, handling linkables, replies popups
 * etc. onDestroy, onStart and onStop must be called from the activity/fragment
 */
public class ThreadManager implements Loader.LoaderListener {
    public static enum ViewMode {
        LIST, GRID
    }

    private static final String TAG = "ThreadManager";

    private final Activity activity;
    private final ThreadManagerListener threadManagerListener;
    private final List<RepliesPopup> popupQueue = new ArrayList<>();
    private PostRepliesFragment currentPopupFragment;
    private int highlightedPost = -1;
    private int lastPost = -1;
    private String highlightedId = null;

    private Loader loader;

    public ThreadManager(Activity activity, final ThreadManagerListener listener) {
        this.activity = activity;
        threadManagerListener = listener;
    }

    public void onDestroy() {
        unbindLoader();
    }

    public void onStart() {
        if (loader != null) {
            if (shouldWatch()) {
                loader.setAutoLoadMore(true);
                loader.requestMoreDataAndResetTimer();
            }
        }
    }

    public void onStop() {
        if (loader != null) {
            loader.setAutoLoadMore(false);
        }
    }

    public void bindLoader(Loadable loadable) {
        if (loader != null) {
            unbindLoader();
        }

        loader = LoaderPool.getInstance().obtain(loadable, this);
        if (shouldWatch()) {
            loader.setAutoLoadMore(true);
        }
    }

    public void unbindLoader() {
        if (loader != null) {
            loader.setAutoLoadMore(false);
            LoaderPool.getInstance().release(loader, this);
            loader = null;
        } else {
            Logger.e(TAG, "Loader already unbinded");
        }

        highlightedPost = -1;
        lastPost = -1;
        highlightedId = null;
    }

    public void bottomPostViewed() {
        if (loader != null && loader.getLoadable().isThreadMode()) {
            List<Post> posts = loader.getCachedPosts();
            if (posts.size() > 0) {
                loader.getLoadable().lastViewed = posts.get(posts.size() - 1).no;
            }
        }

        Pin pin = ChanApplication.getWatchManager().findPinByLoadable(loader.getLoadable());
        if (pin != null) {
            pin.onBottomPostViewed();
            ChanApplication.getWatchManager().onPinsChanged();
        }
    }

    public boolean shouldWatch() {
        if (!loader.getLoadable().isThreadMode()) {
            return false;
        } else if (!ChanPreferences.getThreadAutoRefresh()) {
            return false;
        } else if (loader.getCachedPosts().size() > 0 && loader.getCachedPosts().get(0).closed) {
            return false;
        } else {
            return true;
        }
    }

    public void requestData() {
        if (loader != null) {
            loader.requestData();
        } else {
            Logger.e(TAG, "Loader null in requestData");
        }
    }

    /**
     * Called by postadapter and threadwatchcounterview.onclick
     */
    public void requestNextData() {
        if (loader != null) {
            loader.requestMoreData();
        } else {
            Logger.e(TAG, "Loader null in requestData");
        }
    }

    @Override
    public void onError(VolleyError error) {
        threadManagerListener.onThreadLoadError(error);
    }

    @Override
    public void onData(List<Post> result, boolean append) {
        if (!shouldWatch()) {
            loader.setAutoLoadMore(false);
        }

        if (result.size() > 0) {
            lastPost = result.get(result.size() - 1).no;
        }

        threadManagerListener.onThreadLoaded(result, append);
    }

    public boolean hasLoader() {
        return loader != null;
    }

    public Post findPostById(int id) {
        if (loader == null)
            return null;
        return loader.findPostById(id);
    }

    public Loadable getLoadable() {
        if (loader == null)
            return null;
        return loader.getLoadable();
    }

    public Loader getLoader() {
        return loader;
    }

    public void onThumbnailClicked(Post post) {
        threadManagerListener.onThumbnailClicked(post);
    }

    public void onPostClicked(Post post) {
        if (loader != null) {
            threadManagerListener.onPostClicked(post);
        }
    }

    public void showPostOptions(final Post post, PopupMenu popupMenu) {
        Menu menu = popupMenu.getMenu();

        if (loader.getLoadable().isBoardMode() || loader.getLoadable().isCatalogMode()) {
            menu.add(Menu.NONE, 9, Menu.NONE, activity.getString(R.string.action_pin));
        }

        if (loader.getLoadable().isThreadMode()) {
            menu.add(Menu.NONE, 10, Menu.NONE, activity.getString(R.string.post_quick_reply));
        }

        String[] baseOptions = activity.getResources().getStringArray(R.array.post_options);
        for (int i = 0; i < baseOptions.length; i++) {
            menu.add(Menu.NONE, i, Menu.NONE, baseOptions[i]);
        }

        if (!TextUtils.isEmpty(post.id)) {
            menu.add(Menu.NONE, 6, Menu.NONE, activity.getString(R.string.post_highlight_id));
        }

        // Only add the delete option when the post is a saved reply
        if (ChanApplication.getDatabaseManager().isSavedReply(post.board, post.no)) {
            menu.add(Menu.NONE, 7, Menu.NONE, activity.getString(R.string.delete));
        }

        if (ChanPreferences.getDeveloper()) {
            menu.add(Menu.NONE, 8, Menu.NONE, "Make this a saved reply");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                switch (item.getItemId()) {
                    case 10: // Quick reply
                        openReply(false);
                        // Pass through
                    case 0: // Quote
                        ChanApplication.getReplyManager().quote(post.no);
                        break;
                    case 1: // Quote inline
                        ChanApplication.getReplyManager().quoteInline(post.no, post.comment.toString());
                        break;
                    case 2: // Info
                        showPostInfo(post);
                        break;
                    case 3: // Show clickables
                        showPostLinkables(post);
                        break;
                    case 4: // Copy text
                        copyToClipboard(post.comment.toString());
                        break;
                    case 5: // Report
                        Utils.openLink(activity, ChanUrls.getReportUrl(post.board, post.no));
                        break;
                    case 6: // Id
                        highlightedId = post.id;
                        threadManagerListener.onRefreshView();
                        break;
                    case 7: // Delete
                        deletePost(post);
                        break;
                    case 8: // Save reply
                        ChanApplication.getDatabaseManager().saveReply(new SavedReply(post.board, post.no, "foo"));
                        break;
                    case 9: // Pin
                        ChanApplication.getWatchManager().addPin(post);
                        break;
                }
                return false;
            }
        });
    }

    public void openReply(boolean startInActivity) {
        if (loader == null || loader.isLoading())
            return;
        if (loader.getOP() != null && loader.getOP().closed) {
            Toast.makeText(activity, R.string.reply_locked_thread, Toast.LENGTH_SHORT).show();
            return;
        }
        if (startInActivity) {
            ReplyActivity.setLoadable(loader.getLoadable());
            Intent i = new Intent(activity, ReplyActivity.class);
            activity.startActivity(i);
        } else {
            ReplyFragment reply = ReplyFragment.newInstance(loader.getLoadable(), true);
            reply.show(activity.getFragmentManager(), "replyDialog");
        }
    }

    public void onPostLinkableClicked(PostLinkable linkable) {
        handleLinkableSelected(linkable);
    }

    public void scrollToPost(int post) {
        threadManagerListener.onScrollTo(post);
    }

    public void highlightPost(int post) {
        highlightedPost = post;
    }

    public boolean isPostHightlighted(Post post) {
        return (highlightedPost >= 0 && post.no == highlightedPost) || (highlightedId != null && post.id.equals(highlightedId));
    }

    public boolean isPostLastSeen(Post post) {
        return post.no == loader.getLoadable().lastViewed && post.no != lastPost;
    }

    private void copyToClipboard(String comment) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Post text", comment);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(activity, R.string.post_text_copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void showPostInfo(Post post) {
        String text = "";

        if (post.hasImage) {
            text += "File: " + post.originalFilename + "." + post.ext + " \nDimensions: " + post.imageWidth + "x"
                    + post.imageHeight + "\nSize: " + Utils.getReadableFileSize(post.fileSize, false) + "\n\n";
        }

        text += "Time: " + post.time; // TODO: Humanize the time

        if (!TextUtils.isEmpty(post.id)) {
            text += "\nId: " + post.id;
        }

        if (!TextUtils.isEmpty(post.tripcode)) {
            text += "\nTripcode: " + post.tripcode;
        }

        if (!TextUtils.isEmpty(post.countryName)) {
            text += "\nCountry: " + post.countryName;
        }

        if (!TextUtils.isEmpty(post.capcode)) {
            text += "\nCapcode: " + post.capcode;
        }

        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle(R.string.post_info).setMessage(text)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();

        dialog.show();
    }

    /**
     * Show a list of things that can be clicked in a list to the user.
     *
     * @param post The post that was clicked.
     */
    public void showPostLinkables(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final ArrayList<PostLinkable> linkables = post.linkables;

        if (linkables.size() > 0) {
            String[] keys = new String[linkables.size()];
            for (int i = 0; i < linkables.size(); i++) {
                keys[i] = linkables.get(i).key;
            }

            builder.setItems(keys, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handleLinkableSelected(linkables.get(which));
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void showPostReplies(Post post) {
        RepliesPopup l = new RepliesPopup();
        List<Post> p = new ArrayList<>();
        for (int no : post.repliesFrom) {
            Post r = findPostById(no);
            if (r != null) {
                p.add(r);
            }
        }
        l.posts = p;
        l.forNo = post.no;
        if (p.size() > 0) {
            showPostsRepliesFragment(l);
        }
    }

    public ThreadManager.ViewMode getViewMode() {
        return threadManagerListener.getViewMode();
    }

    /**
     * Handle when a linkable has been clicked.
     *
     * @param linkable the selected linkable.
     */
    private void handleLinkableSelected(final PostLinkable linkable) {
        if (linkable.type == PostLinkable.Type.QUOTE) {
            Post post = findPostById((Integer) linkable.value);
            if (post != null) {
                RepliesPopup l = new RepliesPopup();
                l.forNo = (Integer) linkable.value;
                l.posts.add(post);
                showPostsRepliesFragment(l);
            }
        } else if (linkable.type == PostLinkable.Type.LINK) {
            if (ChanPreferences.getOpenLinkConfirmation()) {
                new AlertDialog.Builder(activity)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Utils.openLink(activity, (String) linkable.value);
                            }
                        })
                        .setTitle(R.string.open_link_confirmation)
                        .setMessage((String) linkable.value)
                        .show();
            } else {
                Utils.openLink(activity, (String) linkable.value);
            }
        } else if (linkable.type == PostLinkable.Type.THREAD) {
            final PostLinkable.ThreadLink link = (PostLinkable.ThreadLink) linkable.value;
            final Loadable thread = new Loadable(link.board, link.threadId);

            new AlertDialog.Builder(activity)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            threadManagerListener.onOpenThread(thread, link.postId);
                        }
                    })
                    .setTitle(R.string.open_thread_confirmation)
                    .setMessage("/" + thread.board + "/" + thread.no)
                    .show();
        }
    }

    private void showPostsRepliesFragment(RepliesPopup repliesPopup) {
        // Post popups are now queued up, more than 32 popups on top of each
        // other makes the system crash!
        popupQueue.add(repliesPopup);

        if (currentPopupFragment != null) {
            currentPopupFragment.dismissNoCallback();
        }

        PostRepliesFragment popup = PostRepliesFragment.newInstance(repliesPopup, this);

        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        ft.add(popup, "postPopup");
        ft.commitAllowingStateLoss();

        currentPopupFragment = popup;
    }

    public void onPostRepliesPop() {
        if (popupQueue.size() == 0)
            return;

        popupQueue.remove(popupQueue.size() - 1);

        if (popupQueue.size() > 0) {
            PostRepliesFragment popup = PostRepliesFragment.newInstance(popupQueue.get(popupQueue.size() - 1), this);

            FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
            ft.add(popup, "postPopup");
            ft.commit();

            currentPopupFragment = popup;
        } else {
            currentPopupFragment = null;
        }
    }

    public void closeAllPostFragments() {
        popupQueue.clear();
        currentPopupFragment = null;
    }

    public boolean arePostRepliesOpen() {
        return popupQueue.size() > 0;
    }

    private void deletePost(final Post post) {
        final CheckBox checkBox = new CheckBox(activity);
        checkBox.setText(R.string.delete_image_only);

        LinearLayout wrapper = new LinearLayout(activity);
        wrapper.addView(checkBox);
        int padding = Utils.dp(8f);
        wrapper.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(activity).setTitle(R.string.delete_confirm).setView(wrapper)
                .setPositiveButton(R.string.delete, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDeletePost(post, checkBox.isChecked());
                    }
                }).setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
    }

    private void doDeletePost(Post post, boolean onlyImageDelete) {
        SavedReply reply = ChanApplication.getDatabaseManager().getSavedReply(post.board, post.no);
        if (reply == null) {
            /*
             * reply = new SavedReply(); reply.board = "g"; reply.no = 1234;
             * reply.password = "boom";
             */
            return;
        }

        final ProgressDialog dialog = ProgressDialog.show(activity, null, activity.getString(R.string.delete_wait));

        ChanApplication.getReplyManager().sendDelete(reply, onlyImageDelete, new DeleteListener() {
            @Override
            public void onResponse(DeleteResponse response) {
                dialog.dismiss();

                if (response.isNetworkError || response.isUserError) {
                    int resId;

                    if (response.isTooSoonError) {
                        resId = R.string.delete_too_soon;
                    } else if (response.isInvalidPassword) {
                        resId = R.string.delete_password_incorrect;
                    } else if (response.isTooOldError) {
                        resId = R.string.delete_too_old;
                    } else {
                        resId = R.string.delete_fail;
                    }

                    Toast.makeText(activity, resId, Toast.LENGTH_LONG).show();
                } else if (response.isSuccessful) {
                    Toast.makeText(activity, R.string.delete_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, R.string.delete_fail, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public interface ThreadManagerListener {
        public void onThreadLoaded(List<Post> result, boolean append);

        public void onThreadLoadError(VolleyError error);

        public void onPostClicked(Post post);

        public void onThumbnailClicked(Post post);

        public void onScrollTo(int post);

        public void onRefreshView();

        public void onOpenThread(Loadable thread, int highlightedPost);

        public ThreadManager.ViewMode getViewMode();
    }

    public static class RepliesPopup {
        public List<Post> posts = new ArrayList<>();
        public int listViewIndex;
        public int listViewTop;
        public int forNo = -1;
    }
}
