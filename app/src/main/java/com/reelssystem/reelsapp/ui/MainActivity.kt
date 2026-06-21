package com.reelssystem.reelsapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.reelssystem.reelsapp.R
import com.reelssystem.reelsapp.databinding.ActivityMainBinding
import com.reelssystem.reelsapp.cache.CacheManagerReels
import com.reelssystem.reelsapp.player.MediaSetupFactory
import com.reelssystem.reelsapp.player.PreloadConfig
import com.reelssystem.reelsapp.cache.ReelsConfig
import com.reelssystem.reelsapp.player.ReelsPlayerManager
import com.reelssystem.reelsapp.player.ReelsPreloadManager
import com.reelssystem.reelsapp.presentation.ReelsViewModel

/**
 * Entry point wiring everything together: RecyclerView + snapping,
 * the shared player, disk cache, and preload manager, then handing
 * scroll/playback ownership to ReelsScrollController.
 *
 * Deliberately thin — no business logic lives here, just construction
 * and wiring order. See initReelsUi(), initMedia(), and
 * setupScrollController() for the three phases of setup; their order
 * matters since later phases capture earlier ones in lambdas (e.g. the
 * adapter's isCurrentlyPlaying callback references scrollController,
 * which isn't assigned until setupScrollController() runs).
 */
@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ReelsViewModel by viewModels()

    private var reelsAdapter: ReelsAdapter? = null
    private var snapHelper: SnapHelper? = null

    private lateinit var reelsPreloadManager: ReelsPreloadManager
    private var preloadManager: DefaultPreloadManager? = null
    private lateinit var scrollController: ReelsScrollController

    private val config = ReelsConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initReelsUi()
        initMedia()
        setupScrollController()
    }

    override fun onDestroy() {
        super.onDestroy()
        ReelsPlayerManager.release()
        if (::reelsPreloadManager.isInitialized) {
            reelsPreloadManager.release()
        }
        CacheManagerReels.release()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initReelsUi() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.reelsRv.layoutManager = layoutManager

        snapHelper = PagerSnapHelper()
        snapHelper?.attachToRecyclerView(binding.reelsRv)

        reelsAdapter = ReelsAdapter { position -> scrollController.currentPosition() == position }
        binding.reelsRv.adapter = reelsAdapter

        reelsAdapter?.setDataset(viewModel.reels)
        reelsAdapter?.notifyDataSetChanged()
    }

    private fun initMedia() {
        CacheManagerReels.init(this, config)
        val cacheFactory = CacheManagerReels.getCacheDataSourceFactory()
        val preloadConfig = PreloadConfig(preloadForwardRange = 2)

        val setup = MediaSetupFactory.buildPreloadSetup(
            context = this,
            reels = viewModel.reels,
            cacheFactory = cacheFactory,
            preloadConfig = preloadConfig,
            statusControl = MyTargetPreloadStatusControl()
        )

        reelsPreloadManager = setup.reelsPreloadManager
        preloadManager = setup.preloadManager
        ReelsPlayerManager.init(setup.builder)

    }

    private fun setupScrollController() {
        scrollController = ReelsScrollController(
            recyclerView = binding.reelsRv,
            snapHelper = requireNotNull(snapHelper),
            adapter = requireNotNull(reelsAdapter),
            preloadManager = preloadManager,
            reelsPreloadManager = reelsPreloadManager,
            playerProvider = { ReelsPlayerManager.player }
        )
        scrollController.attach()

        ReelsPlayerManager.player?.addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() {
                scrollController.currentHolder()?.hideViewsOnFirstFrame()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val keepOn = (ReelsPlayerManager.player?.playWhenReady == true) && isPlaying
                setKeepScreenOn(keepOn)
            }
        })

        binding.reelsRv.post { scrollController.autoplayFirstReel() }
        reelsPreloadManager.updatePreloadWindow(0)
    }

    inner class MyTargetPreloadStatusControl : TargetPreloadStatusControl<Int, DefaultPreloadManager.PreloadStatus> {
        override fun getTargetPreloadStatus(index: Int): DefaultPreloadManager.PreloadStatus? {
            return reelsPreloadManager.getPreloadStatus(index)
        }
    }

    private fun setKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}