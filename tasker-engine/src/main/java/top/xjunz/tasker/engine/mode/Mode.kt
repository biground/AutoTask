package top.xjunz.tasker.engine.mode

data class Mode(
    val id: String,
    val name: String,
    val icon: String? = null,
    val color: Int? = null,
    val isActive: Boolean = false
)
