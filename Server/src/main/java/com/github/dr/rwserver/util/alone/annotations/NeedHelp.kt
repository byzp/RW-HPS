/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.alone.annotations

/**
 * 指定方法是有可用的,但是存在隐藏的问题, 需要进行重构 但是由于新的语言或新的特性无法完成 需要帮助
 * 一般来说，这并不会改变IDEA的行为——它只是一个标记，表明指定方法是未完成的。
 * The specified method is defective and needs to be overridden.
 * In general, this doesn't change the behavior of the idea - it's just a token that indicates that the specified method is incomplete。
 * @author Dr
 */
@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class NeedHelp 