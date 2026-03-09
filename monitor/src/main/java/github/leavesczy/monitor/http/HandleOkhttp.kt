package github.leavesczy.monitor.http

import github.leavesczy.monitor.MonitorInterceptor
import okhttp3.Interceptor


/**
 * Created By Mahongyin
 * Date    2025/9/12 11:49
 *
 */
object HandleOkhttp {

    //有从来ASM修改字节码对OKHTTP进行hook用的
    val hookInterceptors = listOf(
        MonitorInterceptor()
    )
}