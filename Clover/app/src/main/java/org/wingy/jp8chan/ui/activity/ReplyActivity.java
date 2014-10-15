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
package org.wingy.jp8chan.ui.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import org.wingy.jp8chan.core.model.Loadable;
import org.wingy.jp8chan.ui.fragment.ReplyFragment;
import org.wingy.jp8chan.utils.Logger;
import org.wingy.jp8chan.utils.ThemeHelper;

public class ReplyActivity extends Activity {
    private static final String TAG = "ReplyActivity";

    private static Loadable loadable;

    public static void setLoadable(Loadable l) {
        loadable = l;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.setTheme(this);

        if (loadable != null && savedInstanceState == null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(android.R.id.content, ReplyFragment.newInstance(loadable, false), "reply");
            ft.commitAllowingStateLoss();

            loadable = null;
        } else if (savedInstanceState == null) {
            Logger.e(TAG, "ThreadFragment was null, exiting!");
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment f = getFragmentManager().findFragmentByTag("reply");
        if (f != null && ((ReplyFragment)f).onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
