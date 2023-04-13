package sandbox

import com.google.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle


@Singleton
class Application @Inject()(lifecycle: ApplicationLifecycle) extends AnyRef {
    init()

    private def init(): Unit = {
        println("********************************************")

    }
    // Shut-down hook
    //lifecycle.addStopHook { () =>
    //}
}
