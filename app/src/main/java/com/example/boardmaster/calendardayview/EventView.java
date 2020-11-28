package com.example.boardmaster.calendardayview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.boardmaster.R;

public class EventView extends FrameLayout {

    protected IEvent mEvent;
    protected OnEventClickListener mEventClickListener;
    protected RelativeLayout mEventHeader;
    protected LinearLayout mEventContent;
    protected TextView mEventHeaderText1;
    protected TextView mEventHeaderText2;
    protected TextView mEventName;

    /**
     *
     * @param context
     */
    public EventView(Context context) {
        super(context);
        init(null);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public EventView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     *
     * @param attrs
     */
    protected void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_event, this, true);

        mEventHeader = (RelativeLayout) findViewById(R.id.item_event_header);
        mEventContent = (LinearLayout) findViewById(R.id.item_event_content);
        mEventName = (TextView) findViewById(R.id.item_event_name);
        mEventHeaderText1 = (TextView) findViewById(R.id.item_event_header_text1);
        mEventHeaderText2 = (TextView) findViewById(R.id.item_event_header_text2);

        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEventClickListener!= null){
                    mEventClickListener.onEventClick(EventView.this, mEvent);
                }
            }
        });

        OnClickListener eventItemClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEventClickListener != null) {
                    mEventClickListener.onEventViewClick(v, EventView.this, mEvent);
                }
            }
        };

        mEventHeaderText1.setOnClickListener(eventItemClickListener);
        mEventHeaderText2.setOnClickListener(eventItemClickListener);
        mEventContent.setOnClickListener(eventItemClickListener);
    }

    /**
     *
     * @param listener
     */
    public void setOnEventClickListener(OnEventClickListener listener){
        this.mEventClickListener = listener;
    }

    /**
     *
     * @param l
     */
    @Override
    public void setOnClickListener(final OnClickListener l) {
        throw new UnsupportedOperationException("you should use setOnEventClickListener()");
    }

    /**
     *
     * @param event
     */
    public void setEvent(IEvent event) {
        this.mEvent = event;
        mEventName.setText(String.valueOf(event.getName()));
        mEventContent.setBackgroundColor(event.getColor());
    }

    /**
     *
     * @return
     */
    public int getHeaderHeight() {
        return mEventHeader.getMeasuredHeight();
    }

    /**
     *
     * @return
     */
    public int getHeaderPadding() {
        return mEventHeader.getPaddingBottom() + mEventHeader.getPaddingTop();
    }

    /**
     *
     * @param rect
     * @param topMargin
     * @param bottomMargin
     */
    public void setPosition(Rect rect, int topMargin, int bottomMargin){
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = rect.top - getHeaderHeight() - getHeaderPadding() + topMargin
                - getResources().getDimensionPixelSize(R.dimen.cdv_extra_dimen);
        params.height = rect.height()
                + getHeaderHeight()
                + getHeaderPadding()
                + bottomMargin
                + getResources().getDimensionPixelSize(R.dimen.cdv_extra_dimen);
        params.leftMargin = rect.left;
        setLayoutParams(params);
    }

    /**
     *
     */
    public interface OnEventClickListener {
        void onEventClick(EventView view, IEvent data);
        void onEventViewClick(View view, EventView eventView, IEvent data);
    }
}