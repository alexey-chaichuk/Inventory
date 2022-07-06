package com.chaichuk.inventory.ui.home

import android.graphics.Rect
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import by.kirich1409.viewbindingdelegate.viewBinding
import com.chaichuk.inventory.R
import com.chaichuk.inventory.databinding.FragmentHomeBinding
import com.chaichuk.inventory.ui.barcodes.BarcodeReporter
import com.chaichuk.inventory.ui.barcodes.BarcodeImageAnalyzer
import com.chaichuk.inventory.utils.showSnackbar
import com.chaichuk.inventory.utils.showToast
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), BarcodeReporter {

    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder().build()
        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        var camera: Camera? = null

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            // enable the following line if RGBA output is needed.
            // .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val imageAnalyzer = BarcodeImageAnalyzer(this)
        imageAnalysis.setAnalyzer(cameraExecutor, imageAnalyzer)

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    override fun reportValue(value: String) {
        binding.boxSelection.showSnackbar(value)
        requireContext().showToast(value)
    }

    override fun reportBoundingBox(boundingBox: Rect) {
        (binding.boxSelection.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = 200 //boundingBox.top
            leftMargin = 300 //boundingBox.left
            width = 300 //boundingBox.right - boundingBox.left
            height = 100 //boundingBox.bottom - boundingBox.top
        }
    }
}