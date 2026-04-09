package top.xjunz.tasker.engine.mode

sealed class ModeChangeEvent {
    data class Activated(val mode: Mode, val previousMode: Mode?) : ModeChangeEvent()
    data class Deactivated(val mode: Mode) : ModeChangeEvent()
    data class Created(val mode: Mode) : ModeChangeEvent()
    data class Deleted(val modeId: String) : ModeChangeEvent()
}
