package pro.postaru.sandu.nearbychat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import pro.postaru.sandu.nearbychat.fragments.ConversationsFragment;
import pro.postaru.sandu.nearbychat.fragments.MapViewFragment;
import pro.postaru.sandu.nearbychat.fragments.UserListFragment;

public class OnlineFragmentPagerAdapter extends FragmentPagerAdapter {

    public OnlineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserListFragment.newInstance(1);

            case 1:
                return MapViewFragment.newInstance();
            case 2:
                return ConversationsFragment.newInstance();
            default:
                return MapViewFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}