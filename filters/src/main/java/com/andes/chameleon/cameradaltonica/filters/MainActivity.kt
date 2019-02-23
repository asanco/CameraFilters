package com.andes.chameleon.cameradaltonica.filters

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.andes.chameleon.cameradaltonica.filters.Adapter.ViewPagerAdapter
import com.andes.chameleon.cameradaltonica.filters.Interface.EditImageFragmentListener
import com.andes.chameleon.cameradaltonica.filters.Interface.FilterListFragmentListener
import com.andes.chameleon.cameradaltonica.filters.Utils.BitmapUtils
import com.andes.chameleon.cameradaltonica.filters.Utils.NonSwipeableViewPage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), FilterListFragmentListener, EditImageFragmentListener {

    val SELECT_GALLERY_PERMISSION = 1000

    init {
        System.loadLibrary("NativeImageProcessor")
    }

    object Main{
        val IMAGE_NAME = "flash.jpg"
    }

    override fun onBrightnessChanged(brightness: Int) {
        brightnessFinal = brightness
        val myFilter = Filter()
        myFilter.addSubFilter(BrightnessSubFilter(brightness))
        image_preview.source.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onSaturationChanged(saturation: Float) {
        saturationFinal = saturation
        val myFilter = Filter()
        myFilter.addSubFilter(SaturationSubfilter(saturation))
        image_preview.source.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))    }

    override fun onConstraintChanged(constraint: Float) {
        contrastFinal = constraint
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(constraint))
        image_preview.source.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))    }

    override fun onEditStarted() {
    }

    override fun onEditCompleted() {
        val bitmap: Bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true)
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(contrastFinal))
        myFilter.addSubFilter(BrightnessSubFilter(brightnessFinal))
        myFilter.addSubFilter(ContrastSubFilter(saturationFinal))
        finalImage = myFilter.processFilter(bitmap)
    }

    override fun onFilterSelected(filter: Filter) {
        resetControls()
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        image_preview.source.setImageBitmap(filter.processFilter(filteredImage))
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun resetControls(){
        if(editImageFragment != null)
            editImageFragment.resetControls()
        brightnessFinal = 0
        saturationFinal = 1.0f
        contrastFinal = 1.0f
    }

    internal var originalImage: Bitmap?= null
    internal lateinit var filteredImage: Bitmap
    internal lateinit var finalImage: Bitmap

    internal lateinit var filterListFragment: FilterListFragment
    internal lateinit var editImageFragment: EditImageFragment
    internal var brightnessFinal = 0
    internal var saturationFinal = 1.0f
    internal var contrastFinal = 1.0f
    internal var imageUri: Uri?=null
    internal val CAMERA_REQUEST: Int = 9999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title="Color Blindness Filter"

        loadImage()
        setupViewPager(viewPager)
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(viewPager: NonSwipeableViewPage?) {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        filterListFragment = FilterListFragment()
        filterListFragment.setListener(this)

        editImageFragment = EditImageFragment()
        editImageFragment.setListener(this)

        adapter.addFragment(filterListFragment, "FILTERS")
        adapter.addFragment(editImageFragment, "EDIT")

        viewPager!!.adapter = adapter
    }

    private fun loadImage() {
        originalImage = BitmapUtils.getBitmapFromAssets(this, Main.IMAGE_NAME, 300,300)
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        image_preview.source.setImageBitmap(originalImage)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if(id == R.id.action_open){
            openImageFromGallery()
            return true
        }else if (id == R.id.action_save){
            saveImageToGallery()
            return true
        }else if (id == R.id.action_camera){
            openCamera()
            return true
        }


        return super.onOptionsItemSelected(item)
    }

    private fun openCamera() {
        Dexter.withActivity(this)
            .withPermissions(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                       var values = ContentValues()
                       values.put(MediaStore.Images.Media.TITLE, "New Picture")
                       values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                       imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                        var cameraIntent = Intent (android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST)

                    } else
                        Toast.makeText(applicationContext, "Permissions denied", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }

            }).check()
    }

    private fun saveImageToGallery() {
        Dexter.withActivity(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                        val path = BitmapUtils.insertImage(contentResolver,
                            finalImage,
                            System.currentTimeMillis().toString() + "_profile.jpg",
                            description = "")
                        if(!TextUtils.isEmpty(path)){
                            val snackBar = Snackbar.make(coordinator, "Image saved to gallery", Snackbar.LENGTH_LONG)
                            snackBar.show()
                        }
                        else {
                            val snackBar = Snackbar.make(coordinator, "Unable to save file", Snackbar.LENGTH_LONG)
                                .setAction("OPEN", {
                                    openImage(path)
                                })
                            snackBar.show()
                        }
                    }else
                        Toast.makeText(applicationContext, "Permissions denied", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }

            }).check()

    }

    private fun openImage(path: String?) {
        val intent = Intent ()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(Uri.parse(path), "image/*")
        startActivity(intent)
    }

    private fun openImageFromGallery() {
        val withListener = Dexter.withActivity(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted())
                    {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/"
                        startActivityForResult(intent, SELECT_GALLERY_PERMISSION)
                    } else
                        Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }

            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK)
        {
            if (requestCode == SELECT_GALLERY_PERMISSION) {

                val bitmap = BitmapUtils.getBitmapFromGallery(this, data!!.data!!, 800, 800)

                originalImage!!.recycle()
                finalImage!!.recycle()
                filteredImage!!.recycle()

                originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
                finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
                image_preview.source.setImageBitmap(originalImage)
                bitmap.recycle()

                filterListFragment = FilterListFragment.getInstance(originalImage!!)
                filterListFragment.setListener(this)

            } else if (requestCode == CAMERA_REQUEST) {
                val bitmap = BitmapUtils.getBitmapFromGallery(this, imageUri!!, 800, 800)
                var image_selected_uri = imageUri!!

                originalImage!!.recycle()
                finalImage!!.recycle()
                filteredImage!!.recycle()

                originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
                finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
                image_preview.source.setImageBitmap(originalImage)
                bitmap.recycle()

                filterListFragment = FilterListFragment.getInstance(originalImage!!)
                filterListFragment.setListener(this)
            }

        }
    }
}
