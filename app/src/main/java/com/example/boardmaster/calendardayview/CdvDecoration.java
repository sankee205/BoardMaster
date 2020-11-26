package com.example.boardmaster.calendardayview;

import android.graphics.Rect;

public interface CdvDecoration {

    EventView getEventView(IEvent event, Rect eventBound, int hourHeight, int seperateHeight);

    DayView getDayView(int hour);
}