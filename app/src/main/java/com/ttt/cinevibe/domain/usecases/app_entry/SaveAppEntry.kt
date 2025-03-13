package com.ttt.cinevibe.domain.usecases.app_entry

import com.ttt.cinevibe.domain.manager.LocalUserManager

class SaveAppEntry(
    private val localUserManager: LocalUserManager
) {
    suspend operator fun invoke() {
        localUserManager.saveAppEntry()
    }
}