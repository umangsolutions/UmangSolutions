package com.example.samplefirebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

public class DrawView extends androidx.appcompat.widget.AppCompatImageView {

    private int noOfRectangles = 0;
    private List<Rect> listRect;
    private Bitmap finalBitmap;

    // Java Constructor
    public DrawView(Context context) {
        super(context);
    }

    // XML Constructor
    public DrawView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*  public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
*/
    /*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(noOfRectangles> 0 && listRect!=null) {

            for (int i = 0; i < noOfRectangles; i++) {

                Paint paint = new Paint();
                paint.setColor(Color.TRANSPARENT);
                paint.setStyle(Paint.Style.FILL);
                // FILL
                canvas.drawRect(listRect.get(i), paint);

                paint.setStrokeWidth(2);
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                // Border
                canvas.drawRect(listRect.get(i), paint);


            }
        }
    }

    public void createRectangles(List<Rect> listRect) {
        noOfRectangles = listRect.size();
        this.listRect = listRect;
        this.invalidate();
    }
}
