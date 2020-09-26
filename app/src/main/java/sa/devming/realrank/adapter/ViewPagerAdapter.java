package sa.devming.realrank.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import sa.devming.realrank.fragment.DaumFragment;
import sa.devming.realrank.fragment.NaverFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final int TAB_COUNT = 2;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new NaverFragment();
        } else {
            return new DaumFragment();
        }
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }
}
