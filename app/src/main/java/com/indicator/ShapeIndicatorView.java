package com.indicator;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


/**
 * Created by yjwfn on 15-12-16.
 */
public class ShapeIndicatorView extends View  implements TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener{

    private Paint       mShapePaint;
    private Path        mShapePath;
    private int         mShapeHorizontalSpace;
    private int         mShapeColor = Color.GREEN;

    private TabLayout   mTabLayout;
    private ViewPager   mViewPager;



    public ShapeIndicatorView(Context context) {
        this(context, null);
    }

    public ShapeIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context, attrs, defStyleAttr, 0);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShapeIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews(context, attrs, defStyleAttr, defStyleRes);
    }

    private void    initViews(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ShapeIndicatorView ,defStyleRes, 0);
        mShapeHorizontalSpace = array.getInteger(R.styleable.ShapeIndicatorView_horizontalSpace, 15);
        mShapeColor = array.getColor(R.styleable.ShapeIndicatorView_fullColor, Color.GREEN);
        int radius = array.getInteger(R.styleable.ShapeIndicatorView_radius, 50);
        array.recycle();

        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(true);
        mShapePaint.setDither(true);
        mShapePaint.setColor(mShapeColor);
        mShapePaint.setStyle(Paint.Style.FILL);
        mShapePaint.setPathEffect(new CornerPathEffect(radius));
        mShapePaint.setStrokeCap(Paint.Cap.ROUND);

    }


    public void setupWithTabLayout(final TabLayout tableLayout){
        mTabLayout = tableLayout;



        tableLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT);
        tableLayout.setOnTabSelectedListener(this);



        tableLayout.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (mTabLayout.getScrollX() != getScrollX())
                    scrollTo(mTabLayout.getScrollX(), mTabLayout.getScrollY());
            }
        });

        ViewCompat.setElevation(this, ViewCompat.getElevation(mTabLayout));
        tableLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mTabLayout.getTabCount() > 0)
                    onTabSelected(mTabLayout.getTabAt(0));

            }
        });

        //清除Tab background
        for(int tab = 0; tab < tableLayout.getTabCount() ; tab++){
            View tabView = getTabViewByPosition(tab);
            tabView.setBackgroundResource(0);
        }
    }

    public void setupWithViewPager(ViewPager viewPager){
        mViewPager = viewPager;
        viewPager.addOnPageChangeListener(this);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawPath(canvas);
    }

    private void drawPath(Canvas canvas) {
        if(mShapePath == null || mShapePath.isEmpty())
            return;

        int savePos = canvas.save();
        canvas.drawPath(mShapePath, mShapePaint);
        canvas.restoreToCount(savePos);
    }

    private Path    generatePath(int position, float positionOffset){

        RectF range = new RectF();
        View tabView = getTabViewByPosition(position);

        if(tabView == null)
            return null;

        int left, top, right, bottom;
        left = top = right = bottom  = 0;

        if(positionOffset > 0.f && position < mTabLayout.getTabCount() - 1){
             View nextTabView = getTabViewByPosition(position + 1);
             left += (int) ( nextTabView.getLeft() * positionOffset + tabView.getLeft() * (1.f - positionOffset) );
             right += (int) ( nextTabView.getRight() * positionOffset + tabView.getRight() * (1.f - positionOffset) );


            left += mShapeHorizontalSpace;
            right -= mShapeHorizontalSpace;
            top  = tabView.getTop()  + getPaddingTop();
            bottom = tabView.getBottom()  - getPaddingBottom();
            range.set(left, top, right, bottom);
        } else {

            left = tabView.getLeft() + mShapeHorizontalSpace;
            right = tabView.getRight() - mShapeHorizontalSpace;
            top  = tabView.getTop()    +  getPaddingTop() ;
            bottom = tabView.getBottom() - getPaddingBottom();
            range.set(left, top, right, bottom);


            if(range.isEmpty())
                return mShapePath;

        }

        if(mShapePath == null)
            mShapePath = new Path();

        Rect tabsRect = getTabArea();
        tabsRect.right += range.width();
        tabsRect.left  -= range.width();

        mShapePath.reset();
        mShapePath.moveTo(tabsRect.left, tabsRect.bottom);

        mShapePath.lineTo(range.left, range.bottom);
        mShapePath.lineTo(range.left, range.top);
        mShapePath.lineTo(range.right, range.top);
        mShapePath.lineTo(range.right, range.bottom);

        mShapePath.lineTo(tabsRect.right  , tabsRect.bottom);
        mShapePath.lineTo(tabsRect.right, tabsRect.top);
        mShapePath.lineTo(tabsRect.left, tabsRect.top);
        mShapePath.close();

        return mShapePath;
    }

    private Rect  getTabArea(){

        Rect rect = null;

        if(mTabLayout != null){
            View    view = mTabLayout.getChildAt(0);
            rect = new Rect();
            view.getHitRect(rect);
        }

        return rect ;
    }

    private View    getTabViewByPosition(int position){
        if(mTabLayout != null && mTabLayout.getTabCount() > 0) {
            ViewGroup tabStrip = (ViewGroup) mTabLayout.getChildAt(0);
            return tabStrip != null ? tabStrip.getChildAt(position) : null;
        }

        return null;
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        generatePath(position, positionOffset);
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        if(mTabLayout.getSelectedTabPosition() != position)
            mTabLayout.getTabAt(position).select();

    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
      * 当已经有一个ViewPager后，当TabLayout的tab改变的时候在onTabSelected方法直接调用ViewPager的
      * setCurrentItem方法调用这个方法后会触发ViewPager的scroll事件也就是在onPageScrolled方法中调用
      * generatePath方法来更新Path，如果没有ViewPager的话直接在onTabSelected的方法中调用generatePath
      * 方法。
      **/
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if(mViewPager != null) {
            if (tab.getPosition() != mViewPager.getCurrentItem())
                mViewPager.setCurrentItem(tab.getPosition());
        }else{
            generatePath(tab.getPosition(), 0);
            invalidate();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}