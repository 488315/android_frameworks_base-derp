/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.common.ui.compose

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmap
import com.android.systemui.common.shared.model.Icon

/**
 * Icon composable that draws [icon] using [tint].
 *
 * Note: You can use [Color.Unspecified] to disable the tint and keep the original icon colors.
 */
@Composable
fun Icon(icon: Icon, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    val contentDescription = icon.contentDescription?.load()
    when (icon) {
        is Icon.Loaded -> {
            Icon(
                remember(icon.drawable) { icon.drawable.toBitmap().asImageBitmap() },
                contentDescription,
                modifier,
                tint,
            )
        }
        is Icon.Resource -> {
            val context = LocalContext.current
            val drawable = context.getDrawable(icon.res)
            if (drawable != null) {
                // Convert drawable to bitmap to handle unsupported types
                // This handles cases like StateListDrawable, LayerDrawable, etc.
                Icon(
                    remember(drawable) { drawable.toBitmap().asImageBitmap() },
                    contentDescription,
                    modifier,
                    tint,
                )
            } else {
                // Fallback to a default error icon if drawable loading fails
                Icon(
                    painterResource(android.R.drawable.ic_dialog_alert),
                    contentDescription,
                    modifier,
                    tint,
                )
            }
        }
    }
}
