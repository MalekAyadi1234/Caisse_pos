package com.caisse.pos

import android.app.Application
import com.caisse.pos.data.local.AppDatabase
import com.caisse.pos.data.remote.FirebaseSaleRemote
import com.caisse.pos.data.repository.SaleRepository
import com.caisse.pos.print.SimulatedTicketPrinter
import com.caisse.pos.util.DeviceIdProvider
import com.google.firebase.database.FirebaseDatabase

class CaisseApp : Application() {

    lateinit var saleRepository: SaleRepository
        private set

    lateinit var devicePrefix: String
        private set

    override fun onCreate() {
        super.onCreate()

        // cache offline Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val device = DeviceIdProvider.getOrCreate(this)
        devicePrefix = device.prefix

        val db = AppDatabase.getInstance(this)
        val remote = FirebaseSaleRemote(FirebaseDatabase.getInstance())
        // mettre 0.2 si tu veux tester les échecs d'impression
        val printer = SimulatedTicketPrinter(failureRate = 0.0)

        saleRepository = SaleRepository(
            db = db,
            remote = remote,
            printer = printer,
            device = device
        )
        saleRepository.start()
    }
}
