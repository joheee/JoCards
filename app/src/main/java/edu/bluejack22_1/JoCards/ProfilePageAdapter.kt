package edu.bluejack22_1.JoCards

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ProfilePageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 2
    }
    override fun getPageTitle(position: Int): CharSequence? {
        when(position) {
            0 -> {return Profile.profile
            }
            else -> {return Profile.setting
            }
        }
        return super.getPageTitle(position)
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> {return FirstProfileFragment()}
            else -> {return SecondProfileFragment()}
        }
    }
}