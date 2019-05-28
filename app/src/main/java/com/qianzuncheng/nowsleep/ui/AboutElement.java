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

package com.qianzuncheng.nowsleep.ui;

import android.content.Intent;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

/**
 * AboutElement class represents an about item in the about page.
 * Use {@link AboutPage#addItem(AboutElement)} to add your
 * custom items to the AboutPage. This class can be constructed in a builder pattern type fashion.
 */
public class AboutElement {

    private String title;
    private Integer iconDrawable;
    private Integer colorDay;
    private Integer colorNight;
    private String value;
    private Intent intent;
    private Integer gravity;
    private Boolean autoIconColor = true;

    private View.OnClickListener onClickListener;

    public AboutElement() {

    }

    public AboutElement(String title, Integer iconDrawable) {
        this.title = title;
        this.iconDrawable = iconDrawable;
    }

    /**
     * Get the onClickListener for this AboutElement
     *
     * @return
     * @see android.view.View.OnClickListener
     */
    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    /**
     * Set the onClickListener for this AboutElement. It will be invoked when this particular element
     * is clicked on the AboutPage. This method has higher priority than
     * {@link AboutElement#setIntent(android.content.Intent)} when both methods are used
     *
     * @param onClickListener
     * @return this AboutElement instance for builder pattern support
     * @see android.view.View.OnClickListener
     */
    public AboutElement setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    /**
     * Get the gravity of the content of this AboutElement
     *
     * @return See {@link android.view.Gravity}
     */
    public Integer getGravity() {
        return gravity;
    }

    /**
     * Set the Gravity of the content for this AboutElement
     *
     * @param gravity See {@link android.view.Gravity}
     * @return this AboutElement instance for builder pattern support
     */
    public AboutElement setGravity(Integer gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * @return the title for this AboutElement
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * Set the title for this AboutElement
     *
     * @param title the string value to set
     * @return this AboutElement instance for builder pattern support
     */
    public AboutElement setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the icon drawable for this AboutElement that appears to the left of the title or to the
     * right of the title in RTL layout mode.
     *
     * @return the icon drawable resource of this AboutElement
     */
    @DrawableRes
    @Nullable
    public Integer getIconDrawable() {
        return iconDrawable;
    }

    /**
     * Set the icon drawable for this AboutElement that appears to the left of the title or to the
     * right of the title in RTL layout mode.
     *
     * @param iconDrawable the icon drawable resource to set
     * @return this AboutElement instance for builder pattern support
     */
    public AboutElement setIconDrawable(@DrawableRes Integer iconDrawable) {
        this.iconDrawable = iconDrawable;
        return this;
    }

    /**
     * @return the color resource identifier for this Elements icon
     */
    @ColorRes
    //@Nullable
    public Integer getIconTint() {
        return colorDay;
    }

    /**
     * Set the color resource identifier for this Elements icon
     *
     * @param color the color resource identifier to use for this AboutElement
     * @return this AboutElement instance for builder pattern support
     */
    public AboutElement setIconTint(@ColorRes Integer color) {
        this.colorDay = color;
        return this;
    }

    /**
     * Get the color resource identifier for this Elements icon when in night mode
     *
     * @return
     //* @see AppCompatDelegate#setDefaultNightMode(int)
     */
    @ColorRes
    public Integer getIconNightTint() {
        return colorNight;
    }

    /**
     * Set the icon tint to be used for this Elements icon when in night mode. If no color
     * is specified the accent color of the current theme will be used in night mode.
     *
     * @param colorNight
     * @return
     */
    public AboutElement setIconNightTint(@ColorRes Integer colorNight) {
        this.colorNight = colorNight;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AboutElement setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the intent to be used for when this AboutElement
     *
     * @return
     * @see AboutElement#setIntent(android.content.Intent)
     */
    public Intent getIntent() {
        return intent;
    }

    /**
     * Set the intent to pass to the
     * {@link android.content.Context#startActivity(android.content.Intent)} method when this item
     * is clicked. This method has lower priority than
     * {@link AboutElement#setOnClickListener(android.view.View.OnClickListener)}
     * when both are used.
     *
     * @param intent the intent to be used
     * @return this AboutElement instance for builder pattern support
     * @see android.content.Intent
     */
    public AboutElement setIntent(Intent intent) {
        this.intent = intent;
        return this;
    }

    /**
     * @return the AutoIcon
     */
    public Boolean getAutoApplyIconTint() {
        return autoIconColor;
    }

    /**
     * Automatically apply tint to this Elements icon.
     *
     * @param autoIconColor
     * @return this AboutElement instance for builder pattern support
     */
    public AboutElement setAutoApplyIconTint(Boolean autoIconColor) {
        this.autoIconColor = autoIconColor;
        return this;
    }
}