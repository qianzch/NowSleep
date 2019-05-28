/**
 *         Go to Sleep is an open source app to manage a healthy sleep schedule
 *         Copyright (C) 2019 Cole Gerdemann
 *
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.qianzuncheng.nowsleep;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.qianzuncheng.nowsleep.ui.AboutPage;
import com.qianzuncheng.nowsleep.ui.AboutElement;

public class AboutActivity extends AppCompatActivity {

    private int clicked = 0;
    static final String EGG_KEY = "curiosity_killed_the_cat";
    private boolean egg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        egg = settings.getBoolean(EGG_KEY, false);

        AboutElement version = new AboutElement();
        version.setTitle("Version " + BuildConfig.VERSION_NAME);
        version.setOnClickListener(view -> {
            if (clicked < 10) {
                if (!egg) {
                    switch (clicked) {
                        case 1:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg1), Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg2), Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg3), Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg8), Toast.LENGTH_SHORT).show();
                            break;
                        case 9:
                            Toast.makeText(getApplicationContext(), getString(R.string.egg9), Toast.LENGTH_LONG).show();
                            settings.edit().putBoolean(EGG_KEY, true).apply();
                            break;
                    }
                } else {
                    switch (clicked) {
                        case 1:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable1), Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable2), Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable3), Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable8), Toast.LENGTH_SHORT).show();
                            break;
                        case 9:
                            Toast.makeText(getApplicationContext(), getString(R.string.eggDisable9), Toast.LENGTH_LONG).show();
                            settings.edit().putBoolean(EGG_KEY, false).apply();
                            break;
                    }
                }
                clicked++;
            }
        });


        AboutElement github = new AboutElement();
        github.setTitle(getString(R.string.aboutGitHub));
        github.setOnClickListener(view -> {
           Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/qianzch/NowSleep"));
           startActivity(browserIntent);
        });

        // TODO: Rate the app
        /*
        AboutElement playStore = new AboutElement();
        playStore.setTitle(getString(R.string.aboutRate));
        playStore.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.qianzuncheng.nowsleep"));
            startActivity(browserIntent);
        });
        */

        AboutElement email = new AboutElement();
        email.setTitle(getString(R.string.aboutContact));
        email.setOnClickListener(view -> {
            String subject = getString(R.string.aboutContactSubject);
            String mailto = "mailto:qianzch@qq.com" +
                    "?subject=" + Uri.encode(subject);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, getString(R.string.aboutContactError), Toast.LENGTH_LONG).show();
            }
        });

        // TODO: visit my website
        /*
        AboutElement website = new AboutElement();
        website.setTitle(getString(R.string.aboutWebsite));
        website.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://qianzuncheng.com/"));
            startActivity(browserIntent);
        });
        */


        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .addItem(version)
                .addItem(github)
                .addItem(email)
                .create();

        setContentView(aboutPage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

