package com.jwhh.jim.notekeeper.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.jwhh.jim.notekeeper.R;

/**
 * TODO: document your custom view class.
 */
public class ModuleStatusView extends View {
    private static final int EDIT_MODE_MODULE_COUNT = 7;
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;
    private float mOutlineWidth;
    private float mShapeSize;
    private float mSpacing;
    private Rect[] mModuleRectangle;
    private int mOutlineColor;
    private Paint mPaintOutline;
    private Paint mPaintFill;
    private int mFillColor;
    private float mRadius;

    //mModuleStatus hold boolean value for no of courses completed

    public boolean[] getModuleStatus() {
        return mModuleStatus;
    }

    public void setModuleStatus(boolean[] moduleStatus) {
        mModuleStatus = moduleStatus;
    }

    private boolean[] mModuleStatus;

    public ModuleStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
/** Drawing code---sizing values
     * @method init does the view initialization work`for the view
 *ANTI_ALIAS_FLAG makes edges smooth
     */
    private void init(AttributeSet attrs, int defStyle) {
        //
        // Load attribute check if the view is in use
         if(isInEditMode())
             setupEditModeValues();
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ModuleStatusView, defStyle, 0);



        a.recycle();

        //6px
        mOutlineWidth = 6f;
        //size of shapes
        mShapeSize = 144f;
        //space between each of the shapes
        mSpacing = 30f;
        //Circle radius
        mRadius = (mShapeSize-mOutlineWidth)/2;

        //List of rectangles and where they will be drawn on canvas
        setUpModuleRectangles();
        //Draw the circle after defining the rectangles
        mOutlineColor = Color.BLACK;
        mPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOutline.setStyle(Paint.Style.STROKE);
        mPaintOutline.setStrokeWidth(mOutlineWidth);
        mPaintOutline.setColor(mOutlineColor);

        //Color to fill in the circles
        mFillColor = getContext().getResources().getColor(R.color.pluralsightOrange);
        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(mFillColor);

    }

    private void setupEditModeValues() {
        //Setup when view is in use in layout designer
        boolean[] exampleModeuleValue= new boolean[EDIT_MODE_MODULE_COUNT];//array of size 7
        int middle=EDIT_MODE_MODULE_COUNT/2;
        for(int i=0;i<middle;i++)
            exampleModeuleValue[i]=true;
        setModuleStatus(exampleModeuleValue);
    }

    //Position rectangle
    private void setUpModuleRectangles() {
        mModuleRectangle = new Rect[mModuleStatus.length];
        //Loop to populate the array
        for(int moduleIndex=0;moduleIndex<mModuleRectangle.length;moduleIndex++)
        {
//            Top left corner position for each rectangle
            int x=(int) (moduleIndex* (mShapeSize+mSpacing));
            //Top edge position
            int y=0;

            /*
            Create new instance of class rect
            Position of right edge= x+shapeSize
            Position of bottom edge= y+shapeSize
             */
            mModuleRectangle[moduleIndex]=new Rect(x,y,x+ (int) mShapeSize,y+ (int) mShapeSize);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Gets called for each circle to be drawn on canvas
        for(int moduleIndex=0;moduleIndex<mModuleRectangle.length;moduleIndex++)  {
            float x=mModuleRectangle[moduleIndex].centerX();//Get x co-ordinate
            float y = mModuleRectangle[moduleIndex].centerY();//Get y co-ordinate


//Draw filled in circle
            if(mModuleStatus[moduleIndex])
               canvas.drawCircle(x, y, mRadius,mPaintFill);
            canvas.drawCircle(x, y, mRadius,mPaintOutline);
            // TODO: consider storing these as member variables to reduce
        }

    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {

    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {

    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {

    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
