/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.countdownwidget.app.appwidget;

import java.util.Calendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.format.Time;
import android.widget.RemoteViews;

import org.jraf.android.countdownwidget.R;
import org.jraf.android.util.log.wrapper.Log;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int logoWidth = context.getResources().getDimensionPixelSize(R.dimen.logoWidth);
        int logoHeight = context.getResources().getDimensionPixelSize(R.dimen.logoHeight);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.padding);
        int textSize = context.getResources().getDimensionPixelSize(R.dimen.textSize);

        int nbDays = getNbDaysToDate(2015, Calendar.DECEMBER, 18);
        //        int nbDays = getNbDaysToDate(2013, Calendar.DECEMBER, 27);
        Log.d("onUpdate nbDays=" + nbDays);

        int resId;
        switch (nbDays) {
            case -1:
                resId = R.string.countdown_minus_1;
                break;

            case 0:
                resId = R.string.countdown_zero;
                break;

            case 1:
                resId = R.string.countdown_one;
                break;

            default:
                if (nbDays < 0) {
                    resId = R.string.countdown_minus_other;
                    nbDays = -nbDays;
                } else {
                    resId = R.string.countdown_other;
                }
                break;
        }

        String text = context.getResources().getString(resId, nbDays);

        Paint paint = new Paint();
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Judson-Regular.ttf"));

        // Measure text
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);

        // Reduce text size if it's too wide
        while (textBounds.width() >= logoWidth) {
            textSize -= 5;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), textBounds);
        }

        int bitmapHeight = logoHeight + padding + textBounds.height();
        Bitmap bitmap = Bitmap.createBitmap(logoWidth, bitmapHeight, Config.ARGB_8888);

        // Draw logo
        Canvas canvas = new Canvas(bitmap);
        Bitmap logoBitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.episode_vii_logo3)).getBitmap();
        canvas.drawBitmap(logoBitmap, 0, 0, null);

        // Draw text
        paint.setShader(new LinearGradient(0, logoHeight + padding, 0, bitmapHeight, context.getResources().getColor(R.color.text0), context.getResources()
                .getColor(R.color.text1), TileMode.CLAMP));
        canvas.drawText(text, logoWidth / 2, logoHeight + padding + -textBounds.top, paint);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        remoteViews.setImageViewBitmap(R.id.imgLogo, bitmap);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    public static int getNbDaysToDate(int year, int month, int day) {
        Calendar todayCal = Calendar.getInstance();
        stripTime(todayCal);
        Time todayTime = new Time();
        todayTime.set(todayCal.getTimeInMillis());
        int todayJulianDay = Time.getJulianDay(todayCal.getTimeInMillis(), todayTime.gmtoff);

        Calendar eventCal = Calendar.getInstance();
        eventCal.set(Calendar.YEAR, year);
        eventCal.set(Calendar.MONTH, month);
        eventCal.set(Calendar.DAY_OF_MONTH, day);
        stripTime(eventCal);
        Time eventTime = new Time();
        eventTime.set(eventCal.getTimeInMillis());
        int eventJulianDay = Time.getJulianDay(eventCal.getTimeInMillis(), eventTime.gmtoff);

        return eventJulianDay - todayJulianDay;
    }

    public static void stripTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
