package com.example.myapplication

import android.support.annotation.LayoutRes
import android.support.v7.recyclerview.extensions.ListAdapter
import android.view.ViewGroup

fun <T> ListAdapter<T, *>.submitListCopy(items: List<T>) {
    submitList(ArrayList(items))
}

fun <T> ListAdapter<T, *>.createLayout(parent: ViewGroup, @LayoutRes layoutRes: Int)
        = android.view.LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)