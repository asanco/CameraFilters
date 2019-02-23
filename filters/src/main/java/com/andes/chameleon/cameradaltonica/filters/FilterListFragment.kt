package com.andes.chameleon.cameradaltonica.filters


import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andes.chameleon.cameradaltonica.filters.Adapter.ThumbnailAdapter
import com.andes.chameleon.cameradaltonica.filters.Interface.FilterListFragmentListener
import com.andes.chameleon.cameradaltonica.filters.Utils.BitmapUtils
import com.andes.chameleon.cameradaltonica.filters.Utils.SpaceItemDecoration
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import kotlinx.android.synthetic.main.fragment_filter_list.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FilterListFragment : Fragment(), FilterListFragmentListener{

    internal lateinit var recycler_view: RecyclerView
    internal var listener : FilterListFragmentListener?=null
    internal lateinit var adapter:ThumbnailAdapter
    internal lateinit var thumbnailItemList: MutableList<ThumbnailItem>

    companion object {
        internal var instance:FilterListFragment? = null
        internal var bitmap: Bitmap?= null

        fun getInstance(bitmapSave:Bitmap?): FilterListFragment{
            bitmap = bitmapSave
            if(instance == null){
                instance = FilterListFragment()
            }
            return instance!!
        }
    }

    fun setListener(listFragmentListener: FilterListFragmentListener){
        this.listener = listFragmentListener
    }

    override fun onFilterSelected(filter: Filter) {
        if (listener != null)
            listener!!.onFilterSelected(filter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val itemView = inflater.inflate(R.layout.fragment_filter_list, container, false)

        thumbnailItemList = ArrayList()
        adapter = ThumbnailAdapter(activity!!, thumbnailItemList, this)

        recycler_view = itemView.findViewById<RecyclerView>(R.id.recycler_view)
        recycler_view.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recycler_view.itemAnimator = DefaultItemAnimator()
        val space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9f, resources.displayMetrics).toInt()
        recycler_view.addItemDecoration(SpaceItemDecoration(space))
        recycler_view.adapter = adapter

        displayImage(bitmap)

        return itemView
    }

    fun displayImage(bitmap: Bitmap?) {
        val r = Runnable {
            val thumbImage : Bitmap?

            if(bitmap == null)
                thumbImage = BitmapUtils.getBitmapFromAssets(activity!!, MainActivity.Main.IMAGE_NAME, 100, 100)
            else
                thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false)

            if(thumbImage == null)
                return@Runnable

            ThumbnailsManager.clearThumbs()
            thumbnailItemList.clear()

            val thumbnailItem = ThumbnailItem()
            thumbnailItem.image = thumbImage
            thumbnailItem.filterName = "Normal"
            ThumbnailsManager.addThumb(thumbnailItem)

            val filters = FilterPack.getFilterPack(activity!!)

            for (filter in filters){
                val item = ThumbnailItem()
                item.image = thumbImage
                item.filter = filter
                item.filterName = filter.name
                ThumbnailsManager.addThumb(item)
            }

            thumbnailItemList.addAll(ThumbnailsManager.processThumbs(activity))
            activity!!.runOnUiThread{
                adapter.notifyDataSetChanged()
            }
        }
        Thread(r).start()
    }


}
