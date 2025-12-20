package br.com.brunomateus.todolist

import android.app.Application
import br.com.brunomateus.todolist.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TodoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TodoApplication)
            modules(appModule)
        }
    }
}