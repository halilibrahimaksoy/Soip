package com.haksoy.soip.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.databinding.FragmentMediaGalleryBinding
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.showMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@AndroidEntryPoint
class MediaGalleryFragment : Fragment() {

    companion object {
        fun newInstance(userUid: String, selectedChatUid: String) = MediaGalleryFragment().apply {
            arguments = bundleOf(
                Constants.ConversationMediaFragmentUser to userUid,
                Constants.ConversationMediaFragmentSelectedChat to selectedChatUid
            )
        }
    }

  private val viewModel: MediaGalleryViewModel by viewModels()

    private lateinit var binding: FragmentMediaGalleryBinding
    private lateinit var adapter: MediaGalleryAdapter
    private lateinit var userUid: String
    private lateinit var selectedChatUid: String
    private var selectedChatPosition = -1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMediaGalleryBinding.inflate(inflater, container, false)
        setupViewPager()
        fillData()

        viewModel.errorMessages.observe(viewLifecycleOwner, requireContext()::showMessage)
        return binding.root
    }

    private fun fillData() {
        viewModel.getConversationMedia(userUid).observe(viewLifecycleOwner, Observer {
            adapter = MediaGalleryAdapter(it as ArrayList<Chat>)
            selectedChatPosition = it.indexOf(getChatFromList(it, selectedChatUid))
            adapter.setSelectedPosition(selectedChatPosition)
            binding.viewPager.adapter = adapter
            binding.viewPager.setCurrentItem(
                selectedChatPosition,
                false
            )
        })
    }

    private fun getChatFromList(chatList: List<Chat>, chatUid: String): Chat? {
        for (chat in chatList) {
            if (chat.uid == chatUid)
                return chat
        }
        return null
    }

    private fun setupViewPager() {
        binding.viewPager.addItemDecoration(
            DividerItemDecoration(
                this.context,
                DividerItemDecoration.HORIZONTAL
            )
        )
        binding.viewPager.clipToPadding = false
        binding.viewPager.isEnabled = false
        binding.viewPager.clipChildren = false
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        val compositeTransformer = CompositePageTransformer()
        compositeTransformer.addTransformer(MarginPageTransformer(20))
        compositeTransformer.addTransformer(ViewPager2.PageTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.75f + r * 0.25f
            page.scaleX = 0.75f + r * 0.25f
        })
        binding.viewPager.setPageTransformer(compositeTransformer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.get(Constants.ConversationMediaFragmentUser)?.let {
            userUid = it as String
        }
        arguments?.get(Constants.ConversationMediaFragmentSelectedChat)?.let {
            selectedChatUid = it as String
        }
    }

}