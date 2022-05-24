package com.kdapps.productscanner.ui.database

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdapps.productscanner.R
import com.kdapps.productscanner.SettingsActivity
import com.kdapps.productscanner.data.Product
import com.kdapps.productscanner.databinding.FragmentDatabaseBinding


class DatabaseFragment : Fragment() {

    private lateinit var databaseViewModel          : DatabaseViewModel
    private          var _binding                   : FragmentDatabaseBinding? = null
    private          var rvAdapter                  : ListAdapter? = null
    private lateinit var rvFilterAdapter            : FilterListAdapter

    private          var itemTouchHelperFilter      : ItemTouchHelper? = null
    private          var itemTouchHelper            : ItemTouchHelper? = null

    private val filterBuffer : ArrayList<Product> = ArrayList()

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        databaseViewModel = ViewModelProvider(this).get(DatabaseViewModel::class.java)

        _binding = FragmentDatabaseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Recycler View

        rvFilterAdapter = FilterListAdapter(requireContext())

        itemTouchHelperFilter = ItemTouchHelper(SwipeToDeleteCallback(rvFilterAdapter, activity?.application))

        binding.topAppBar.setOnClickListener { binding.listRecycler.smoothScrollToPosition(0) }

        Firebase.auth.addAuthStateListener {
            if(Firebase.auth.currentUser == null){
                // Disabling swipe
                itemTouchHelper?.attachToRecyclerView(null)
                itemTouchHelperFilter?.attachToRecyclerView(null)

                // Stopping adapter listening for database changes
                rvAdapter?.stopListening()
                rvAdapter?.notifyDataSetChanged()

                // Destroy adapter and set
                rvAdapter = null
                binding.listRecycler.adapter    = rvAdapter
                (binding.listRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                //binding.listRecycler.swapAdapter(rvAdapter,true)

            }else{
                // Creating new adapter with new options
                rvAdapter                       = ListAdapter(requireContext(), createOptionsForAdapter())

                // Enabling swipe
                itemTouchHelper                 = ItemTouchHelper(SwipeToDeleteCallback(rvAdapter!!, activity?.application))
                itemTouchHelper?.attachToRecyclerView(binding.listRecycler)

                // Set new adapter
                binding.listRecycler.adapter    = rvAdapter

                // Starting adapter listening fro database changes
                rvAdapter!!.startListening()
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (context as AppCompatActivity).setSupportActionBar(binding.topAppBar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        rvAdapter?.stopListening()
        rvAdapter?.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.database_top_menu, menu)

        // Reselect (double tap) of menu item opens search with keyboard
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.setOnItemReselectedListener {
            if (it.itemId == R.id.navigation_database) {
                menu.getItem(1).expandActionView()
            }
        }

        // Removing background and setting hint of search view
        val searchView = menu.findItem(R.id.navigation_search)?.actionView as SearchView
        searchView.queryHint = getString(R.string.search_product_hint)

        val backgroundView = searchView.findViewById(androidx.appcompat.R.id.search_plate) as View
        backgroundView.background = null


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(rvAdapter != null) {


                    if (newText.isEmpty()) {
                        binding.listRecycler.adapter = rvAdapter
                        itemTouchHelperFilter?.attachToRecyclerView(null)
                        itemTouchHelper?.attachToRecyclerView(binding.listRecycler)
                        return false
                    }

                    filterBuffer.clear()

                    for (i in 0 until rvAdapter!!.itemCount) {
                        if (rvAdapter!!.getItem(i).product_code.lowercase().contains(newText.lowercase())) {
                            filterBuffer.add(rvAdapter!!.getItem(i))
                        }
                    }

                    itemTouchHelper?.attachToRecyclerView(null)
                    itemTouchHelperFilter?.attachToRecyclerView(binding.listRecycler)

                    binding.listRecycler.adapter = rvFilterAdapter
                    rvFilterAdapter.setData(filterBuffer)

                }

                return false
            }
        })

        menu.getItem(1).setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                itemTouchHelperFilter?.attachToRecyclerView(null)
                itemTouchHelper?.attachToRecyclerView(binding.listRecycler)
                binding.listRecycler.adapter = rvAdapter
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.navigation_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if(!hidden){
            binding.listRecycler.scrollToPosition(0)
        }
    }

    fun createOptionsForAdapter() : FirebaseRecyclerOptions<Product>{

        return FirebaseRecyclerOptions.Builder<Product>()
            .setQuery(databaseViewModel.getAllProducts() ,Product::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

    }
}