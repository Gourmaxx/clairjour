package com.clairjour.app.data

import android.content.Context
import com.clairjour.app.data.backup.BackupRepository
import com.clairjour.app.data.db.ClairjourDatabase
import com.clairjour.app.data.db.MilestoneDao
import com.clairjour.app.data.db.RelapseDao
import com.clairjour.app.data.motivations.MotivationsRepository
import com.clairjour.app.data.prefs.SettingsRepository
import com.clairjour.app.data.repository.AddictionRepository
import com.clairjour.app.data.repository.JournalRepository
import com.clairjour.app.data.repository.PledgeRepository
import com.clairjour.app.data.repository.RelapseRepository

interface AppContainer {
    val settingsRepository: SettingsRepository
    val motivationsRepository: MotivationsRepository
    val addictionRepository: AddictionRepository
    val journalRepository: JournalRepository
    val pledgeRepository: PledgeRepository
    val relapseRepository: RelapseRepository
    val milestoneDao: MilestoneDao
    val relapseDao: RelapseDao
    val backupRepository: BackupRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val db = ClairjourDatabase.get(context)

    override val settingsRepository = SettingsRepository(context.applicationContext)
    override val motivationsRepository = MotivationsRepository(context.applicationContext)
    override val addictionRepository = AddictionRepository(db.addictionDao())
    override val journalRepository = JournalRepository(db.journalDao())
    override val pledgeRepository = PledgeRepository(db.pledgeDao())
    override val relapseRepository = RelapseRepository(
        db.relapseDao(),
        db.milestoneDao(),
        db.addictionDao()
    )
    override val milestoneDao = db.milestoneDao()
    override val relapseDao = db.relapseDao()
    override val backupRepository = BackupRepository(db, context.contentResolver)
}
