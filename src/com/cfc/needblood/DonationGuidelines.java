package com.cfc.needblood;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class DonationGuidelines extends Activity {
        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.donation_guidelines);

                MyPagerAdapter adapter = new MyPagerAdapter();
                ViewPager myPager = (ViewPager) findViewById(R.id.myfivepanelpager);
                myPager.setAdapter(adapter);
                myPager.setCurrentItem(0);
        }
        
        public void farLeftButtonClick(View v)
        {
                Toast.makeText(this, "Far Left Button Clicked", Toast.LENGTH_SHORT).show(); 
        }

        public void farRightButtonClick(View v)
        {
                Toast.makeText(this, "Far Right Elephant Button Clicked", Toast.LENGTH_SHORT).show(); 
        }

        private class MyPagerAdapter extends PagerAdapter {

                public int getCount() {
                        return 11;
                }

                public Object instantiateItem(View collection, int position) {

                        LayoutInflater inflater = (LayoutInflater) collection.getContext()
                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        int resId = 0;
                        switch (position) {
                        case 0:
                            resId = R.layout.guideline_pre_1;
                            break;
                        case 1:
                            resId = R.layout.guideline_pre_2;
                            break;
                        case 2:
                            resId = R.layout.guideline_pre_3;
                            break;
                        case 3:
                            resId = R.layout.guideline_post_1;
                            break;
                        case 4:
                            resId = R.layout.guideline_post_2;
                            break;
	                    case 5:
                            resId = R.layout.guideline_post_3;
                            break;
	                    case 6:
	                        resId = R.layout.guideline_post_4;
	                        break;
	                    case 7:
                            resId = R.layout.guideline_post_5;
                            break;
                        case 8:
                            resId = R.layout.guideline_post_6;
                            break;
	                    case 9:
                            resId = R.layout.guideline_post_7;
                            break;
	                    case 10:
	                        resId = R.layout.guideline_post_8;
	                        break;
                        }

                        View view = inflater.inflate(resId, null);

                        ((ViewPager) collection).addView(view, 0);

                        return view;
                }

                @Override
                public void destroyItem(View arg0, int arg1, Object arg2) {
                        ((ViewPager) arg0).removeView((View) arg2);

                }

                @Override
                public void finishUpdate(View arg0) {

                }

                @Override
                public boolean isViewFromObject(View arg0, Object arg1) {
                        return arg0 == ((View) arg1);
                }

                @Override
                public void restoreState(Parcelable arg0, ClassLoader arg1) {

                }

                @Override
                public Parcelable saveState() {
                        return null;
                }

                @Override
                public void startUpdate(View arg0) {

                }

        }

}