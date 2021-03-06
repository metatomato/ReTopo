package gl.iglou.studio.retopo;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import gl.iglou.studio.retopo.CLOUD.CloudManagerFragment;
import gl.iglou.studio.retopo.DATA.DataManagerFragment;
import gl.iglou.studio.retopo.MAPS.MapsFragment;
import gl.iglou.studio.retopo.MAPS.MapsGUIFragment;
import gl.iglou.studio.retopo.TOPO.TopoManagerFragment;
import gl.iglou.studio.retopo.TRACKS.LocationService;


public class ReTopoActivity extends ActionBarActivity {

    final private String TAG = "ReTopoActivity";

    private DisplayMetrics mDisplayMetrics;
    private float mDpHeight;
    private float mDpWidth;
    private int mDisplayWidth;
    private int mDisplayHeigh;

    final private int MAPS_FRAGMENT = 0;

    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;
    private View mContentView;
    private View mNavigationDrawer;

    private LocationService mLocationService;
    private MapsFragment mMapsManager;
    private DataManagerFragment mDataManagerFragment;
    private TopoManagerFragment mTopoManager;
    private CloudManagerFragment mCloudManager;


    String[] mFragmentLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retopo);

        mLocationService  = new LocationService(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mFragmentLabel = new String[4];
        mFragmentLabel[0] = getResources().getString(R.string.fragment_label_maps);

        //Layout Views Setup
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mContentView = findViewById(R.id.main_content);
        mNavigationDrawer = findViewById(R.id.navigation_drawer);

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);


        //Metrics Stuffs
        mDisplayMetrics = this.getResources().getDisplayMetrics();

        mDpHeight = mDisplayMetrics.heightPixels / mDisplayMetrics.density;
        mDpWidth = mDisplayMetrics.widthPixels / mDisplayMetrics.density;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDisplayWidth = size.x;
        mDisplayHeigh = size.y;

        if (mToolbar  != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                return;
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                return;
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                super.onDrawerSlide(drawerView,slideOffset);
                float moveFactor = (mNavigationDrawer.getWidth() * slideOffset);

                mContentView.setTranslationX(moveFactor);
            }
        };

        if(mDpWidth >= 600.f) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.setHomeAsUpIndicator(R.drawable.iglou_logo);
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        //Start Fragments
        FragmentManager fm = getFragmentManager();
        if(mDataManagerFragment == null) {
            mDataManagerFragment = new DataManagerFragment();
            fm.beginTransaction().add(mDataManagerFragment, "DataFrag").commit();
        }
        if(mTopoManager == null) {
            mTopoManager= new TopoManagerFragment();
            fm.beginTransaction().add(mTopoManager, "TopoFrag").commit();
        }
        if(mMapsManager == null) {
            mMapsManager = new MapsFragment();
            fm.beginTransaction().add(mMapsManager, "MapsFrag").commit();
        }
        if(mCloudManager == null) {
            Log.v("CLOUD", "starting CloudManager from retop activity");
            mCloudManager = new CloudManagerFragment();
            fm.beginTransaction().add(mCloudManager, "CloudFrag").commit();
        }

        contentViewResolver(MAPS_FRAGMENT);
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(mDpWidth >= 600.0) {
            //Disable the drawer shadow (shadow already attached to contentView)
            mNavigationDrawer.setBackgroundResource(0);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mContentView.setBackgroundDrawable(getResources().getDrawable(R.drawable.drawer_shadow_reverse_left));
                mContentView.setPadding(Math.round(5 * mDisplayMetrics.density),0,0,0);
            }
        }

        mLocationService.startService();
    }

    public void contentViewResolver(int fragmentId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment main_content_fragment;
        switch(fragmentId) {
            default:
            case MAPS_FRAGMENT :

                if(mMapsManager == null) {
                    mMapsManager = new MapsFragment();
                }
                main_content_fragment = mMapsManager;

                break;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.main_content, main_content_fragment)
                .commit();

        getSupportActionBar().setTitle(mFragmentLabel[fragmentId]);

        closeDrawer();

    }

    private void closeDrawer() {
        Handler scheduler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawers();
            }
        };
        scheduler.postDelayed( task, 150L);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }


    public int getDisplayWidth() {
        return mDisplayWidth;
    }

    public int getDisplayHeigh() {
        return mDisplayHeigh;
    }

    public float getDpWidth() {return mDpWidth;}

    public Location getLocation() { return mLocationService.getLocation(); }

    public DataManagerFragment getDataManager() {
        return mDataManagerFragment;
    }

    public MapsFragment getMapsManager() {
        return mMapsManager;
    }

    public TopoManagerFragment getTopoManager() {
        return mTopoManager;
    }

}
