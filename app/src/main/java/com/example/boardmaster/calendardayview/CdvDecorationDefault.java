package com.example.boardmaster.calendardayview;


import android.content.Context;
import android.graphics.Rect;

public class CdvDecorationDefault implements CdvDecoration {

    protected Context mContext;
    protected EventView.OnEventClickListener mEventClickListener;

    /**
     *
     * @param context
     */
    public CdvDecorationDefault(Context context) {
        this.mContext = context;
    }

    /**
     *
     * @param event
     * @param eventBound
     * @param hourHeight
     * @param separateHeight
     * @return
     */
    @Override
    public EventView getEventView(IEvent event, Rect eventBound, int hourHeight,
                                  int separateHeight) {
        EventView eventView = new EventView(mContext);
        eventView.setEvent(event);
        eventView.setPosition(eventBound, -hourHeight, hourHeight - separateHeight * 2);
        eventView.setOnEventClickListener(mEventClickListener);
        return eventView;
    }


    /**
     *
     * @param hour
     * @return
     */
    @Override
    public DayView getDayView(int hour) {
        DayView dayView = new DayView(mContext);
        dayView.setText(String.format("%1$2s:00", hour));
        return dayView;
    }

    /**
     *
     * @param listener
     */
    public void setOnEventClickListener(EventView.OnEventClickListener listener) {
        this.mEventClickListener = listener;
    }


}

