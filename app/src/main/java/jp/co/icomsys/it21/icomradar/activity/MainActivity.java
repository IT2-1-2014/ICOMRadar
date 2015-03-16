package jp.co.icomsys.it21.icomradar.activity;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.google.android.gms.maps.GoogleMap;

import jp.co.icomsys.it21.icomradar.fragment.NavigationDrawerFragment;
import jp.co.icomsys.it21.icomradar.R;
import jp.co.icomsys.it21.icomradar.fragment.RadarMapFragment;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final int MENU_SELECT_MAP_ON = 0;
    public static final int MENU_SELECT_MAP_OFF = 1;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private boolean mapShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        RadarMapFragment fragment = (RadarMapFragment) fragmentManager.findFragmentById(R.id.container);

        if (fragment == null) {
            fragment = RadarMapFragment.newInstance(position + 1);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, RadarMapFragment.newInstance(position + 1))
                    .commit();
        }

        switch (position) {
            case 0:
                mTitle = getString(R.string.title_section0);
                fragment.setZoom(19);
                break;
            case 1:
                mTitle = getString(R.string.title_section1);
                fragment.setZoom(16);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                fragment.setZoom(12);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                fragment.setZoom(9);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                fragment.setZoom(7);
                break;
        }
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(mTitle);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!mNavigationDrawerFragment.isDrawerOpen()) {

            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.menu_main, menu);

            // 地図を表示
            MenuItem itemClear = menu.add(0, MENU_SELECT_MAP_ON, 0, "地図を表示");

            // 地図を非表示
            MenuItem itemFinish = menu.add(0, MENU_SELECT_MAP_OFF, 1, "地図を非表示");

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onPrepareOptionsMenu (Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        // メニューアイテムを取得
//        MenuItem menuMapon = (MenuItem)menu.findItem(MENU_SELECT_MAP_ON);
//        MenuItem menuMapOff = (MenuItem)menu.findItem(MENU_SELECT_MAP_OFF);
//
//        if (mapShown) {
//            // menu0を表示
//            menuMapon.setVisible(false);
//            // menu1を非表示
//            menuMapOff.setVisible(true);
//        } else {
//            // menu0を非表示
//            menuMapon.setVisible(true);
//            // menu1を表示
//            menuMapOff.setVisible(false);
//        }
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        FragmentManager fragmentManager = getFragmentManager();
        RadarMapFragment fragment = (RadarMapFragment) fragmentManager.findFragmentById(R.id.container);

        switch (item.getItemId()) {
            case MENU_SELECT_MAP_ON:

                fragment.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mapShown = true;
                return true;

            case MENU_SELECT_MAP_OFF:

                fragment.setMapType(GoogleMap.MAP_TYPE_NONE);
                mapShown = false;
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
