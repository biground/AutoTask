package top.xjunz.tasker.engine.variable

/**
 * 管理所有已注册的魔术变量提供者
 */
class MagicVariableRegistry {
    private val providers = mutableMapOf<String, MagicVariableProvider>()

    fun register(provider: MagicVariableProvider) {
        providers[provider.name] = provider
    }

    fun get(name: String): MagicVariableProvider? = providers[name]

    fun getAllNames(): Set<String> = providers.keys.toSet()

    fun resolve(name: String): Variable? {
        val provider = providers[name] ?: return null
        return Variable(
            id = "magic_$name",
            name = name,
            type = provider.type,
            value = provider.getCurrentValue(),
            scope = VariableScope.GLOBAL,
            persisted = false
        )
    }
}
