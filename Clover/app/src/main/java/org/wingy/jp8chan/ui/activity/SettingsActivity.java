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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.wingy.jp8chan.R;
import org.wingy.jp8chan.ui.fragment.SettingsFragment;
import org.wingy.jp8chan.utils.ThemeHelper;

public class SettingsActivity extends Activity {
    private static boolean doingThemeRestart = false;
    private static ThemeHelper.Theme lastTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!doingThemeRestart) {
            lastTheme = ThemeHelper.getInstance().getTheme();
        }

        ThemeHelper.setTheme(this);

        SettingsFragment frag = new SettingsFragment();
        frag.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
    }

    public void restart(Intent intent) {
        doingThemeRestart = true;
        startActivity(intent);
        finish();
        doingThemeRestart = false;
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ThemeHelper.getInstance().getTheme() != lastTheme) {
            lastTheme = ThemeHelper.getInstance().getTheme();

            BaseActivity.doRestartOnResume = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.action_settings_advanced) {
            startActivity(new Intent(this, AdvancedSettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
