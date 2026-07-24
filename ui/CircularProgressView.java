package com.example.todolist.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.todolist.R;

/**
 * View tự vẽ (Canvas) 1 vòng tròn tiến độ đơn giản - không cần thêm thư viện ngoài.
 * Constructor (Context, AttributeSet) là bắt buộc để layout XML có thể inflate được View này.
 */
public class CircularProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private float progressPercent = 0f;

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float strokeWidthPx = 14f * getResources().getDisplayMetrics().density;

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.progress_track));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthPx);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(ContextCompat.getColor(context, R.color.primary));

        textPaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(28f * getResources().getDisplayMetrics().scaledDensity);
        textPaint.setFakeBoldText(true);
    }

    public void setProgressPercent(float percent) {
        this.progressPercent = Math.max(0f, Math.min(100f, percent));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = trackPaint.getStrokeWidth() / 2f + 4f;
        arcBounds.set(padding, padding, getWidth() - padding, getHeight() - padding);

        canvas.drawArc(arcBounds, 0, 360, false, trackPaint);
        canvas.drawArc(arcBounds, -90, 360f * (progressPercent / 100f), false, progressPaint);

        String label = Math.round(progressPercent) + "%";
        float textY = getHeight() / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(label, getWidth() / 2f, textY, textPaint);
    }
}
