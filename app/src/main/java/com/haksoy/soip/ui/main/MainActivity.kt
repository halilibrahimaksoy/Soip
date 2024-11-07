package com.haksoy.soip.ui.main

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.haksoy.soip.R
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.user.User
import com.haksoy.soip.databinding.ActivityMainBinding
import com.haksoy.soip.databinding.CustomtabBinding
import com.haksoy.soip.ui.conversationDetail.ConversationDetailFragment
import com.haksoy.soip.ui.conversationList.ConversationListFragment
import com.haksoy.soip.ui.discover.MapsFragment
import com.haksoy.soip.ui.profile.UserProfileFragment
import com.haksoy.soip.ui.userlist.UserListFragment
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.Resource
import com.haksoy.soip.utlis.Utils
import com.haksoy.soip.utlis.observeOnce
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


private const val TAG = "SoIP:MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var firebaseDao: FirebaseDao

    private val viewModel: MainActivityViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by viewModels()

    @Inject
    lateinit var conversationDetailFragment: ConversationDetailFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        viewModel.isUserDataExist.observeOnce {
            if (it.status == Resource.Status.SUCCESS) {
                if (!it.data!!) {
                    showUserUpdateFragment()
                } else
                    prepareUi()
            }
        }

        sharedViewModel.selectedUserUid.observe(this, Observer {
            Log.i(TAG, "sharedViewModel  :  selectedUserUid observed")

            showUserList()
            Log.i(
                TAG,
                "supportFragmentManager.backStackEntryCount : ${supportFragmentManager.backStackEntryCount}"
            )
        })
        sharedViewModel.selectedUser.observe(this, Observer {
            Log.i(TAG, "sharedViewModel  :  selectedUser observed")

            showUserProfileFragment(it)
            Log.i(
                TAG,
                "supportFragmentManager.backStackEntryCount : ${supportFragmentManager.backStackEntryCount}"
            )
        })
        intent.getStringExtra(Constants.ConversationDetailFragmentSelectedUser)?.let {
            viewModel.getUser(it).observeOnce {
                sharedViewModel.conversationDetailWithUser.postValue(it.data)
            }
        }

        sharedViewModel.conversationDetailWithUser.observe(this, Observer { user ->
            Log.i(TAG, "sharedViewModel  :  conversationDetailWithUser observed")
            showConversationDetailFragment(user)
            viewModel.addUser(user)
            Log.i(
                TAG,
                "supportFragmentManager.backStackEntryCount : ${supportFragmentManager.backStackEntryCount}"
            )
        })
        sharedViewModel.userProfileEditMode.observe(this, Observer {
            Log.i(TAG, "sharedViewModel  :  userProfileEditMode($it) observed")
            if (it) {
                Utils.hideWithAnimationY(binding.tabs)
            } else Utils.showWithAnimationY(binding.tabs)
        })
    }


    private fun prepareUi() {
        setViewPager()
        setupTabIcons()
    }

    @Inject
    lateinit var userProfileFragmentReg: UserProfileFragment
    private fun showUserUpdateFragment() {
        userProfileFragmentReg.apply {
            arguments = bundleOf(
                Constants.UserProfileFragmentReason to UserProfileFragment.Status.REGISTRATION
            )
        }
        supportFragmentManager.beginTransaction()
            .add(
                R.id.mainFragmentContainer,
                userProfileFragmentReg,
                Constants.UserProfileFragmentTag
            )
            .commit()
    }

    @Inject
    lateinit var userProfileFragmentOther: UserProfileFragment
    private fun showUserProfileFragment(user: User) {
        val popBackStackImmediate =
            supportFragmentManager.popBackStackImmediate(Constants.UserProfileFragmentTag, 0)
        if (!popBackStackImmediate) {
            userProfileFragmentOther.apply {
                arguments = bundleOf(
                    Constants.UserProfileFragmentReason to UserProfileFragment.Status.OTHER_USER,
                    Constants.UserProfileFragmentSelectedUser to user
                )
            }
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.from_right, R.anim.to_left, R.anim.from_left, R.anim.to_right
                )
                .replace(
                    R.id.mainFragmentContainer,
                    userProfileFragmentOther,
                    Constants.UserProfileFragmentTag
                )
                .addToBackStack(Constants.UserProfileFragmentTag)
                .commit()
        }

        setClickable(true)
    }

    private fun showConversationDetailFragment(user: User) {
        val popBackStackImmediate =
            supportFragmentManager.popBackStackImmediate(Constants.ConversationDetailFragmentTag, 0)
        if (!popBackStackImmediate) {
            conversationDetailFragment.apply {
                arguments = bundleOf(
                    Constants.ConversationDetailFragmentSelectedUser to user
                )
            }
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.from_right, R.anim.to_left, R.anim.from_left, R.anim.to_right
                )
                .replace(
                    R.id.mainFragmentContainer,
                    conversationDetailFragment,
                    Constants.ConversationDetailFragmentTag
                )
                .addToBackStack(Constants.ConversationDetailFragmentTag)
                .commit()
        }
    }

    @Inject
    lateinit var userListFragment: UserListFragment
    private fun showUserList() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, 0, R.anim.from_left, R.anim.to_right)
            .replace(
                R.id.mainFragmentContainer,
                userListFragment,
                Constants.UserListFragmentTag
            ).addToBackStack(Constants.UserListFragmentTag)
            .commit()
        setClickable(true)
    }

    private fun setClickable(status: Boolean) {
        binding.mainFragmentContainer.isClickable = status
        binding.mainFragmentContainer.isFocusable = status
    }

    @Inject
    lateinit var mapsFragment: MapsFragment

    @Inject
    lateinit var conversationListFragment: ConversationListFragment

    @Inject
    lateinit var userProfileFragmentAuth: UserProfileFragment
    private fun setViewPager() {
        userProfileFragmentAuth.apply {
            arguments = bundleOf(
                Constants.UserProfileFragmentReason to UserProfileFragment.Status.AUTH_USER
            )
        }
        val sectionsPagerAdapter =
            SectionsPagerAdapter(
                supportFragmentManager
            )
        sectionsPagerAdapter.setFragmentList(
            listOf(
                mapsFragment,
                conversationListFragment,
                userProfileFragmentAuth
            )
        )
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.viewPager.offscreenPageLimit = 3
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    private fun setupTabIcons() {
        val bindingTab1 = CustomtabBinding.inflate(layoutInflater)
        val bindingTab2 = CustomtabBinding.inflate(layoutInflater)
        val bindingTab3 = CustomtabBinding.inflate(layoutInflater)
        bindingTab1.icon.setBackgroundResource(R.drawable.ic_map)
        bindingTab2.icon.setBackgroundResource(R.drawable.ic_message)
        bindingTab3.icon.setBackgroundResource(R.drawable.ic_profile)
        binding.tabs.getTabAt(0)!!.customView = bindingTab1.root
        binding.tabs.getTabAt(1)!!.customView = bindingTab2.root
        binding.tabs.getTabAt(2)!!.customView = bindingTab3.root

    }

    override fun onBackPressed() {
        hideKeyboard()
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isVisible && hasBackStack(fragment)) {
                if (popFragment(fragment)) return
            }
        }
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount == 0)
            setClickable(false)
        Log.i(
            TAG,
            "supportFragmentManager.backStackEntryCount : ${supportFragmentManager.backStackEntryCount}"
        )
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = this.currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun hasBackStack(fragment: Fragment): Boolean {
        return fragment.childFragmentManager.backStackEntryCount > 0
    }

    private fun popFragment(fragment: Fragment): Boolean {
        val fragmentManager = fragment.childFragmentManager
        for (childFragment in fragment.childFragmentManager.fragments) {
            if (childFragment.isVisible) {
                return if (hasBackStack(childFragment)) {
                    popFragment(childFragment)
                } else {
                    fragmentManager.popBackStack()
                    true
                }
            }
        }
        return false
    }

}