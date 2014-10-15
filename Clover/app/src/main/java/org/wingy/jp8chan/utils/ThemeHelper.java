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
package org.wingy.jp8chan.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;

import org.wingy.jp8chan.R;
import org.wingy.jp8chan.core.ChanPreferences;

public class ThemeHelper {
    public enum Theme {
        LIGHT("light", R.style.AppTheme, true),
        DARK("dark", R.style.AppTheme_Dark, false),
        BLACK("black", R.style.AppTheme_Dark_Black, false);

        public String name;
        public int resValue;
        public boolean isLightTheme;

        private Theme(String name, int resValue, boolean isLightTheme) {
            this.name = name;
            this.resValue = resValue;
            this.isLightTheme = isLightTheme;
        }
    }

    private static ThemeHelper instance;
    private Context context;
    private int quoteColor;
    private int highlightQuoteColor;
    private int linkColor;
    private int spoilerColor;
    private int inlineQuoteColor;
    private int codeTagSize;
    private int fontSize;

    public static ThemeHelper getInstance() {
        if (instance == null) {
            instance = new ThemeHelper();
        }

        return instance;
    }

    public static void setTheme(Activity activity) {
        activity.setTheme(ThemeHelper.getInstance().getTheme().resValue);
    }

    public ThemeHelper() {
    }

    public Theme getTheme() {
        String themeName = ChanPreferences.getTheme();

        Theme theme = null;
        switch (themeName) {
            case "light":
                theme = Theme.LIGHT;
                break;
            case "dark":
                theme = Theme.DARK;
                break;
            case "black":
                theme = Theme.BLACK;
                break;
        }

        return theme;
    }

    public Context getThemedContext() {
        return context;
    }

    public void reloadPostViewColors(Context context) {
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(null, R.styleable.PostView, R.attr.post_style, 0);
        quoteColor = ta.getColor(R.styleable.PostView_quote_color, 0);
        highlightQuoteColor = ta.getColor(R.styleable.PostView_highlight_quote_color, 0);
        linkColor = ta.getColor(R.styleable.PostView_link_color, 0);
        spoilerColor = ta.getColor(R.styleable.PostView_spoiler_color, 0);
        inlineQuoteColor = ta.getColor(R.styleable.PostView_inline_quote_color, 0);
        codeTagSize = ta.getDimensionPixelSize(R.styleable.PostView_code_tag_size, 0);
        fontSize = ChanPreferences.getFontSize();
        ta.recycle();
    }

    public int getQuoteColor() {
        return quoteColor;
    }

    public int getHighlightQuoteColor() {
        return highlightQuoteColor;
    }

    public int getLinkColor() {
        return linkColor;
    }

    public int getSpoilerColor() {
        return spoilerColor;
    }

    public int getInlineQuoteColor() {
        return inlineQuoteColor;
    }

    public int getCodeTagSize() {
        return codeTagSize;
    }

    public int getFontSize() {
        return fontSize;
    }
}
