package gl.iglou.studio.retopo.MAPS;


import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import gl.iglou.studio.retopo.DATA.Topo;
import gl.iglou.studio.retopo.DATA.Trace;
import gl.iglou.studio.retopo.R;
import gl.iglou.studio.retopo.ReTopoActivity;
import gl.iglou.studio.retopo.TOPO.TopoManagerFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, MapsGUIFragment.OnMapsGUIListener,
MapsTimeLineFragment.OnTimeLineListerner{

    private final String TAG = "MapsManager";

    private final int WAIT_FOR_GOOGLE = 1000;
    private Handler mGoogleMapsAccess;
    private Runnable mGoogleMapsAccessTask;
    private int mGoogleAccessAttemp = 0;

    private MapsTimeLineFragment mTimeLineFragment;
    private MapsGUIFragment mMapsGUIFragment;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private boolean mIsGoogleMapReady = false;

    private ArrayList<OnMapsEvent> mListeners;

    private TopoManagerFragment mTopoManager;
    private Topo mCurrentTopo;
    private Trace mCurrentMilestone;
    private int mCurrentMilestoneIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mListeners = new ArrayList<>();

        mTimeLineFragment = new MapsTimeLineFragment();
        mMapsGUIFragment = new MapsGUIFragment();

        mGoogleMapsAccess = new Handler();
        mGoogleMapsAccessTask = new Runnable() {
            @Override
            public void run() {
                onGoogleAccessAttemp();
                onGoogleAccessSucess();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps_container, container, false);

        launchMapsFragment();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTopoManager = ((ReTopoActivity)getActivity()).getTopoManager();
        //mCurrentTopo = mTopoManager.getCurrentTopo();
        //mCurrentMilestone = mCurrentTopo.getMilestone(5);
        mCurrentMilestoneIndex = 5;
    }

    private void launchMapsFragment() {
        getChildFragmentManager().beginTransaction().
                replace(R.id.maps_frag_container, mMapsGUIFragment).commit();

    }

    private void launchTimeLineFragment() {
        getChildFragmentManager().beginTransaction().replace(R.id.maps_frag_container
                ,mTimeLineFragment,"TimeLineFragment").commit();
        resetGoogleAccessAttemp();
    }


    private void resetGoogleAccessAttemp() {
        mIsGoogleMapReady = false;
        mGoogleAccessAttemp = 0;
    }

    public void onPostInit() {
        Log.v(TAG,"POST INIT CALL!!");
        mMapFragment = mMapsGUIFragment.getMapFragment();
        if(mMapFragment != null) {
            mMapFragment.getMapAsync(this);
            if(mIsGoogleMapReady) {
                onGoogleAccessSucess();
            } else {
                mGoogleMapsAccess.post(mGoogleMapsAccessTask);
            }

        } else {
            Log.v(TAG, "MapFragment is null!!");
       }
    }


    private void onGoogleAccessAttemp() {
        mGoogleAccessAttemp++;
        if(mGoogleAccessAttemp > 5) {
            Log.v(TAG,"Exceeded attemp max number... Aborting.");
            mGoogleMapsAccess.removeCallbacksAndMessages(null);
        }else {
            Log.v(TAG,"Attemp maps access " + String.valueOf(mGoogleAccessAttemp));
            mGoogleMapsAccess.postDelayed(mGoogleMapsAccessTask, WAIT_FOR_GOOGLE);
        }
    }

    private void onGoogleAccessSucess() {
        if(mIsGoogleMapReady) {
            Log.v(TAG,"Sucess! Google is ready!");
            //updateCard();
            updatePosition();
            mTopoManager.enterDisplayMode();
            mGoogleMapsAccess.removeCallbacksAndMessages(null);
        }
    }

    public void setMapsListener(OnMapsEvent listener) {
        mListeners.add(listener);
    }




    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mIsGoogleMapReady = true;
        Log.v(TAG,"Google Map is ready");
    }

    public void addMarker(Location loc) {
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(loc.getLatitude(), loc.getLongitude())));
    }

    public void addMarker(Location loc, String title) {
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                .title(title));
    }

    public void updatePosition() {
        Location loc = ((ReTopoActivity) getActivity()).getLocation();
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                .title("I'm Here!"));

        updateCamera(loc);
    }

    public void updateCamera(Location loc) {
        LatLng pos = new LatLng(loc.getLatitude(),loc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
    }


    public Topo getCurrentTopo() {
        return mTopoManager.getCurrentTopo();
    }


    public void updateGUICard() {

        mMapsGUIFragment.updateCard();
    }


    private void updateCard() {
        updateGUICard();
        Location loc = mCurrentMilestone.getLocation(0);
        Log.v(TAG,"Update Card " + mCurrentMilestone.getTitle() + " at "
                + String.valueOf(loc.getLatitude()) + " " + String.valueOf(loc.getLongitude()));
        addMarker(loc);
        updateCamera(loc);
    }


    public Trace getNext() {
        int currentMilestone = getNextIndex();
        return mCurrentTopo.getMilestone(currentMilestone);
    }


    public Trace getCurrent() {
        return mCurrentTopo.getMilestone(mCurrentMilestoneIndex);
    }


    public Trace getPrev() {
        int currentMilestone = getPrevIndex();
        return mCurrentTopo.getMilestone(currentMilestone);
    }

    public int getNextIndex() {
        int index = mCurrentMilestoneIndex;
        if((index + 1) < mCurrentTopo.getMilestoneNum()) {
            index++;
        } else {
            index = 0;
        }
        return index;
    }

    public int getPrevIndex() {
        int index = mCurrentMilestoneIndex;
        if((index - 1) >= 0) {
            index--;
        } else {
            index = mCurrentTopo.getMilestoneNum() - 1;
        }
        return index;
    }

    public void onCardChange(int cardType) {
        if(cardType == mMapsGUIFragment.NEXT_CARD) {
            mCurrentMilestone = getNext();
            mCurrentMilestoneIndex = getNextIndex();
        } else {
            mCurrentMilestone = getPrev();
            mCurrentMilestoneIndex = getPrevIndex();
        }
        updateCard();
    }


    @Override
    public void onCardListClick() {
        for(OnMapsEvent listener : mListeners) {
            listener.OnPinMeClick();
        }

        launchTimeLineFragment();
    }



    @Override
    public void onMilestoneSelected(int index) {
        mCurrentMilestoneIndex = index;
        mCurrentMilestone = mCurrentTopo.getMilestone(mCurrentMilestoneIndex);
        launchMapsFragment();
    }
}
