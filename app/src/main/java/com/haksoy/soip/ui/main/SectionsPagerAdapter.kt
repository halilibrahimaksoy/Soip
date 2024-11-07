package com.haksoy.soip.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.haksoy.soip.ui.conversationList.ConversationListFragment
import com.haksoy.soip.ui.discover.MapsFragment
import com.haksoy.soip.ui.profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class SectionsPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm) {


    private var fragmentList: List<Fragment> = emptyList()
    fun setFragmentList(newList: List<Fragment>) {
        fragmentList = newList
    }

    override fun getItem(position: Int): Fragment = fragmentList[position]


    override fun getCount(): Int {
        return 3
    }
}