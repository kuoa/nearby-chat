package pro.postaru.sandu.nearbychat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import pro.postaru.sandu.nearbychat.fragments.ConversationsFragment;
import pro.postaru.sandu.nearbychat.fragments.MapFragment;

public class OnlineFragmentPagerAdapter extends FragmentPagerAdapter {

    public OnlineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return MapFragment.newInstance();
        } else {
            return ConversationsFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}