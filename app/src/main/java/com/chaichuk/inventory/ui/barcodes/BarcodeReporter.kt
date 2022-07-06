package com.chaichuk.inventory.ui.barcodes

import android.graphics.Rect

interface BarcodeReporter {
    fun reportValue(value: String)
    fun reportBoundingBox(boundingBox: Rect)
}
