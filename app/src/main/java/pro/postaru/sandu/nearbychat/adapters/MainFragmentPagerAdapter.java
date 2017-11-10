package pro.postaru.sandu.nearbychat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import pro.postaru.sandu.nearbychat.fragments.LoginFragment;
import pro.postaru.sandu.nearbychat.fragments.RegisterFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return LoginFragment.newInstance();
        } else {
            return RegisterFragment.newInstance();
        }

    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }
}