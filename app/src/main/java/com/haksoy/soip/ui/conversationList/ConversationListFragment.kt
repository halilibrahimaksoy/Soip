package com.haksoy.soip.ui.conversationList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.haksoy.soip.R
import com.haksoy.soip.data.user.User
import com.haksoy.soip.databinding.FragmentConversationListBinding
import com.haksoy.soip.ui.main.SharedViewModel
import com.haksoy.soip.utlis.showMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConversationListFragment @Inject constructor() : Fragment(),
    ConversationListAdapter.ConversationListItemClickListener {

    private lateinit var binding: FragmentConversationListBinding
    private lateinit var adapter: ConversationListAdapter

    companion object {
        fun newInstance() = ConversationListFragment()
    }

    private val viewModel: ConversationListViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversationListBinding.inflate(inflater, container, false)

        initiateData()
        adapter = ConversationListAdapter(this)
        binding.chatRecyclerView.adapter = adapter
        setHasOptionsMenu(true)

        viewModel.errorMessages.observe(viewLifecycleOwner, requireContext()::showMessage)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getConversationWithUserList().observe(viewLifecycleOwner, Observer {
            adapter.setDataList(it)
            showEmptyMessageIfNecessary(it.isNotEmpty())
        })
    }

    private fun showEmptyMessageIfNecessary(isNotEmpty: Boolean) {
        if (isNotEmpty) {
            binding.txtChatMessage.visibility = View.GONE
            binding.chatRecyclerView.visibility = View.VISIBLE
        } else {
            binding.txtChatMessage.visibility = View.VISIBLE
            binding.chatRecyclerView.visibility = View.GONE
        }
    }

    private fun initiateData() {
        binding.chatRecyclerView.setHasFixedSize(true)
        binding.chatRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.chatRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                DividerItemDecoration.VERTICAL
            )
        )
//        val swipeHandler = object : SwipeToDeleteCallback() {
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                onRemoveRequest(viewHolder.absoluteAdapterPosition)
//
//            }
//        }
//        val itemTouchHelper = ItemTouchHelper(swipeHandler)
//        itemTouchHelper.attachToRecyclerView(binding.chatRecyclerView)
    }

    private fun onRemoveRequest(position: Int) {
//        val dialogBinding =
//            ConversationDeteleMenuBinding.inflate(layoutInflater, null, false)
//
//        dialogBinding.blurView.setupWith(binding.root)
//            .setFrameClearDrawable(binding.root.background)
//            .setBlurAutoUpdate(true)
//
//        val builder =
//            AlertDialog.Builder(
//                requireContext(),
//                android.R.style.Theme_DeviceDefault_Light_NoActionBar
//            ).setView(dialogBinding.root).setCancelable(false)
//
//
//        val dialog = builder!!.show()
//        dialogBinding.btnCancel.setOnClickListener {
//            adapter.notifyDataSetChanged()
//            dialog.dismiss()
//        }
//        dialogBinding.btnDeleteForMe.text = getString(R.string.delete)
//        dialogBinding.btnDeleteForMe.setOnClickListener {
//            removeOnlyMe(position)
//            dialog.dismiss()
//        }
//        dialogBinding.btnDeleteEveryone.visibility = View.GONE

    }


    private fun removeOnlyMe(position: Int) {
        viewModel.removeConversationAtPosition(position)
        adapter.removeAt(position)
    }

    override fun onClickedUser(user: User) {
        sharedViewModel.conversationDetailWithUser.postValue(user)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        binding.toolbar.menu.clear()
        binding.toolbar.inflateMenu(R.menu.conversation_list_menu)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

    }
}
