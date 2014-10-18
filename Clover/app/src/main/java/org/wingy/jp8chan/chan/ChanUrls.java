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
package org.wingy.jp8chan.chan;

import java.util.Locale;

public class ChanUrls {
    private static String scheme;

    public static void loadScheme(boolean useHttps) {
        scheme = useHttps ? "https" : "http";
    }

    public static String getCatalogUrl(String board) {
        return scheme + "://jp.8chan.co/" + board + "/catalog.json";
    }

    public static String getPageUrl(String board, int pageNumber) {
        return scheme + "://jp.8chan.co/" + board + "/" + pageNumber + ".json";
    }

    public static String getThreadUrl(String board, int no) {
        return scheme + "://jp.8chan.co/" + board + "/res/" + no + ".json";
    }

    public static String getImageUrl(String board, String code, String extension, boolean thumb) {
        return scheme + "://jp.8chan.co/" + board + (thumb ? "/thumb/" : "/src/") + code + "." + extension;
    }

    public static String getSpoilerUrl() {
        return scheme + "://jp.8chan.co/static/spoiler.png";
    }

    // TODO: Use https://jp.8chan.co/static/flags/flags.png instead. Somehow...
    public static String getCountryFlagUrl(String countryCode) {
        return scheme + "://s.4cdn.org/image/country/" + countryCode.toLowerCase(Locale.ENGLISH) + ".gif";
    }

    // TODO: Remove if unused
    public static String getTrollCountryFlagUrl(String countryCode) {
        return getCountryFlagUrl(countryCode);
    }

    public static String getBoardsUrl() {
        return scheme + "://jp.8chan.co/boards.json";
    }

    public static String getReplyUrl() {
        return "https://jp.8chan.co/post.php";
    }

    // TODO: Implement
    public static String getDeleteUrl() {
        return getReplyUrl();
    }

    public static String getBoardUrlDesktop(String board) {
        return scheme + "://jp.8chan.co/" + board + "/";
    }

    public static String getThreadUrlDesktop(String board, int no) {
        return scheme + "://jp.8chan.co/" + board + "/res/" + no + ".html";
    }

    public static String getCatalogUrlDesktop(String board) {
        return scheme + "://jp.8chan.co/" + board + "/catalog.html";
    }

    // TODO: Implement
    public static String getReportUrl(String board, int no) {
        return ""; //return "https://sys.4chan.org/" + board + "/imgboard.php?mode=report&no=" + no;
    }
}
