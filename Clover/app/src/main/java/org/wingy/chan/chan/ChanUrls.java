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
package org.wingy.chan.chan;

import java.util.Locale;

public class ChanUrls {
    private static String scheme;

    public static void loadScheme(boolean useHttps) {
        scheme = useHttps ? "https" : "http";
    }

    public static String getCatalogUrl(String board) {
        return scheme + "://8chan.co/" + board + "/catalog.json";
    }

    public static String getPageUrl(String board, int pageNumber) {
        return scheme + "://8chan.co/" + board + "/" + pageNumber + ".json";
    }

    public static String getThreadUrl(String board, int no) {
        return scheme + "://8chan.co/" + board + "/res/" + no + ".json";
    }

    // TODO: Review
    public static String getCaptchaChallengeUrl() {
        return scheme + "://www.google.com/recaptcha/api/challenge?k=6Ldp2bsSAAAAAAJ5uyx_lx34lJeEpTLVkP5k04qc";
    }

    // TODO: Review
    public static String getCaptchaImageUrl(String challenge) {
        return scheme + "://www.google.com/recaptcha/api/image?c=" + challenge;
    }

    public static String getImageUrl(String board, String code, String extension, boolean thumb) {
        return scheme + "://8chan.co/" + board + (thumb ? "/thumb/" : "/src/") + code + "." + extension;
    }

    // TODO: Review
    public static String getSpoilerUrl() {
        return scheme + "://s.4cdn.org/image/spoiler.png";
    }

    // TODO: Review
    public static String getCustomSpoilerUrl(String board, int value) {
        return scheme + "://s.4cdn.org/image/spoiler-" + board + value + ".png";
    }

    // TODO: Review
    public static String getCountryFlagUrl(String countryCode) {
        return scheme + "://s.4cdn.org/image/country/" + countryCode.toLowerCase(Locale.ENGLISH) + ".gif";
    }

    // TODO: Review
    public static String getTrollCountryFlagUrl(String countryCode) {
        return scheme + "://s.4cdn.org/image/country/troll/" + countryCode.toLowerCase(Locale.ENGLISH) + ".gif";
    }

    public static String getBoardsUrl() {
        return scheme + "://8chan.co/boards.json";
    }

    public static String getReplyUrl() {
        return "https://8chan.co/post.php";
    }

    // TODO: Review
    public static String getDeleteUrl(String board) {
        return "https://sys.4chan.org/" + board + "/imgboard.php";
    }

    public static String getBoardUrlDesktop(String board) {
        return scheme + "://8chan.co/" + board + "/";
    }

    public static String getThreadUrlDesktop(String board, int no) {
        return scheme + "://8chan.co/" + board + "/res/" + no + ".html";
    }

    public static String getCatalogUrlDesktop(String board) {
        return scheme + "://8chan.co/" + board + "/catalog.html";
    }

    // TODO: Review
    public static String getPassUrl() {
        return "https://sys.4chan.org/auth";
    }

    // TODO: Review
    public static String getReportUrl(String board, int no) {
        return "https://sys.4chan.org/" + board + "/imgboard.php?mode=report&no=" + no;
    }
}
