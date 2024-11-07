package com.haksoy.soip.ui.userlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.haksoy.soip.data.user.User
import com.haksoy.soip.databinding.FragmentUserListBinding
import com.haksoy.soip.ui.main.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "SoIP:UserListFragment"

@AndroidEntryPoint
class UserListFragment @Inject constructor() : Fragment(), UserListAdapter.UserItemListener {
    private lateinit var binding: FragmentUserListBinding
    private var adapter = UserListAdapter(this)
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var currentItem: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("UserListFragment", "onCreateView")
        binding = FragmentUserListBinding.inflate(inflater, container, false)
        setupViewPager()
        fillList()

        return binding.root
    }

    private fun fillList() {
        adapter.setItems(sharedViewModel.selectedUserList)
        binding.userViewPager.currentItem =
            currentItem ?: sharedViewModel.getPositionFromUid()
    }

    private fun setupViewPager() {
        binding.userViewPager.adapter = adapter
        binding.userViewPager.clipToPadding = false
        binding.userViewPager.clipChildren = false
        binding.userViewPager.offscreenPageLimit = 3
        binding.userViewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        val compositeTransformer = CompositePageTransformer()
        compositeTransformer.addTransformer(MarginPageTransformer(20))
        compositeTransformer.addTransformer(ViewPager2.PageTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.75f + r * 0.25f
            page.scaleX = 0.75f + r * 0.25f
        })
        binding.userViewPager.setPageTransformer(compositeTransformer)
    }

    override fun onSelectedUser(user: User) {
        Log.i(TAG, "sharedViewModel  :  selectedUser posted new value")
        sharedViewModel.selectedUser.postValue(user)
    }

    override fun onStartConversationWithUser(user: User) {
        sharedViewModel.conversationDetailWithUser.postValue(user)
    }

    override fun onPause() {
        super.onPause()
        currentItem = binding.userViewPager.currentItem
    }
}