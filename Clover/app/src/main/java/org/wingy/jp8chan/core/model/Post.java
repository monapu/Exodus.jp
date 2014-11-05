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
package org.wingy.jp8chan.core.model;

import android.text.SpannableString;
import android.text.TextUtils;

import org.wingy.jp8chan.ChanApplication;
import org.wingy.jp8chan.chan.ChanUrls;
import org.wingy.jp8chan.core.loader.ChanParser;
import org.wingy.jp8chan.ui.view.PostView;
import org.jsoup.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contains all data needed to represent a single post.
 */
public class Post {
    private static final Random random = new Random();

    public String board;
    public int no = -1;
    public int resto = -1;
    public boolean isOP = false;
    public String name = "";
    public CharSequence comment = "";
    public String subject = "";
    public long tim = -1;
    public String ext;
    public String serverFilename;
    public String originalFilename;
    public int replies = -1;
    public int imageWidth;
    public int imageHeight;
    public int thumbWidth;
    public int thumbHeight;
    public boolean hasImage = false;
    public String thumbnailUrl;
    public String imageUrl;
    public boolean sticky = false;
    public boolean closed = false;
    public String tripcode = "";
    public String id = "";
    public String capcode = "";
    public String country = "";
    public String countryName = "";
    public long time = -1;
    public boolean isSavedReply = false;
    public String title = "";
    public int fileSize;
    public int images = -1;
    public String rawComment;
    public String countryUrl;
    public boolean spoiler = false;

    public boolean deleted = false;

    /**
     * This post replies to the these ids
     */
    public List<Integer> repliesTo = new ArrayList<>();

    /**
     * These ids replied to this post
     */
    public List<Integer> repliesFrom = new ArrayList<>();

    public final ArrayList<PostLinkable> linkables = new ArrayList<>();
    public boolean parsedSpans = false;
    public SpannableString subjectSpan;
    public SpannableString nameSpan;
    public SpannableString tripcodeSpan;
    public SpannableString idSpan;
    public SpannableString capcodeSpan;
    public CharSequence nameTripcodeIdCapcodeSpan;

    /**
     * The PostView the Post is currently bound to.
     */
    private PostView linkableListener;

    public Post() {
    }

    public void setLinkableListener(PostView listener) {
        linkableListener = listener;
    }

    public PostView getLinkableListener() {
        return linkableListener;
    }

    /**
     * Finish up the data
     *
     * @return false if this data is invalid
     */
    public boolean finish() {
        if (board == null)
            return false;

       if (no < 0 || resto < 0 || time < 0)
            return false;

        isOP = resto == 0;

        if (ext != null && !ext.equals("swf")) {
            hasImage = true;
        }

        if (hasImage) {
            if (serverFilename == null || originalFilename == null || ext == null || imageWidth <= 0 || imageHeight <= 0)
                return false;

            imageUrl = ChanUrls.getImageUrl(board, serverFilename, ext, false);
            originalFilename = Parser.unescapeEntities(originalFilename, false);

            if (spoiler) {
                // TODO: Add custom spoiler support
                thumbnailUrl = ChanUrls.getSpoilerUrl();
            } else {
                String thumbExt = ext.equals("webm") ? "jpg" : ext;
                thumbnailUrl = ChanUrls.getImageUrl(board, serverFilename, thumbExt, true);
            }
        }

        if (!TextUtils.isEmpty(country)) {
            Board b = ChanApplication.getBoardManager().getBoardByValue(board);
            countryUrl = (b != null && b.trollFlags) ? ChanUrls.getTrollCountryFlagUrl(country)
                                                     : ChanUrls.getCountryFlagUrl(country);
        }

        ChanParser.getInstance().parse(this);

        return true;
    }
}
