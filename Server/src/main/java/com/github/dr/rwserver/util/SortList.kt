/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.struct.Sort
import java.io.File


/**
 * @author Dr
 */
object SortList {
    @JvmStatic
    fun sortByFileName(list: Seq<File>) {
        Sort.instance().sort(list) { o1, o2 ->
            o1.name.compareTo(o2.name)
        }
    }
}