package com.example.musicplayerapp.ui.theme

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.musicplayerapp.R
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 1
        var musicFiles: ArrayList<MusicFiles> = ArrayList()
        var albums :ArrayList<MusicFiles> =ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permission()
    }

    private fun permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        } else {
            musicFiles = getAllAudio(this)
            initViewPager()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                musicFiles = getAllAudio(this)
                initViewPager()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Storage permission is required. Please enable it in settings.", Toast.LENGTH_LONG).show()
                    openAppSettings()
                } else {
                    Toast.makeText(this, "Permission Denied! Please allow storage access.", Toast.LENGTH_SHORT).show()
                    permission()
                }
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + packageName)
        startActivity(intent)
    }

    private fun initViewPager() {
        val viewPager: ViewPager = findViewById(R.id.viewpager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragments(SongsFragment(), "Songs")
        viewPagerAdapter.addFragments(AlbumFragment(), "Albums")
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        fun addFragments(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence = titles[position]
    }

    private fun getAllAudio(context: Context): ArrayList<MusicFiles> {

        val duplicate = ArrayList<String>()
        val tempAudioList = ArrayList<MusicFiles>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val album = it.getString(0)
                val title = it.getString(1)
                val duration = it.getString(2)
                val path = it.getString(3)
                val artist = it.getString(4)

                val musicFile = MusicFiles(path, title, artist, album, duration)
                tempAudioList.add(musicFile)
                if (!duplicate.contains(album)) {
                    albums.add(musicFile)
                    duplicate.add(album)
                }
            }
        }
        return tempAudioList
    }
}