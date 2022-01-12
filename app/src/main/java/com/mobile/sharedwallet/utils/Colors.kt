package com.mobile.sharedwallet.utils

import java.util.*

class Colors {

    companion object {
        private val palette = arrayListOf(
            "#3da4ab", "#f6cd61", "#fe8a71", "#dec3c3", "#e7d3d3", "#f0e4e4", "#f9f4f4",
            "#051e3e", "#251e3e", "#451e3e", "#651e3e", "#851e3e", "#011f4b", "#03396c",
            "#005b96", "#6497b1", "#b3cde0", "#eee3e7", "#ead5dc", "#eec9d2", "#f4b6c2",
            "#f6abb6", "#fe4a49", "#2ab7ca", "#fed766", "#e6e6ea", "#fe9c8f", "#feb2a8",
            "#fec8c1", "#fad9c1", "#f9caa7", "#ee4035", "#f37736", "#fdf498", "#7bc043",
            "#0392cf", "#96ceb4", "#ffeead", "#ff6f69", "#ffcc5c", "#88d8b0", "#008744",
            "#0057e7", "#d62d20", "#ffa700", "#ffffff", "#3385c6", "#4279a3", "#476c8a",
            "#a8e6cf", "#dcedc1", "#ffd3b6", "#ffaaa5", "#ff8b94", "#d11141", "#00b159",
            "#00aedb", "#f37735", "#ffc425", "#ff77aa", "#ff99cc", "#ffbbee", "#edc951",
            "#eb6841", "#cc2a36", "#4f372d", "#00a0b0"
        )

        fun randomColor() : String {
            return palette[Random().nextInt(palette.size - 1)]
        }
    }
}